package com.cbnb.jiaoyishi.util.DealPdf;

import com.cbnb.jiaoyishi.model.waipan.Cchz;
import com.cbnb.jiaoyishi.model.waipan.Cjmx;
import com.cbnb.jiaoyishi.model.waipan.Zj;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: hcf
 * @Description: 期货公司：南华
 * @Date: Create in 13:09 2021/1/16
 */
public class DealNanhuaExcelUtil {
	public static String dealNanhua(XSSFWorkbook workbook){
		Sheet zj = workbook.getSheet("Financial Summary");
		Sheet cj = workbook.getSheet("Trade Confo");
		Sheet cc = workbook.getSheet("Open Position");
		Map<String,String> account = getAccountMsg(zj);
		JSONArray result = new JSONArray();
		//开始处理成交
		List<Cjmx> cjmxList = dealCj(cj,account);
		//开始处理持仓汇总
		List<Cchz> cchzList = dealCc(cc,account);
		//开始处理资金
		List<Zj> zjList = dealZj(zj,account);
		return "{'cjmx':"+cjmxList+",'cchz':"+cchzList+",'zj':"+zjList+"}";
	}

	public static Map<String,String> getAccountMsg(Sheet sheet){
		Map<String,String> map = new HashMap<String, String>();
		map.put("qhgsmc",ExcelPubUtil.getValue(sheet.getRow(0).getCell(2)));
		map.put("wfgsmc",ExcelPubUtil.getValue(sheet.getRow(2).getCell(0)).replaceAll("'","’"));
		map.put("account_num",ExcelPubUtil.getValue(sheet.getRow(2).getCell(3)));
		return map;
	}

	public static List<Cjmx> dealCj(Sheet sheet,Map<String,String> map){
		List<Cjmx> cjmxList = new ArrayList<Cjmx>();
		boolean flag = true;
		for(int i = 4;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "SUMMARY".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				flag = false;
				break;
			}
			if(flag && sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && StringUtils.isNotBlank(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				Cjmx cjmx = new Cjmx();
				cjmx.setQhgsmc(map.get("qhgsmc"));
				cjmx.setQhgszh(map.get("account_num"));
				cjmx.setWsmc(map.get("wfgsmc"));
				cjmx.setCjrq(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)));
				cjmx.setBuy(ExcelPubUtil.getValue(sheet.getRow(i).getCell(1)));
				cjmx.setSell(ExcelPubUtil.getValue(sheet.getRow(i).getCell(2)));
				String hynr = ExcelPubUtil.getValue(sheet.getRow(i).getCell(3));
				cjmx.setHydqr(hynr.split(" ")[0] + " " + hynr.split(" ")[1]);
				cjmx.setPzhynr(hynr);
				cjmx.setCjj(new BigDecimal(ExcelPubUtil.getValue(sheet.getRow(i).getCell(4))).setScale(4,BigDecimal.ROUND_FLOOR).toString());
				cjmx.setBz(ExcelPubUtil.getValue(sheet.getRow(i).getCell(5)));
				cjmxList.add(cjmx);
			}
		}
		return cjmxList;
	}

	public static List<Zj> dealZj(Sheet sheet,Map<String,String> map){
		List<Zj> zjList = new ArrayList<Zj>();
		for(int i = 13;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)).contains("The information provided in this excel")){
				break;
			}
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && StringUtils.isNotBlank(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				Zj zj = new Zj();
				zj.setQhgsmc(map.get("qhgsmc"));
				zj.setWsmc(map.get("wfgsmc"));
				zj.setQhgszh(map.get("account_num"));
				zj.setLx(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)).trim());
				zj.setValue(new BigDecimal(ExcelPubUtil.getValue(sheet.getRow(i).getCell(1)).trim()).setScale(4,BigDecimal.ROUND_FLOOR).toString());
				zjList.add(zj);
			}
		}
		return zjList;
	}

	public static List<Cchz> dealCc(Sheet sheet,Map<String,String> map){
		List<Cchz> cchzList = new ArrayList<Cchz>();
		boolean flag = false;
		String hynr = "";
		String bizhong = "";
		for(int i = 3;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "SUMMARY".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				break;
			}
			if(sheet.getRow(i) == null || sheet.getRow(i).getCell(0) == null || StringUtils.isEmpty(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				flag = true;
			}else{
				flag = false;
				if(sheet.getRow(i) != null && sheet.getRow(i).getCell(3) != null) {
					hynr = ExcelPubUtil.getValue(sheet.getRow(i).getCell(3));
					bizhong = ExcelPubUtil.getValue(sheet.getRow(i).getCell(6));
				}
			}
			if(flag && sheet.getRow(i) != null && sheet.getRow(i).getCell(1) != null && StringUtils.isNotBlank(ExcelPubUtil.getValue(sheet.getRow(i).getCell(1)))){
				Cchz cchz = new Cchz();
				cchz.setQhgsmc(map.get("qhgsmc"));
				cchz.setQhgszh(map.get("account_num"));
				cchz.setWsmc(map.get("wfgsmc"));
				cchz.setBuy(ExcelPubUtil.getValue(sheet.getRow(i).getCell(1)));
				cchz.setSell(ExcelPubUtil.getValue(sheet.getRow(i).getCell(2)));
				cchz.setJys(hynr.split(" ")[2]);
				cchz.setHydqr(hynr.split(" ")[0] + " " + hynr.split(" ")[1]);
				cchz.setBz(bizhong);
				cchz.setCjjj(new BigDecimal(ExcelPubUtil.getValue(sheet.getRow(i+1).getCell(4))).setScale(4,BigDecimal.ROUND_FLOOR).toString());
				cchzList.add(cchz);
			}
		}
		return cchzList;
	}
}
