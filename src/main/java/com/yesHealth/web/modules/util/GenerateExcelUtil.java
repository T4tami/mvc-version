package com.yesHealth.web.modules.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.yesHealth.web.modules.util.model.CellInfo;
import com.yesHealth.web.modules.util.model.CellStyleInfo;
import com.yesHealth.web.modules.util.model.ExcelCell;
import com.yesHealth.web.modules.util.model.MergeCell;
import com.yesHealth.web.modules.util.model.ReportInfo;

public class GenerateExcelUtil {

	private static final int MinDataRowCount = 18;

	public static void genDailySeedingReport(ReportInfo reportInfo) {

	}

	public static byte[] genDailyReport(ReportInfo reportInfo) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(reportInfo.getSheetName());
		List<ExcelCell> ExcelcellList = reportInfo.getCellList();
		List<CellInfo> cellInfoList = new ArrayList<>();
		for (ExcelCell excelCell : ExcelcellList) {
			if (excelCell instanceof MergeCell) {
				MergeCell mergeCell = (MergeCell) excelCell;
				int firstRow = convertToRowIndex(mergeCell.getStartCell());
				int firstCol = convertToColIndex(mergeCell.getStartCell(), Boolean.TRUE);
				int endRow = convertToRowIndex(mergeCell.getEndCell());
				int endCol = convertToColIndex(mergeCell.getEndCell(), Boolean.TRUE);

				CellRangeAddress range = new CellRangeAddress(firstRow, endRow, firstCol, endCol);
				sheet.addMergedRegion(range);

				Row dataRow = sheet.createRow(firstRow);
				Cell cell = dataRow.createCell(firstCol);
				cell.setCellValue(mergeCell.getValue());
				CellStyle style = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setFontHeightInPoints(mergeCell.getCellStyleInfo().getFontSize());
				font.setFontName(mergeCell.getCellStyleInfo().getFontName());
				style.setFont(font);
				style.setAlignment(mergeCell.getCellStyleInfo().getHorizontalAlignment());
				style.setVerticalAlignment(mergeCell.getCellStyleInfo().getVerticalAlignment());
				cell.setCellStyle(style);
			} else if (excelCell instanceof CellInfo) {

				// 整理成Row
				CellInfo cellInfo = (CellInfo) excelCell;
				cellInfoList.add(cellInfo);
			}
		}

		if (cellInfoList != null && cellInfoList.size() > 0) {
			Map<Integer, List<CellInfo>> cellInfoMap = cellInfoList.stream()
					.collect(Collectors.groupingBy(item -> convertToRowIndex(item.getCell())));

			cellInfoMap.forEach((rowIndex, cellInfos) -> {
				Row dataRow = sheet.createRow(rowIndex);
				cellInfos.forEach(cellInfo -> {
					int colIndex = convertToColIndex(cellInfo.getCell(), Boolean.TRUE);
					Cell cell = dataRow.createCell(colIndex);
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
				});
			});
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

	public static int convertToRowIndex(String cell) {
		if (!cell.matches("[a-zA-z]+[0-9]+")) {
			throw new ValidationException("不合法的Excel欄位");
		}
		return Integer.parseInt(cell.replaceAll("[^0-9]", "")) - 1;
	}

	public static int convertToColIndex(String cell, boolean isValid) {
		if (isValid && !cell.matches("[a-zA-z]+[0-9]+")) {
			throw new ValidationException("不合法的Excel欄位");
		}
		String columnPart = cell.replaceAll("[0-9]", "").toUpperCase(); // 提取字母部分
		int colIndex = 0;

		for (int i = 0; i < columnPart.length(); i++) {
			colIndex *= 26; // 每增加一位，乘以 26
			colIndex += columnPart.charAt(i) - 'A' + 1; // 計算當前字母的值
		}
		return colIndex - 1;
	}

	public static void setBorders(CellStyle style) {
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
	}

	public static List<? extends ExcelCell> fillBlankRow(int dataSize, String startCol, String endCol, int skipRow) {
		List<CellInfo> cellInfoList = new ArrayList<>();
		int startColIndex = GenerateExcelUtil.convertToColIndex(startCol, Boolean.FALSE);
		int enColdIndex = GenerateExcelUtil.convertToColIndex(endCol, Boolean.FALSE);
		for (int i = dataSize; i < MinDataRowCount; i++) {
			for (int j = startColIndex; j <= enColdIndex; j++) {
				CellInfo cellInfo = new CellInfo();
				char col = (char) ('A' + j);
				cellInfo.setCell(col + Integer.toString(i + skipRow + 1));
				cellInfo.setValue(col == 'B' ? String.format("%03d", i + 1) : "");
				cellInfo.setCellStyleInfo(CellStyleInfo.TD_CENTER);
				cellInfoList.add(cellInfo);
			}
		}
		return cellInfoList;
	}
}
