package com.yesHealth.web.modules.util;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateExcelUtil {
	public static void genDailySeedingReport(ReportInfo reportInfo) {

	}

	public static byte[] genDailyWateringReport(ReportInfo reportInfo) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(reportInfo.getSheetName());
		List<Object> dataList = reportInfo.getDataList();
		return null;
	}

	public static void genDailyHeadOutReport() {

	}
}
