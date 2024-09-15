package com.yesHealth.web.modules.report.domain.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.report.domain.repository.QcReportRepository;
import com.yesHealth.web.modules.report.domain.service.QcReportService;
import com.yesHealth.web.modules.report.domain.view.QcReportView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class QcReportServiceImpl implements QcReportService {
	private QcReportRepository qcReportRepository;

	public QcReportServiceImpl(QcReportRepository qcReportRepository) {
		this.qcReportRepository = qcReportRepository;
	}

	@Override
	public byte[] exportToExcel(Date startDate, Date endDate) {

		List<QcReportView> data = qcReportRepository.findByActTransDateBetween(startDate, endDate);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("QC Report");

			// Create cell styles
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("MM/dd"));

			CellStyle processGStyle = workbook.createCellStyle();
			processGStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			processGStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			Font greenFont = workbook.createFont();
			greenFont.setColor(IndexedColors.DARK_GREEN.getIndex());
			processGStyle.setFont(greenFont);

			CellStyle actTransDateStyle = workbook.createCellStyle();
			actTransDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle boronStyle = createCellStyle(workbook, IndexedColors.LIGHT_BLUE);
			CellStyle yellowingStyle = createCellStyle(workbook, IndexedColors.LIGHT_YELLOW);
			CellStyle elongationStyle = createCellStyle(workbook, IndexedColors.LIGHT_GREEN);
			CellStyle browningStyle = createCellStyle(workbook, IndexedColors.LIGHT_ORANGE);
			CellStyle growthStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT);
			CellStyle pestStyle = createCellStyle(workbook, IndexedColors.LIGHT_BLUE);
			CellStyle otherStyle = createCellStyle(workbook, IndexedColors.LIGHT_GREEN);

			// Create the first row: "巡檢日期, now()"
			Row firstRow = sheet.createRow(0);
			Cell firstCell = firstRow.createCell(0);
			firstCell.setCellValue("巡檢日期");
			firstCell.setCellStyle(headerStyle);
			firstRow.createCell(1).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

			// Create the second row: headers
			Row headerRow = sheet.createRow(1);
			String[] headers = { "工單號", "品名", "盤數", "採收日期", "目前儲位", "製程", "S", "G", "P", "移入天數", "理想株高", "實際株高", "缺硼",
					"%", "黃化", "%", "徒長", "%", "褐化", "%", "長勢", "%", "蟲害", "%", "其他", "%" };

			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
				sheet.autoSizeColumn(i);
			}

			// Create data rows
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
			int rowNum = 2;
			for (QcReportView report : data) {
				Row row = sheet.createRow(rowNum++);

				row.createCell(0).setCellValue(report.getManuNo());
				row.createCell(1).setCellValue(report.getProductName());
				row.createCell(2).setCellValue(report.getActBoardCount() != null ? report.getActBoardCount() : 0);

				Cell harvestDateCell = row.createCell(3);
				harvestDateCell.setCellValue(
						report.getEstHarvestDate() != null ? dateFormat.format(report.getEstHarvestDate()) : "");
				harvestDateCell.setCellStyle(dateCellStyle);

				row.createCell(4).setCellValue(report.getPosition());

				// Determine cell style for 目前儲位
				Cell positionCell = row.createCell(4);
				CellStyle positionStyle = workbook.createCellStyle();
				if (report.getPlantLux() != null && report.getStockLux() != null) {
					if (report.getPlantLux().equals(report.getStockLux())) {
						positionStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
					} else if (report.getPlantLux() < report.getStockLux()) {
						positionStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
					} else {
						positionStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
					}
					positionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
				positionCell.setCellStyle(positionStyle);

				// Determine cell style for 製程
				Cell processCell = row.createCell(5);
				processCell.setCellValue(report.getStage());
				CellStyle processStyle = processCell.getCellStyle();
				if ("G".equals(report.getStage())) {
					processStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
					processStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					Font font = workbook.createFont();
					font.setColor(IndexedColors.DARK_GREEN.getIndex());
					processStyle.setFont(font);
				}
				processCell.setCellStyle(processStyle);

				// Determine cell style for 移入天數
				Cell actTransDateCell = row.createCell(9);
				long days = (new Date().getTime() - report.getActTransDate().getTime()) / (1000 * 60 * 60 * 24);
				actTransDateCell.setCellValue(days);
				if (days > 14) {
					actTransDateStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
				} else if (days == 7) {
					actTransDateStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
				} else if (days <= 1) {
					actTransDateStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
				} else if (days == 4) {
					actTransDateStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
				}
				actTransDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				actTransDateCell.setCellStyle(actTransDateStyle);

				// Fill other cells
				row.createCell(6).setCellValue(""); // S
				row.createCell(7).setCellValue(""); // G
				row.createCell(8).setCellValue(""); // P
				row.createCell(10).setCellValue(report.getIdealHeight() != null ? report.getIdealHeight() : 0); // 理想株高
				row.createCell(11).setCellValue(""); // 實際株高
				row.createCell(12).setCellValue(""); // 缺硼
				row.createCell(13).setCellValue(""); // %
				row.createCell(14).setCellValue(""); // 黃化
				row.createCell(15).setCellValue(""); // %
				row.createCell(16).setCellValue(""); // 徒長
				row.createCell(17).setCellValue(""); // %
				row.createCell(18).setCellValue(""); // 褐化
				row.createCell(19).setCellValue(""); // %
				row.createCell(20).setCellValue(""); // 長勢
				row.createCell(21).setCellValue(""); // %
				row.createCell(22).setCellValue(""); // 蟲害
				row.createCell(23).setCellValue(""); // %
				row.createCell(24).setCellValue(""); // 其他
				row.createCell(25).setCellValue(""); // %

				// Apply background colors to other columns
				row.getCell(12).setCellStyle(boronStyle);
				row.getCell(14).setCellStyle(yellowingStyle);
				row.getCell(16).setCellStyle(elongationStyle);
				row.getCell(18).setCellStyle(browningStyle);
				row.getCell(20).setCellStyle(growthStyle);
				row.getCell(22).setCellStyle(pestStyle);
				row.getCell(24).setCellStyle(otherStyle);
			}

			workbook.write(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	// Helper method to create cell styles
	private CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(color.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}
}