package com.cbnb.jiaoyishi.util.DealPdf;

import com.cbnb.jiaoyishi.model.waipan.Bz;
import com.cbnb.jiaoyishi.model.waipan.Cchz;
import com.cbnb.jiaoyishi.model.waipan.Cjmx;
import com.cbnb.jiaoyishi.model.waipan.Zj;
import com.cbnb.jiaoyishi.util.SubstrBean;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: hcf
 * @Description: 期货公司：ADMIS
 * @Date: Create in 9:30 2021/1/12
 */
public class DealAdmisPdfUtil {
    public static String dealAdmis(String str) {
        int USDOLLARS_170_begin = 0;
        int USDOLLARS_170_end = 0;
        int USDOLLARS_326_begin = 0;
        int USDOLLARS_326_end = 0;
        String[] results = str.split("\r");
        boolean flag_170 = false;
        boolean flag_326 = false;
        String wfgsmc = results[16].replaceAll("T     h     i  s       i  s        a        p     r   e     l  i   m       i  n     a     r   y       r", "").replace("PTE LTD                                                                      e     p     o     r   t   .", "").trim();
        List<Bz> cjList = new ArrayList<Bz>();
        List<Bz> hzList = new ArrayList<Bz>();
        String accountnum = results[13].replaceAll(" ", "").replace("ACCOUNTNUMBER:", "").trim();
        boolean cj = false;
        boolean hz = false;
        for (int i = 0; i < results.length; i++) {
            if (results[i].replaceAll(" ", "").contains("ACCOUNTNUMBER:")) {
                accountnum = results[i].replaceAll(" ", "").replace("ACCOUNTNUMBER:", "");
            }
            if (results[i].replaceAll(" ", "").contains("**CONFIRMATION**")) {//成交明细开始
                cj = true;
                Bz bz = new Bz();
                bz.setAccountnum(accountnum);
                bz.setBegin(i + 4);
                cjList.add(bz);
            }
            if (cj && results[i].replaceAll(" ", "").contains("**OPENPOSITIONS**")) {//成交明细结束
                cjList.get(cjList.size() - 1).setEnd(i - 1);
                cj = false;
            }
            if (results[i].replaceAll(" ", "").contains("**OPENPOSITIONS**")) {//持仓汇总开始
                hz = true;
                Bz bz = new Bz();
                bz.setAccountnum(accountnum);
                bz.setBegin(i + 2);
                hzList.add(bz);
            }
            if (hz && results[i].replaceAll(" ", "").contains("**USDOLLARS**")) {//持仓汇总结束
                hzList.get(hzList.size() - 1).setEnd(i - 1);
                hz = false;
            }
            if (results[i].replaceAll(" ", "").contains("ACCOUNTNUMBER:SQ170")) {//资金170
                flag_170 = true;
            }
            if (results[i].replaceAll(" ", "").contains("ACCOUNTNUMBER:SQ326")) {//资金326
                flag_326 = true;
            }
            if (flag_170) {
                if (results[i].replaceAll(" ", "").contains("**USDOLLARS**")) {//资金相关开始
                    USDOLLARS_170_begin = i - 1;
                }
                if (results[i].replaceAll(" ", "").contains("**CONFIRMATION**")) {
                    USDOLLARS_170_end = i - 1;
                }
            }
            if (flag_326) {
                if (results[i].replaceAll(" ", "").contains("**USDOLLARS**")) {//资金相关开始
                    USDOLLARS_326_begin = i - 1;
                }
                USDOLLARS_326_end = results.length;
            }
        }
        JSONArray result = new JSONArray();
        //开始处理成交
        List<Cjmx> cjmxList = getCjmx(results, wfgsmc, cjList);
        //开始处理持仓汇总
        List<Cchz> cchzList = getCchz(results, wfgsmc, hzList);
        //开始处理资金
        List<Zj> zjList = new ArrayList<Zj>();
        if (flag_170) {
            zjList = getZj(results, USDOLLARS_170_begin, USDOLLARS_170_end, wfgsmc, "SQ170");
        } else if (flag_326) {
            zjList = getZj(results, USDOLLARS_326_begin, USDOLLARS_326_end, wfgsmc, "SQ326");
        }
        return "{'cjmx':" + cjmxList + ",'cchz':" + cchzList + ",'zj':" + zjList + "}";
    }

