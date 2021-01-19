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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: hcf
 * @Description: 期货公司：ED & F MAN CAPITAL MARKETS LIMITED
 * @Date: Create in 10:22 2020/12/23
 */
public class DealEdfPdfUtil {

	/**
	 * EDF
	 * @param str
	 * @return
	 */
	public static String dealEdf(String str,String wfgsmc,String account_num){
		JSONObject jsonObject = new JSONObject();
		String[] results = str.split("\r");
		Map<String,List<Bz>> map = new HashMap<String, List<Bz>>();
		int USDOLLARS_begin = 0;
		int USDOLLARS_end = 0;
		boolean cj = false;
		boolean hz = false;
		List<Bz> cjList = new ArrayList<Bz>();
		List<Bz> hzList = new ArrayList<Bz>();
		for(int i = 0;i < results.length;i++){
			if(results[i].replaceAll(" ","").contains("**PURCHASE&SALE**")){//成交开始
				cj = true;
				Bz bz = new Bz();
				bz.setAccountnum(results[i-24].replace("ACCOUNT NUMBER:","").trim());
				bz.setBegin(i + 2);
				cjList.add(bz);
			}
			if(cj && results[i].replaceAll(" ","").contains("**OPENPOSITIONS**")){//成交结束
				cjList.get(cjList.size() - 1).setEnd(i - 1);
				cj = false;
			}
			if(results[i].replaceAll(" ","").contains("**SUMMARYOPENPOSITIONS**")){//资金相关开始
				Bz bz = new Bz();
				bz.setBegin(i + 2);
				hzList.add(bz);
				bz.setAccountnum(cjList.get(hzList.size() - 1).getAccountnum());
			}
			if(results[i].replaceAll(" ","").contains("*U.S.DOLLARS*")){//资金相关结束
				hzList.get(hzList.size() - 1).setEnd(i - 1);
				USDOLLARS_begin = i;
			}
			if(results[i].replaceAll(" ","").contains("MARGINDEFICIT/EXCESS")){
				USDOLLARS_end = i;
			}
		}
		JSONArray result = new JSONArray();
		//开始处理成交
		List<Cjmx> cjmxList = getCjmx(results,wfgsmc,cjList);
		//开始处理持仓汇总
		List<Cchz> cchzList = getCchz(results,wfgsmc,hzList);
		//开始处理资金
		List<Zj> zjList = getZj(results,USDOLLARS_begin,USDOLLARS_end,wfgsmc,account_num);
		return "{'cjmx':"+cjmxList+",'cchz':"+cchzList+",'zj':"+zjList+"}";
	}
	public static List<Zj> getZj(String[] results,int begin,int end,String wfgsmc,String account_num){
		List<Zj> zjList = new ArrayList<Zj>();
		boolean flag = true;
		for (int i = begin + 1; i < end; i++) {

			if (results[i].contains("Registered Office: Authorised and Regulated by the Financial Conduct Authority Telephone: +44 (0) 20 3580 7171")) {
				flag = false;
			}
			if (results[i].contains("PAGE")) {
				flag = true;
				i = i + 3;
				continue;
			}
			if (flag) {
				if (StringUtils.isNotBlank(results[i].substring(0, 7))) {
					Zj zj = new Zj();
					zj.setQhgsmc("ED & F MAN CAPITAL MARKETS LIMITED");
					zj.setQhgszh(account_num);
					zj.setWsmc(wfgsmc);
					zj.setLx(results[i].substring(0,33).trim());
					zj.setValue(results[i].substring(33,55).trim());
					zjList.add(zj);
				}
			}
		}
		return zjList;
	}

