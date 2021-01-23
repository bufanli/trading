package com.cbnb.jiaoyishi.util.DealPdf;

import com.cbnb.jiaoyishi.model.waipan.Bz;
import com.cbnb.jiaoyishi.model.waipan.Cchz;
import com.cbnb.jiaoyishi.model.waipan.Cjmx;
import com.cbnb.jiaoyishi.model.waipan.Zj;
import com.cbnb.jiaoyishi.util.SubstrBean;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.Logger;

/**
 * @Author: bfl
 * @Description: 期货公司：J.P. Morgan Securities plc
 * @Date: Create in 10:22 2021/01/23
 */
public class DealJpmorganPdfUtil {

    private static Logger logger = Logger.getLogger(DealJpmorganPdfUtil.class);
    private static String QHGSMC = "J.P. Morgan Securities plc";
    private static String STARTER = "JPMorgan.com/commodities";
    private static String SUMMARY = "Summary";
    private static String PROMPT= "Prompt:";
    private static String PAGE= "Page";
    private static String BUY = "Buy";
    private static String SELL= "Sell";
    private static String CCY_USD= "USD";
    private static String COMM = "Comm";
    private static String TO = "TO:";
    private static String DATE= "DATE:";
    private static int MAX_WORDS_OF_PZHYNR= 5;
    private static int OTHERS= 0;
    private static int PRE_ENTERING = 1;
    private static int ENTERING_CJMX = 2;
    private static int ENTERED_CJMX = 3;
    private static int ENTERING_CCHZ = 4;
    private static int ENTERED_CCHZ = 5;
    private static int status = OTHERS;
    /**
     * deal jpmorgan pdf utility
     * @param str pdf contents
     * @return
     */
    public static String dealJpmorgan(String str) {
        JSONObject jsonObject = new JSONObject();
        String[] results = str.split("\r\n");
        // analysis wfgsmc in max top 100 line
        String wfgsmc = "";
        String pzjynr= "";
        String prompt= "";
        List <Cjmx> cjmxes = new ArrayList<Cjmx>();
        List <Cchz> cchzes= new ArrayList<Cchz>();
        List <Zj> zjes= new ArrayList<Zj>();
        for (int i = 0;i < 100; i++){
            // clear ' for json
            results[i] = results[i].replace("'","“");
            // find wfgsmc
            if (results[i].startsWith(TO) ){
               wfgsmc = results[i].substring(TO.length(),results[i].indexOf(DATE));
               // trim
                wfgsmc = wfgsmc.trim();
                break;
            }
        }
        // extract cjmx and cchz
        for(int i = 0;i < results.length;i++){
            // extract cjmx
           String cjmxHeaderPattern = "^\\d{2}-\\w{3}-\\d{4}.*";
           if( (status == OTHERS) && (results[i].startsWith(STARTER)) ){
               status = PRE_ENTERING;
               continue;
           }
           if( (status == PRE_ENTERING) && (results[i].split(" ").length > MAX_WORDS_OF_PZHYNR) ){
              status = OTHERS;
              continue;
           }
           if ( (status == PRE_ENTERING) && (!results[i].equals(SUMMARY)) ){
               status = ENTERING_CJMX;
               pzjynr = results[i];
               continue;
           }
            if ( (status == PRE_ENTERING) && (results[i].equals(SUMMARY)) ){
               status = ENTERING_CCHZ;
               continue;
            }
            if ( (status == ENTERING_CJMX) && (results[i].startsWith(PROMPT))){
               prompt = results[i].substring(PROMPT.length());
               prompt = prompt.trim();
               continue;
            }
           if ( (status == ENTERING_CJMX || status == ENTERED_CJMX) && (Pattern.matches(cjmxHeaderPattern,results[i])) ){
               status = ENTERED_CJMX;
              // this is cjmx line
               String[] items= results[i].split(" ");
               int processPosition = 0;
               // contract date
               String contractDate = items[processPosition++];
               // contract id
               String contractId = items[processPosition++];
               //trade type
               StringBuffer tradeTypeBuffer = new StringBuffer();
               for (;processPosition < items.length;processPosition++) {
                   if (!items[processPosition].equals(BUY) && !items[processPosition].equals(SELL)) {
                       tradeTypeBuffer.append(items[processPosition]);
                       tradeTypeBuffer.append(" ");
                   } else {
                       break;
                   }
               }
               String tradeType = tradeTypeBuffer.toString();
               tradeType.trim();
               // client buy sell
               String clientBuySell = items[processPosition++];
               // contract price
               StringBuffer contractPriceBuffer= new StringBuffer();
               for (;processPosition < items.length;processPosition++) {
                   if (!items[processPosition].equals(CCY_USD)) {
                       contractPriceBuffer.append(items[processPosition]);
                       contractPriceBuffer.append(" ");
                   } else {
                       break;
                   }
               }
               contractPriceBuffer.append(items[processPosition++]);
               String contractPrice = contractPriceBuffer.toString();
               contractPrice = contractPrice.trim();
               // market price
               StringBuffer marketPriceBuffer = new StringBuffer();
               for (;processPosition < items.length;processPosition++){
                   if (!items[processPosition].equals(CCY_USD)){
                      marketPriceBuffer.append(items[processPosition]);
                      marketPriceBuffer.append(" ");
                   }else{
                       break;
                   }
               }
               marketPriceBuffer.append(items[processPosition++]);
               String marketPrice = marketPriceBuffer.toString();
               marketPrice.trim();
               // commission rate
               StringBuffer commissionRateBuffer = new StringBuffer();
               String next = items[processPosition + 1];
               if(next.equals(CCY_USD)){
                   commissionRateBuffer.append(items[processPosition++]);
                   commissionRateBuffer.append(" ");
                   commissionRateBuffer.append(items[processPosition++]);
               }else{
                   commissionRateBuffer.append(items[processPosition++]);
               }
               String commissionRate = commissionRateBuffer.toString();
               commissionRate = commissionRate.trim();
               //commission type
               String commissionType = items[processPosition++];
               //net qty
               String netQty = items[processPosition++];
               //net delta qty
               String netDeltaQty = items[processPosition++];
               //undiscounted
               String undiscounted = items[processPosition++];
               //discounted
               String discounted = items[processPosition++];
               // add items into cjmx object
               Cjmx cjmx = new Cjmx();
               cjmx.setQhgsmc(QHGSMC);
               cjmx.setWsmc(wfgsmc);
               cjmx.setQhgszh(null);
               cjmx.setCjrq(contractDate);
               cjmx.setPzhynr(pzjynr);
               cjmx.setHydqr(prompt);
               if (clientBuySell.equals(BUY)){
                   cjmx.setBuy(netQty);
               }else{
                   cjmx.setSell(netQty);
               }
               cjmx.setCjj(contractPrice);
               cjmx.setJsj(marketPrice);
               cjmx.setCcyk(undiscounted);
               cjmx.setHz(null);
               cjmx.setBz(CCY_USD);
               cjmxes.add(cjmx);
               continue;
            }
            if ( (status == ENTERED_CJMX) && (!Pattern.matches(cjmxHeaderPattern,results[i])) ){
                status = OTHERS;
                continue;
            }
            // end of cjmx
            // start of cchz
            if (status == ENTERING_CCHZ || status == ENTERED_CCHZ){
               if (results[i].startsWith(COMM)){
                   continue;
               }else if(results[i].startsWith(PAGE)) {
                   status = OTHERS;
                   continue;
               }else{
                    status = ENTERED_CCHZ;
                    //pzhynr
                    String items[] = results[i].split(" ");
                    int processPosition = 0;
                    StringBuffer pzhynrBuffer = new StringBuffer();
                    for (;processPosition < items.length;processPosition++) {
                        if (!items[processPosition].equals(CCY_USD)) {
                            pzhynrBuffer.append(items[processPosition]);
                           pzhynrBuffer.append(" ");
                        } else {
                            break;
                        }
                    }
                    String pzhynr = pzhynrBuffer.toString();
                    pzhynr.trim();
                    String ccy = items[processPosition++];
                    // net quantity
                    String netQty = items[processPosition++];
                    // net delta quantity
                    String netDelQty = items[processPosition++];
                    // undiscounted
                    String undiscounted = items[processPosition++];
                    // discounted
                    String discounted = items[processPosition++];
                    Cchz cchz = new Cchz();
                    cchz.setQhgsmc(QHGSMC);
                    cchz.setWsmc(wfgsmc);
                    cchz.setQhgszh(null);
                    cchz.setJys(null);
                    cchz.setPzhynr(pzhynr);
                    cchz.setHydqr(null);
                    cchz.setBuy(null);
                    cchz.setSell(null);
                    cchz.setCjjj(null);
                    cchz.setJsj(null);
                    cchz.setCcyk(undiscounted);
                    cchz.setHz(null);
                    cchz.setJbbzj(null);
                    cchz.setBz(ccy);
                    cchzes.add(cchz);
                    continue;
               }
            }//end of CCHZ
            if(results[i].startsWith(PAGE)){
                status = OTHERS;
                continue;
            }
        }
        return "{'cjmx':" + cjmxes + ",'cchz':" + cchzes + ",'zj':" + zjes+ "}";
    }
//    public static void main(String[] args) throws Exception {
//        String test = "29-Dec-2020 85000BH-SP25K Fwd Sell 7,840.00 USD 7,834.00 USD 0.65 USD Exl. -1,000.00 -1,000.00 5,350.00 5,349.93";
//        String cjmxHeaderPattern = "^\\d{2}-\\w{3}-\\d{4}.*";
//        if(Pattern.matches(cjmxHeaderPattern,test)){
//            System.out.println("matched");
//        }else{
//            System.out.println("not matched");
//        }
//    }
    public static void main(String[] args) throws Exception {
        System.out.println(new Date().getTime());
        File file = new File("D:\\00_study\\05_for_guopeng\\reference\\jpmorgen.pdf");
        try {
            // 新建一个PDF解析器对象
            PDFParser parser = new PDFParser(new RandomAccessFile(file, "rw"));
            // 对PDF文件进行解析
            parser.parse();
            // 获取解析后得到的PDF文档对象
            PDDocument pdfdocument = parser.getPDDocument();
            System.out.println(pdfdocument.getPages().getCount());
            // 新建一个PDF文本剥离器
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); //sort设置为true 则按照行进行读取，默认是false
            // 从PDF文档对象中剥离文本
            String result = stripper.getText(pdfdocument);
            DealJpmorganPdfUtil.dealJpmorgan(result);
        } catch (Exception e) {
            System.out.println("读取PDF文件" + file.getAbsolutePath() + "生失败！" + e);
            e.printStackTrace();
        }
    }
}
