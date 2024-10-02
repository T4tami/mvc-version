package com.yesHealth.web.modules.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.ValidationException;

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

import com.yesHealth.web.modules.planning.domain.service.impl.RowInfo;

public class GenerateExcelUtil {
	public static void genDailySeedingReport(ReportInfo reportInfo) {

	}

	public static byte[] genDailyReport(ReportInfo reportInfo) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(reportInfo.getSheetName());
		List<RowInfo> rowList = reportInfo.getRowList();
		List<ExcelCell> cellList = reportInfo.getCellList();
		for (ExcelCell excelCell : cellList) {
			if (excelCell instanceof MergeCell) {
				MergeCell mergeCell = (MergeCell) excelCell;
				String startCell = mergeCell.getStartCell();
				String endCell = mergeCell.getEndCell();
				int firstRow = convertToRowIndex(mergeCell.getStartCell());
				int firstCol = convertToColIndex(mergeCell.getStartCell());
				int endRow = convertToRowIndex(mergeCell.getEndCell());
				int endCol = convertToColIndex(mergeCell.getEndCell());

				CellRangeAddress range = new CellRangeAddress(firstRow, endRow, firstCol, endCol);
				sheet.addMergedRegion(range);

				Row dataRow = sheet.createRow(firstRow);
				Cell cell = dataRow.createCell(mergeCell.getStartColIndex());
				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setFontHeightInPoints(mergeCell.getCellStyleInfo().getFontSize());
				font.setFontName(mergeCell.getCellStyleInfo().getFontName());
				style.setFont(font);
				style.setAlignment(mergeCell.getCellStyleInfo().getHorizontalAlignment());
				style.setVerticalAlignment(mergeCell.getCellStyleInfo().getVerticalAlignment());
				cell.setCellStyle(style);
			} else if (excelCell instanceof CellInfo) {
				CellInfo cellInfo = (CellInfo) rowData;
				Cell cell = dataRow.createCell(cellInfo.getColIndex());
				cell.setCellValue(cellInfo.getValue());

				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setFontHeightInPoints(cellInfo.getCellStyleInfo().getFontSize());
				font.setFontName(cellInfo.getCellStyleInfo().getFontName());
				style.setFont(font);
				style.setAlignment(cellInfo.getCellStyleInfo().getHorizontalAlignment());
				style.setVerticalAlignment(cellInfo.getCellStyleInfo().getVerticalAlignment());
				style.setWrapText(cellInfo.getCellStyleInfo().isWrapText());
				if (cellInfo.getCellStyleInfo().getBorderTop() != null) {
					style.setBorderTop(cellInfo.getCellStyleInfo().getBorderTop());
				}
				if (cellInfo.getCellStyleInfo().getBorderBottom() != null) {
					style.setBorderBottom(cellInfo.getCellStyleInfo().getBorderBottom());
				}
				if (cellInfo.getCellStyleInfo().getBorderLeft() != null) {
					style.setBorderLeft(cellInfo.getCellStyleInfo().getBorderLeft());
				}
				if (cellInfo.getCellStyleInfo().getBorderRight() != null) {
					style.setBorderRight(cellInfo.getCellStyleInfo().getBorderRight());
				}
				cell.setCellStyle(style);
			}
			if (reportInfo.getDataRowCount() < reportInfo.getMinRowCount()) {
				int dataRowId = reportInfo.getDataRowCount();
				for (int i = rowList.size(); i <= reportInfo.getMinRowCount(); i++) {
					Row blankRow = sheet.createRow(i - 1);
					dataRowId++;
					for (int j = 1; j <= reportInfo.getColCount(); j++) {
						Cell blankCell = blankRow.createCell(j);
						String value = j == 1 ? String.format("%03d", dataRowId) : "";
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
//		for (RowInfo rowInfo : rowList) {
//			Row dataRow = sheet.createRow(rowInfo.getRowIndex());
//			List<? extends ExcelCell> rowDataList = rowInfo.getRowData();
//			for (ExcelCell rowData : rowDataList) {
//				if (rowData instanceof MergeCell) {
//					MergeCell mergeCell = (MergeCell) rowData;
//					CellRangeAddress range = new CellRangeAddress(mergeCell.getStartRowIndex(),
//							mergeCell.getEndRowIndex(), mergeCell.getStartColIndex(), mergeCell.getEndColIndex());
//					sheet.addMergedRegion(range);
//					Cell cell = dataRow.createCell(mergeCell.getStartColIndex());
//					cell.setCellValue(mergeCell.getValue().toString());
//
//					CellStyle style = workbook.createCellStyle();
//					Font font = workbook.createFont();
//					font.setFontHeightInPoints(mergeCell.getCellStyleInfo().getFontSize());
//					font.setFontName(mergeCell.getCellStyleInfo().getFontName());
//					style.setFont(font);
//					style.setAlignment(mergeCell.getCellStyleInfo().getHorizontalAlignment());
//					style.setVerticalAlignment(mergeCell.getCellStyleInfo().getVerticalAlignment());
//					cell.setCellStyle(style);
//				} else if (rowData instanceof CellInfo) {
//					CellInfo cellInfo = (CellInfo) rowData;
//					Cell cell = dataRow.createCell(cellInfo.getColIndex());
//					cell.setCellValue(cellInfo.getValue());
//
//					CellStyle style = workbook.createCellStyle();
//					Font font = workbook.createFont();
//					font.setFontHeightInPoints(cellInfo.getCellStyleInfo().getFontSize());
//					font.setFontName(cellInfo.getCellStyleInfo().getFontName());
//					style.setFont(font);
//					style.setAlignment(cellInfo.getCellStyleInfo().getHorizontalAlignment());
//					style.setVerticalAlignment(cellInfo.getCellStyleInfo().getVerticalAlignment());
//					style.setWrapText(cellInfo.getCellStyleInfo().isWrapText());
//					if (cellInfo.getCellStyleInfo().getBorderTop() != null) {
//						style.setBorderTop(cellInfo.getCellStyleInfo().getBorderTop());
//					}
//					if (cellInfo.getCellStyleInfo().getBorderBottom() != null) {
//						style.setBorderBottom(cellInfo.getCellStyleInfo().getBorderBottom());
//					}
//					if (cellInfo.getCellStyleInfo().getBorderLeft() != null) {
//						style.setBorderLeft(cellInfo.getCellStyleInfo().getBorderLeft());
//					}
//					if (cellInfo.getCellStyleInfo().getBorderRight() != null) {
//						style.setBorderRight(cellInfo.getCellStyleInfo().getBorderRight());
//					}
//					cell.setCellStyle(style);
//				}
//			}
//			if (reportInfo.getDataRowCount() < reportInfo.getMinRowCount()) {
//				int dataRowId = reportInfo.getDataRowCount();
//				for (int i = rowList.size(); i <= reportInfo.getMinRowCount(); i++) {
//					Row blankRow = sheet.createRow(i - 1);
//					dataRowId++;
//					for (int j = 1; j <= reportInfo.getColCount(); j++) {
//						Cell blankCell = blankRow.createCell(j);
//						String value = j == 1 ? String.format("%03d", dataRowId) : "";
//						blankCell.setCellValue(value);
//
//						CellStyle blankStyle = workbook.createCellStyle();
//						blankStyle.setAlignment(HorizontalAlignment.CENTER);
//						blankStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//						setBorders(blankStyle);
//						blankCell.setCellStyle(blankStyle);
//
//					}
//				}
//			}
//		}
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

	private static int convertToRowIndex(String cell) {
		return Integer.parseInt(cell.replaceAll("[^0-9]", ""));
	}

	private static int convertToColIndex(String cell) {
		if (!cell.matches("[a-zA-z]+[0-9]+")) {
			throw new ValidationException("不合法的Excel欄位");
		}
		String columnPart = cell.replaceAll("[0-9]", "").toUpperCase(); // 提取字母部分
		int colIndex = 0;

		for (int i = 0; i < columnPart.length(); i++) {
			colIndex *= 26; // 每增加一位，乘以 26
			colIndex += columnPart.charAt(i) - 'A' + 1; // 計算當前字母的值
		}
		return colIndex;
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