	public static List<Cchz> getCchz(String[] results,String wfgsmc,List<Bz> bzList){
		List<Cchz> cchzList = new ArrayList<Cchz>();
		for(Bz bz : bzList) {
			boolean flag = true;
			for (int i = bz.getBegin() + 1; i < bz.getEnd(); i++) {

				if (results[i].contains("Registered Office: Authorised and Regulated by the Financial Conduct Authority Telephone: +44 (0) 20 3580 7171")) {
					flag = false;
				}
				if (results[i].contains("PAGE")) {
					flag = true;
					i = i + 3;
					continue;
				}
				if (flag) {
					Cchz cchz = new Cchz();
					cchz.setQhgsmc("ED & F MAN CAPITAL MARKETS LIMITED");
					cchz.setQhgszh(bz.getAccountnum());
					cchz.setWsmc(wfgsmc);
					String pzhynr = results[i].substring(50, 80).trim();
					cchz.setPzhynr(pzhynr);
					cchz.setHydqr(pzhynr.split(" ")[0] + " "+pzhynr.split(" ")[1] + " "+pzhynr.split(" ")[2]);
					cchz.setJys(pzhynr.split(" ")[3]);
					cchz.setBuy(results[i].substring(21, 36).trim());
					cchz.setSell(results[i].substring(37, 51).trim());
					cchz.setCjjj(results[i].substring(84, 95).trim());
					cchz.setBz(results[i].substring(97, 99).trim());
					cchzList.add(cchz);
				}
			}
		}
		return cchzList;
	}
	public static List<Cjmx> getCjmx(String[] results,String wfgsmc,List<Bz> bzList){
		List<Cjmx> cjmxList = new ArrayList<Cjmx>();
		for(Bz bz : bzList){
			boolean flag = true;
			for (int i = bz.getBegin() + 1; i < bz.getEnd(); i++) {

				if (results[i].contains("Registered Office: Authorised and Regulated by the Financial Conduct Authority Telephone: +44 (0) 20 3580 7171")) {
					flag = false;
				}
				if (results[i].contains("PAGE")) {
					flag = true;
					i = i + 3;
					continue;
				}
				if (flag) {
					if (StringUtils.isNotBlank(results[i].substring(0, 7))) {
						Cjmx cjmx = new Cjmx();
						cjmx.setQhgsmc("ED & F MAN CAPITAL MARKETS LIMITED");
						cjmx.setQhgszh(bz.getAccountnum());
						cjmx.setWsmc(wfgsmc);
						cjmx.setCjrq(results[i].substring(0,7).trim());
						String pzhynr = results[i].substring(50,80).trim();
						cjmx.setPzhynr(pzhynr);
						cjmx.setHydqr(pzhynr.split(" ")[0] + " "+pzhynr.split(" ")[1] + " "+pzhynr.split(" ")[2]);
						cjmx.setBuy(results[i].substring(19,34).trim());
						cjmx.setSell(results[i].substring(35,49).trim());
						cjmx.setCjj(results[i].substring(84,95).trim());
						cjmx.setBz(results[i].substring(97,99).trim());
						cjmxList.add(cjmx);
					}
				}
			}
		}
		return cjmxList;
	}
	public static void dealEDF(String[] results,int begin,int end,JSONArray result, String type,String wfgsmc,String account_num){
		JSONObject json = new JSONObject();
		json.put("type",type);
		if("usdollars".equals(type)){
			JSONArray array = new JSONArray();
			List<SubstrBean> list = new ArrayList<SubstrBean>();
			JSONArray titles_array = new JSONArray();
			SubstrBean bean = new SubstrBean();
			bean.setTitle("type");
			bean.setBegin(0);
			bean.setEnd(results[begin].indexOf("*U.S. DOLLARS*"));
			list.add(bean);
			JSONObject titles_json = new JSONObject();
			titles_json = new JSONObject();
			titles_json.put("field", "qhgsmc");
			titles_json.put("title", "期货公司名称");
			titles_json.put("sortable", true);
			titles_array.put(titles_json);
			titles_json = new JSONObject();
			titles_json.put("field", "wfgsmc");
			titles_json.put("title", "我方公司名称");
			titles_json.put("sortable", true);
			titles_array.put(titles_json);
			titles_json = new JSONObject();
			titles_json.put("field", "qhgszh");
			titles_json.put("title", "期货公司账号");
			titles_json.put("sortable", true);
			titles_array.put(titles_json);
			titles_json= new JSONObject();
			titles_json.put("field", "type");
			titles_json.put("title", "type");
			titles_json.put("sortable", true);
			titles_array.put(titles_json);
			bean = new SubstrBean();
			bean.setTitle("usdollars");
			bean.setBegin(results[begin].indexOf("*U.S. DOLLARS*"));
			bean.setEnd(results[begin].length());
			list.add(bean);
			titles_json = new JSONObject();
			titles_json.put("field", "usdollars");
			titles_json.put("title", "usdollars");
			titles_json.put("sortable", true);
			titles_array.put(titles_json);
			json.put("titles", titles_array);
			boolean flag = true;
			for (int i = begin + 1; i < end; i++) {
				if (results[i].contains("Registered Office: Authorised and Regulated by the Financial Conduct Authority Telephone: +44 (0) 20 3580 7171")) {
					flag = false;
				}
				if (results[i].contains("PAGE")) {
					flag = true;
					i = i + 3;
					continue;
				}
				if (flag) {
					if (StringUtils.isNotBlank(results[i].substring(list.get(0).getBegin(), list.get(0).getEnd()))) {
						JSONObject openpositions = new JSONObject();
						for (SubstrBean bean1 : list) {
							openpositions.put("wfgsmc",wfgsmc);
							openpositions.put("qhgsmc","EDF");
							openpositions.put("qhgszh",account_num);
							openpositions.put(bean1.getTitle(), results[i].substring(bean1.getBegin(), bean1.getEnd()).trim());
						}
						array.put(openpositions);
					}
				}
			}
			json.put("data", array);
			result.put(json);
		}else {
			JSONArray array = new JSONArray();
			List<SubstrBean> list = new ArrayList<SubstrBean>();
			String title = results[begin - 1];
			String[] titles = results[begin].split(" ");
			JSONArray titles_array = new JSONArray();
			int _begin = 2;
			for (String tit : titles) {
				if (!"\n".equals(tit) && StringUtils.isNotBlank(tit)) {
					SubstrBean bean = new SubstrBean();
					String title_str = title.substring(_begin, _begin + tit.length()).trim();
					bean.setTitle(title_str);
					bean.setBegin(_begin);
					bean.setEnd(_begin + tit.length());
					list.add(bean);
					JSONObject titles_json = new JSONObject();
					titles_json.put("field", title_str);
					titles_json.put("title", title_str);
					titles_json.put("sortable", true);
					titles_array.put(titles_json);
					_begin = _begin + tit.length() + 1;
				}
				if (!"\n".equals(tit) && StringUtils.isBlank(tit)) {
					_begin = _begin + tit.length() + 1;
				}
			}
			json.put("titles", titles_array);
			boolean flag = true;
			for (int i = begin + 1; i < end; i++) {
				if (results[i].contains("Registered Office: Authorised and Regulated by the Financial Conduct Authority Telephone: +44 (0) 20 3580 7171")) {
					flag = false;
				}
				if (results[i].contains("PAGE")) {
					flag = true;
					i = i + 3;
					continue;
				}
				if (flag) {
					if ("purchasesale".equals(type) || "openpositions".equals(type)) {
						if (StringUtils.isNotBlank(results[i].substring(list.get(0).getBegin(), list.get(0).getEnd()))) {
							JSONObject openpositions = new JSONObject();
							for (SubstrBean bean : list) {
								openpositions.put(bean.getTitle(), results[i].substring(bean.getBegin(), bean.getEnd()).trim());
							}
							array.put(openpositions);
						}
					} else {
						JSONObject openpositions = new JSONObject();
						for (SubstrBean bean : list) {
							openpositions.put(bean.getTitle(), results[i].substring(bean.getBegin(), bean.getEnd()).trim());
						}
						array.put(openpositions);
					}
				}
			}
			json.put("data", array);
			result.put(json);
		}
	}

	public static void main(String [] args) throws Exception{
		String title = "\n" +
				"  TRADE   SETTL          BUY            SELL      CONTRACT DESCRIPTION           EX TRADE PRICE CC    DEBIT/CREDIT                ";
		System.out.println(title.substring(2, 9).trim());
	}
}
