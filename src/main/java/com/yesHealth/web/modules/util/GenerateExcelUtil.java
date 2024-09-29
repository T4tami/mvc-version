package com.yesHealth.web.modules.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.yesHealth.web.modules.planning.domain.service.impl.CellInfo;
import com.yesHealth.web.modules.planning.domain.service.impl.RowInfo;

public class GenerateExcelUtil {
	public static void genDailySeedingReport(ReportInfo reportInfo) {

	}

	public static byte[] genDailyReport(ReportInfo reportInfo) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(reportInfo.getSheetName());
		List<RowInfo> rowList = reportInfo.getRowList();

		for (RowInfo rowInfo : rowList) {
			Row dataRow = sheet.createRow(rowInfo.getRowIndex());
			List<? extends ExcelCell> rowDataList = rowInfo.getRowData();
			for (ExcelCell rowData : rowDataList) {
				if (rowData instanceof MergeCell) {
					MergeCell mergeCell = (MergeCell) rowData;
					CellRangeAddress range = new CellRangeAddress(mergeCell.getStartRowIndex(),
							mergeCell.getEndRowIndex(), mergeCell.getStartColIndex(), mergeCell.getEndColIndex());
					sheet.addMergedRegion(range);
					Cell cell = dataRow.createCell(mergeCell.getStartColIndex());
					cell.setCellValue(mergeCell.getValue().toString());

					CellStyle style = workbook.createCellStyle();
					Font font = workbook.createFont();
					font.setFontHeightInPoints(mergeCell.getCellInfoStyle().getFontSize());
					font.setFontName(mergeCell.getCellInfoStyle().getFontName());
					style.setFont(font);
					style.setAlignment(mergeCell.getCellInfoStyle().getHorizontalAlignment());
					style.setVerticalAlignment(mergeCell.getCellInfoStyle().getVerticalAlignment());
					cell.setCellStyle(style);
				} else if (rowData instanceof CellInfo) {
					CellInfo cellInfo = (CellInfo) rowData;
					Cell cell = dataRow.createCell(cellInfo.getColIndex());
					cell.setCellValue(cellInfo.getValue());

					CellStyle style = workbook.createCellStyle();
					Font font = workbook.createFont();
					font.setFontHeightInPoints(cellInfo.getCellInfoStyle().getFontSize());
					font.setFontName(cellInfo.getCellInfoStyle().getFontName());
					style.setFont(font);
					style.setAlignment(cellInfo.getCellInfoStyle().getHorizontalAlignment());
					style.setVerticalAlignment(cellInfo.getCellInfoStyle().getVerticalAlignment());
					style.setWrapText(cellInfo.getCellInfoStyle().isWrapText());
					if (cellInfo.getCellInfoStyle().getBorderTop() != null) {
						style.setBorderTop(cellInfo.getCellInfoStyle().getBorderTop());
					}
					if (cellInfo.getCellInfoStyle().getBorderBottom() != null) {
						style.setBorderBottom(cellInfo.getCellInfoStyle().getBorderBottom());
					}
					if (cellInfo.getCellInfoStyle().getBorderLeft() != null) {
						style.setBorderLeft(cellInfo.getCellInfoStyle().getBorderLeft());
					}
					if (cellInfo.getCellInfoStyle().getBorderRight() != null) {
						style.setBorderRight(cellInfo.getCellInfoStyle().getBorderRight());
					}
					cell.setCellStyle(style);
				}
			}
			if (reportInfo.getDataRowCount() < reportInfo.getMinRowCount()) {
				for (int i = rowList.size(); i <= reportInfo.getMinRowCount(); i++) {
					Row blankRow = sheet.createRow(i - 1);
					for (int j = 1; j <= reportInfo.getColCount(); j++) {
						Cell blankCell = blankRow.createCell(j);
						String value = j == 1 ? String.format("%03d", reportInfo.getDataRowCount() + 1) : "";
						blankCell.setCellValue(value);

						CellStyle blankStyle = workbook.createCellStyle();
						blankStyle.setAlignment(HorizontalAlignment.CENTER);
						blankStyle.setVerticalAlignment(VerticalAlignment.CENTER);
						setBorders(blankStyle);
						blankCell.setCellStyle(blankStyle);

					}
				}
			}
		}
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			workbook.write(outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setBorders(CellStyle style) {
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
	}

	public static void genDailyHeadOutReport() {

	}
}
