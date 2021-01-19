package com.cbnb.jiaoyishi.util.DealPdf;

import com.cbnb.jiaoyishi.model.waipan.Cchz;
import com.cbnb.jiaoyishi.model.waipan.Cjmx;
import com.cbnb.jiaoyishi.model.waipan.Zj;
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
 * @Description: 期货公司：横华国际
 * @Date: Create in 15:56 2021/1/16
 */
public class DealHhgjExcelUtil {

	public static String dealHhgj(XSSFWorkbook workbook){
		Sheet sheet = workbook.getSheetAt(0);
		Map<String,String> account = getAccountMsg(sheet);
		JSONArray result = new JSONArray();
		//开始处理成交
		List<Cjmx> cjmxList = dealCjmx(sheet,account);
		//开始处理持仓汇总
		List<Cchz> cchzList = dealCchz(sheet,account);
		List<Zj> zjList = dealZj(sheet, account);
		return "{'cjmx':"+cjmxList+",'cchz':"+cchzList+",'zj':"+zjList+"}";
	}

	public static List<Cjmx> dealCjmx(Sheet sheet,Map<String,String> account){
		List<Cjmx> cjmxList = new ArrayList<Cjmx>();
		boolean flag = false;
		for(int i =0;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "成交明细 TRADE CONFIRMATION".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				flag = true;
				i = i + 5;
				continue;
			}
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "SubTotal:".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				break;
			}
			if(flag){
				Cjmx cjmx = new Cjmx();
				cjmx.setBz(ExcelPubUtil.getValue(sheet.getRow(i).getCell(50)));
				cjmx.setPzhynr(ExcelPubUtil.getValue(sheet.getRow(i).getCell(8)));
				cjmx.setCjrq(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)) + ExcelPubUtil.getValue(sheet.getRow(i).getCell(4)));
				cjmx.setHydqr(ExcelPubUtil.getValue(sheet.getRow(i).getCell(8)).split("/")[1]);
				cjmx.setQhgsmc(account.get("qhgsmc"));
				cjmx.setQhgszh(account.get("account_num"));
				cjmx.setWsmc(account.get("wfgsmc"));
				cjmx.setBuy(ExcelPubUtil.getValue(sheet.getRow(i).getCell(34)));
				cjmx.setSell(ExcelPubUtil.getValue(sheet.getRow(i).getCell(37)));
				cjmx.setCjj(ExcelPubUtil.getValue(sheet.getRow(i).getCell(39)));
				cjmxList.add(cjmx);
			}
		}
		return cjmxList;
	}

	public static List<Cchz> dealCchz(Sheet sheet, Map<String,String> account){
		List<Cchz> cchzList = new ArrayList<Cchz>();
		boolean flag = false;
		for(int i =0;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "持仓汇总 GATHERED OPEN POSITIONS".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				flag = true;
				i = i + 3;
				continue;
			}
			if(flag && sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "Total:".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				break;
			}
			if(flag){
				Cchz cchz = new Cchz();
				cchz.setBz(ExcelPubUtil.getValue(sheet.getRow(i).getCell(56)));
				cchz.setPzhynr(ExcelPubUtil.getValue(sheet.getRow(i).getCell(3)));
				cchz.setHydqr(ExcelPubUtil.getValue(sheet.getRow(i).getCell(3)).split("/")[1]);
				cchz.setQhgsmc(account.get("qhgsmc"));
				cchz.setQhgszh(account.get("account_num"));
				cchz.setWsmc(account.get("wfgsmc"));
				cchz.setBuy(ExcelPubUtil.getValue(sheet.getRow(i).getCell(13)));
				cchz.setSell(ExcelPubUtil.getValue(sheet.getRow(i).getCell(16)));
				cchz.setCjjj(new BigDecimal(ExcelPubUtil.getValue(sheet.getRow(i).getCell(19))).setScale(4,BigDecimal.ROUND_FLOOR).toString());
				cchz.setJys(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)));
				cchz.setCcyk(ExcelPubUtil.getValue(sheet.getRow(i).getCell(35)));
				cchz.setJbbzj(ExcelPubUtil.getValue(sheet.getRow(i).getCell(48)));
				cchz.setJsj(new BigDecimal(ExcelPubUtil.getValue(sheet.getRow(i).getCell(27))).setScale(4,BigDecimal.ROUND_FLOOR).toString());
				cchzList.add(cchz);
			}
		}
		return cchzList;
	}

	public static List<Zj> dealZj(Sheet sheet, Map<String,String> account){
		List<Zj> zjList = new ArrayList<Zj>();
		boolean flag = false;
		for(int i =0;i<1000;i++){
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "资金状况 DAILY ACCOUNT SUMMARY".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				flag = true;
				i = i + 1;
				continue;
			}
			if(sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && "重要启事 IMPORTANT NOTICE".equals(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)))){
				break;
			}
			if(flag){
				Zj zj = new Zj();
				zj.setQhgsmc(account.get("qhgsmc"));
				zj.setQhgszh(account.get("account_num"));
				zj.setWsmc(account.get("wfgsmc"));
				zj.setLx(ExcelPubUtil.getValue(sheet.getRow(i).getCell(0)));
				zj.setValue(ExcelPubUtil.getValue(sheet.getRow(i).getCell(12)));
				zjList.add(zj);
			}
		}
		return zjList;
	}

	public static Map<String,String> getAccountMsg(Sheet sheet){
		Map<String,String> map = new HashMap<String, String>();
		map.put("qhgsmc",ExcelPubUtil.getValue(sheet.getRow(1).getCell(32)).split("\n")[0]);
		map.put("wfgsmc",ExcelPubUtil.getValue(sheet.getRow(5).getCell(9)).replaceAll("'","’"));
		map.put("account_num",ExcelPubUtil.getValue(sheet.getRow(3).getCell(9)));
		return map;
	}
}
