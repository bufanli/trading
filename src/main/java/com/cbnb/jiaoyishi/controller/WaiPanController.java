package com.cbnb.jiaoyishi.controller;

import com.cbnb.jiaoyishi.model.RestResponse;
import com.cbnb.jiaoyishi.util.DealPdf.*;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: hcf
 * @Description:
 * @Date: Create in 13:36 2020/12/22
 */
@Controller
@RequestMapping("/waipan")
public class WaiPanController {
	@RequestMapping(value = "/upload",method = RequestMethod.POST)
	public @ResponseBody
	RestResponse<String> uploadSl(@RequestParam("file") CommonsMultipartFile[] file){
		ModelAndView mv = new ModelAndView();
		JSONArray jsonArray = new JSONArray();
		if(file != null && file.length > 0) {
			for (int i = 0; i < file.length; i++) {
				try {
					if(file[i].getOriginalFilename().contains(".pdf")) {
						InputStream inp = file[i].getInputStream();
						String fileTempPath = System.getProperty("user.dir") + File.separator + "tempFile" + File.separator + new Date().getTime() + ".pdf";
						File fileNew = new File(fileTempPath);
						File fileParent = fileNew.getParentFile();
						if (!fileParent.exists()) {
							fileParent.mkdirs();// 能创建多级目录
						}
						if (!fileNew.exists()) {
							fileNew.createNewFile();//有路径才能创建文件
						}
						inputStreamToFile(inp, fileNew);
						// 新建一个PDF解析器对象
						PDFParser parser = new PDFParser(new RandomAccessFile(fileNew, "rw"));
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
						System.out.println(result);
						String firstRow = result.split("\r")[0];
						if (firstRow.startsWith("ED & F MAN CAPITAL MARKETS LIMITED")) {//EDF
							//EDF
							String account_num = result.split("\r")[1].replace("ACCOUNT NUMBER:", "").trim();
							String wfgsmc = result.split("\r")[15].trim();
							String json = DealEdfPdfUtil.dealEdf(result, wfgsmc, account_num);
							return new RestResponse<String>(RestResponse.SUCCESS_CODE, "操作成功", json);
						} else if ("AsubsidiaryofADM".equals(result.split("\r")[5].trim().replaceAll(" ", ""))) {//ADMIS
							String json = DealAdmisPdfUtil.dealAdmis(result);
							return new RestResponse<String>(RestResponse.SUCCESS_CODE, "操作成功", json);
						}
					}else if(file[i].getOriginalFilename().contains(".xlsx")){//excel
						InputStream inp = file[i].getInputStream();
						String fileTempPath = System.getProperty("user.dir") + File.separator + "tempFile" + File.separator + new Date().getTime() + ".xlsx";
						File fileNew = new File(fileTempPath);
						File fileParent = fileNew.getParentFile();
						if (!fileParent.exists()) {
							fileParent.mkdirs();// 能创建多级目录
						}
						if (!fileNew.exists()) {
							fileNew.createNewFile();//有路径才能创建文件
						}
						inputStreamToFile(inp, fileNew);
						FileInputStream fileInputStream = new FileInputStream(fileNew);
						XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
						Sheet financialSummary = workbook.getSheet("Financial Summary");//资金
						if(financialSummary != null && financialSummary.getRow(0) != null && financialSummary.getRow(0).getCell(2) != null && "NANHUA UK".equals(ExcelPubUtil.getValue(financialSummary.getRow(0).getCell(2)))){//南华
							String json = DealNanhuaExcelUtil.dealNanhua(workbook);
							return new RestResponse<String>(RestResponse.SUCCESS_CODE, "操作成功", json);
						}else if(workbook.getSheetAt(0) != null && workbook.getSheetAt(0).getRow(1) != null && ExcelPubUtil.getValue(workbook.getSheetAt(0).getRow(1).getCell(32)).contains("横华国际期货有限公司")){
							String json = DealHhgjExcelUtil.dealHhgj(workbook);
							return new RestResponse<String>(RestResponse.SUCCESS_CODE, "操作成功", json);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return new RestResponse<String>(RestResponse.FAILURE_CODE,"操作失败",e.getMessage());
				}
			}
			return null;
		}else{
			return new RestResponse<String>(RestResponse.FAILURE_CODE,"操作失败，请先选择文件",null);
		}
	}



	private static void inputStreamToFile(InputStream ins,File file) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = ins.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			throw new RuntimeException("调用inputStreamToFile异常" +e.getMessage());
		}finally {
			try {
				if (os != null) {
					os.close();
				}
				if (ins != null) {
					ins.close();
				}
			} catch (Exception e) {
				throw new RuntimeException("调用inputStreamToFile异常" +e.getMessage());
			}
		}
	}

	public static void main(String [] args) throws Exception{
		System.out.println(new Date().getTime());
		File file = new File("F:\\交易部\\期货公司报表解析\\整理\\ADMIS.pdf");
		try {
			// 新建一个PDF解析器对象
			PDFParser parser = new PDFParser(new RandomAccessFile(file,"rw"));
			// 对PDF文件进行解析
			parser.parse();
			// 获取解析后得到的PDF文档对象
			PDDocument pdfdocument = parser.getPDDocument();
			System.out.println(pdfdocument.getPages().getCount());
			// 新建一个PDF文本剥离器
			PDFTextStripper stripper = new PDFTextStripper();
			stripper .setSortByPosition(true); //sort设置为true 则按照行进行读取，默认是false
			// 从PDF文档对象中剥离文本
			String result = stripper.getText(pdfdocument);
			FileWriter fileWriter = new FileWriter(new File("pdf.txt"));
			fileWriter.write(result);
			fileWriter.flush();
			fileWriter.close();
			System.out.println("PDF文件的文本内容如下：");
			System.out.println(result);

		} catch (Exception e) {
			System.out.println("读取PDF文件" + file.getAbsolutePath() + "生失败！" + e);
			e.printStackTrace();
		}
	}
}