    public static List<Cjmx> getCjmx(String[] results, String wfgsmc, List<Bz> bzList) {
        List<Cjmx> cjmxList = new ArrayList<Cjmx>();
        for (Bz bz : bzList) {
            boolean flag = true;
            for (int i = bz.getBegin() + 1; i < bz.getEnd(); i++) {

                if (results[i].contains("The Statement shall be deemed correct and shall be conclusive and binding upon the customer if no discrepancy is reported within three (3) days upon receipt of statement. All transactions")) {
                    flag = false;
                }
                if (results[i].contains("SALESMAN NUMBER:")) {
                    flag = true;
                    i = i + 10;
                    continue;
                }
                if (flag) {
                    if (StringUtils.isNotBlank(results[i].substring(0, 7))) {
                        Cjmx cjmx = new Cjmx();
                        cjmx.setQhgsmc("ADMIS Singapore Ptd.Limited");
                        cjmx.setQhgszh(bz.getAccountnum().trim());
                        cjmx.setWsmc(wfgsmc);
                        cjmx.setCjrq(results[i].substring(0, 8).trim());
                        String pzhynr = results[i].substring(51, 81).trim();
                        cjmx.setPzhynr(pzhynr);
                        cjmx.setHydqr(pzhynr.split(" ")[0] + " " + pzhynr.split(" ")[1]);
                        cjmx.setBuy(results[i].substring(20, 34).trim());
                        cjmx.setSell(results[i].substring(35, 49).trim());
                        cjmx.setCjj(results[i].substring(84, 95).trim());
                        cjmx.setBz(results[i].substring(97, 100).trim());
                        cjmxList.add(cjmx);
                    }
                }
            }
        }
        return cjmxList;
    }

    public static List<Cchz> getCchz(String[] results, String wfgsmc, List<Bz> bzList) {
        List<Cchz> cchzList = new ArrayList<Cchz>();
        for (Bz bz : bzList) {
            boolean flag = true;
            String pzhynr = "";
            String bizhong = "";
            for (int i = bz.getBegin() + 1; i < bz.getEnd(); i++) {

                if (results[i].contains("The Statement shall be deemed correct and shall be conclusive and binding upon the customer if no discrepancy is reported within three (3) days upon receipt of statement. All transactions")) {
                    flag = false;
                }
                if (results[i].contains("SALESMAN NUMBER:")) {
                    flag = true;
                    i = i + 10;
                    continue;
                }
                if (flag && StringUtils.isNotBlank(results[i])) {
                    if (StringUtils.isNotBlank(results[i].substring(51, 79).trim())) {
                        pzhynr = results[i].substring(51, 79).trim();
                        bizhong = results[i].substring(97, 100).trim();
                    }
                    //判断字符串是否超长
                    if (results[i].length() > 122) {
                        dealCchz(cchzList, results[i].substring(0, 121), bz, wfgsmc, pzhynr, bizhong);
                        dealCchz(cchzList, results[i].substring(121, 241), bz, wfgsmc, pzhynr, bizhong);
                    } else {
                        dealCchz(cchzList, results[i], bz, wfgsmc, pzhynr, bizhong);
                    }
                }

            }
        }
        return cchzList;
    }

    public static void dealCchz(List<Cchz> cchzList, String result, Bz bz, String wfgsmc, String pzhynr, String bizhong) {
        boolean flag = true;
        if (StringUtils.isNotBlank(result.substring(51, 79).trim())) {
            pzhynr = result.substring(51, 79).trim();
            bizhong = result.substring(97, 100).trim();
        }
        if (flag && result.contains("*") && !result.substring(0, 7).contains("-")) {
            Cchz cchz = new Cchz();
            cchz.setQhgsmc("ADMIS Singapore Ptd.Limited");
            cchz.setQhgszh(bz.getAccountnum().trim());
            cchz.setWsmc(wfgsmc);
            cchz.setPzhynr(pzhynr);
            cchz.setHydqr(pzhynr.split(" ")[0] + " " + pzhynr.split(" ")[1]);
            cchz.setJys(pzhynr.split(" ")[2]);
            cchz.setBuy(result.substring(20, 34).trim().replace("*", ""));
            cchz.setSell(result.substring(35, 49).trim().replace("*", ""));
            cchz.setCjjj(result.substring(84, 95).trim());
            cchz.setBz(bizhong);
            cchzList.add(cchz);
        }
    }

    public static List<Zj> getZj(String[] results, int begin, int end, String wfgsmc, String account_num) {
        List<Zj> zjList = new ArrayList<Zj>();
        boolean flag = true;
        for (int i = begin + 1; i < end; i++) {

            if (results[i].replaceAll(" ", "").contains("TheStatementshallbedeemedcorrectandshall")) {
                flag = false;
            }
            if (results[i].replaceAll(" ", "").contains("SALESMANNUMBER:")) {
                flag = true;
                i = i + 10;
                continue;
            }
            if (flag) {
                if (StringUtils.isNotBlank(results[i]) && StringUtils.isNotBlank(results[i].substring(0, 7))) {
                    if (results[i].length() > 122) {
                        dealZj(zjList, account_num, wfgsmc, results[i].substring(0, 121));
                        dealZj(zjList, account_num, wfgsmc, results[i].substring(121, 241));
                    } else {
                        dealZj(zjList, account_num, wfgsmc, results[i]);
                    }
                }
            }
        }
        return zjList;
    }

    public static void dealZj(List<Zj> zjList, String account_num, String wfgsmc, String result) {
        Zj zj = new Zj();
        zj.setQhgsmc("ADMIS Singapore Ptd.Limited");
        zj.setQhgszh(account_num.trim());
        zj.setWsmc(wfgsmc);
        zj.setLx(result.substring(0, 35).trim());
        zj.setValue(result.substring(35, 53).trim());
        zjList.add(zj);
    }
}
