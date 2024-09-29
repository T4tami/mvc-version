package com.yesHealth.web.modules.planning.domain.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.planning.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.planning.domain.respository.PlanRepository;
import com.yesHealth.web.modules.planning.domain.service.SeedGroupService;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

@Service
public class SeedGroupServiceImpl implements SeedGroupService {
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private PlanRepository planRepository;

	public SeedGroupServiceImpl(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}

	@Override
	public Page<ProductSchedule> findBySeedingGroupForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findBySeedingDateBetweenAndStatus(formateStartDate, formateEndDate, NOT_IMPLEMENTEDSTATUS,
				pageable);
	}

	@Override
	public Page<ProductSchedule> getWateringForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findByWateringDateBetweenAndStatus(formateStartDate, formateEndDate,
				NOT_IMPLEMENTEDSTATUS, pageable);
	}

	@Override
	public Page<ProductSchedule> getHeadOutForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findByHeadOutDateBetweenAndStatus(formateStartDate, formateEndDate, NOT_IMPLEMENTEDSTATUS,
				pageable);
	}

	private Date getStartOfNextWeek() {
		LocalDate today = LocalDate.now();
		LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		return convertToDate(nextMonday);
	}

	// 获取下周的最后一天（周五）
	private Date getEndOfNextWeek() {
		LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextFriday = nextMonday.plusDays(4);
		return convertToDate(nextFriday);
	}

	private Date convertToDate(LocalDate localDate) {
		LocalDateTime localDateTime = localDate.atStartOfDay(); // 默认时间是 00:00:00
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private Date convertStringToDate(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null; // 或抛出自定义异常
		}
	}

	@Override
	public byte[] downloadSeedExcel() throws YhNoDataException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("播種報表");

		createMergedCell(sheet, 0, 1, 14, "菜好吃生物科技股份有限公司", 18, (XSSFWorkbook) workbook);
		createMergedCell(sheet, 1, 1, 14, "播種日報表", 18, (XSSFWorkbook) workbook);

		Row row3 = sheet.createRow(2);
		createCell(row3, 1, "日期:", HorizontalAlignment.LEFT, (XSSFWorkbook) workbook);
		createDateCell(row3, 2, new Date(), (XSSFWorkbook) workbook);

		Cell cellL3 = row3.createCell(11);
		setWeekday(cellL3, (XSSFWorkbook) workbook);

		createCell(row3, 14, "總盤數:盤", HorizontalAlignment.CENTER, (XSSFWorkbook) workbook);

		Row row4 = sheet.createRow(3);
		String[] headers = { "序", "工單號碼", "品名", "盤數", "片數", "播種日期", "人數", "時間起", "時間止", "使用前克數", "使用後克數", "播數", "工時預估",
				"備註" };
		for (int i = 0; i < headers.length; i++) {
			createCell(row4, i + 1, headers[i], HorizontalAlignment.CENTER, (XSSFWorkbook) workbook);
		}

		// 获取当前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		// 设置时间为当天的开始
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startOfDay = calendar.getTime();

		// 设置时间为当天的结束
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		Date endOfDay = calendar.getTime();

		List<ProductSchedule> psList = planRepository.findBySeedingDateBetweenAndStatus(startOfDay, endOfDay,
				NOT_IMPLEMENTEDSTATUS);

		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日播種計畫");
		}
		for (int i = 0; i < psList.size(); i++) {
			Row dataRow = sheet.createRow(4 + i);
			ProductSchedule ps = psList.get(i);

			Cell cellB = dataRow.createCell(1);
			String cellBStr = String.format("%03d", i + 1); // 格式化为三位数
			cellB.setCellValue(cellBStr);
			CellStyle cellBStyle = createTableStyle((XSSFWorkbook) workbook);
			cellBStyle.setAlignment(HorizontalAlignment.CENTER);
			cellBStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellB.setCellStyle(cellBStyle);

			Cell cellC = dataRow.createCell(2);
			cellC.setCellValue(ps.getManuNo());
			CellStyle cellCStyle = createTableStyle((XSSFWorkbook) workbook);
			cellCStyle.setAlignment(HorizontalAlignment.LEFT);
			cellCStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellC.setCellStyle(cellCStyle);

			Cell cellD = dataRow.createCell(3);
			cellD.setCellValue(ps.getProduct().getSpecs());
			CellStyle cellDStyle = createTableStyle((XSSFWorkbook) workbook);
			cellDStyle.setAlignment(HorizontalAlignment.CENTER);
			cellDStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellD.setCellStyle(cellDStyle);

			Cell cellE = dataRow.createCell(4);
			cellE.setCellValue(ps.getSeedingBoardCount());
			CellStyle cellEStyle = createTableStyle((XSSFWorkbook) workbook);
			cellEStyle.setAlignment(HorizontalAlignment.CENTER);
			cellEStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellE.setCellStyle(cellEStyle);

			Cell cellF = dataRow.createCell(5);
			cellF.setCellValue(ps.getSeedingBoardCount() * 3);
			CellStyle cellFStyle = createTableStyle((XSSFWorkbook) workbook);
			cellFStyle.setAlignment(HorizontalAlignment.CENTER);
			cellFStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellF.setCellStyle(cellFStyle);

			Cell cellG = dataRow.createCell(6);
			CellStyle cellGStyle = createTableStyle((XSSFWorkbook) workbook);
			cellGStyle.setAlignment(HorizontalAlignment.CENTER);
			cellGStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellG.setCellStyle(cellGStyle);

			Cell cellH = dataRow.createCell(7);
			CellStyle cellHStyle = createTableStyle((XSSFWorkbook) workbook);
			cellHStyle.setAlignment(HorizontalAlignment.CENTER);
			cellHStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellH.setCellStyle(cellHStyle);

			Cell cellI = dataRow.createCell(8);
			CellStyle cellIStyle = createTableStyle((XSSFWorkbook) workbook);
			cellIStyle.setAlignment(HorizontalAlignment.CENTER);
			cellIStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellI.setCellStyle(cellIStyle);

			Cell cellJ = dataRow.createCell(9);
			CellStyle cellJStyle = createTableStyle((XSSFWorkbook) workbook);
			cellJStyle.setAlignment(HorizontalAlignment.CENTER);
			cellJStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellJ.setCellStyle(cellJStyle);

			Cell cellK = dataRow.createCell(10);
			CellStyle cellKStyle = createTableStyle((XSSFWorkbook) workbook);
			cellKStyle.setAlignment(HorizontalAlignment.CENTER);
			cellKStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellK.setCellStyle(cellKStyle);

			Cell cellL = dataRow.createCell(11);
			CellStyle cellLStyle = createTableStyle((XSSFWorkbook) workbook);
			cellLStyle.setAlignment(HorizontalAlignment.CENTER);
			cellLStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellL.setCellStyle(cellLStyle);

			Cell cellM = dataRow.createCell(12);
			CellStyle cellMStyle = createTableStyle((XSSFWorkbook) workbook);
			cellMStyle.setAlignment(HorizontalAlignment.CENTER);
			cellMStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellM.setCellStyle(cellMStyle);

			Cell cellN = dataRow.createCell(13);
			CellStyle cellNStyle = createTableStyle((XSSFWorkbook) workbook);
			cellNStyle.setAlignment(HorizontalAlignment.CENTER);
			cellNStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellN.setCellStyle(cellNStyle);

			Cell cellO = dataRow.createCell(14);
			CellStyle cellOStyle = createTableStyle((XSSFWorkbook) workbook);
			cellOStyle.setAlignment(HorizontalAlignment.CENTER);
			cellOStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellO.setCellStyle(cellOStyle);

		}

		// 寫入 ByteArrayOutputStream
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

	private void createMergedCell(Sheet sheet, int rowIndex, int startCol, int endCol, String value, int fontSize,
			XSSFWorkbook workbook) {
		CellRangeAddress range = new CellRangeAddress(rowIndex, rowIndex, startCol, endCol);
		sheet.addMergedRegion(range);
		Cell cell = sheet.createRow(rowIndex).createCell(startCol);
		cell.setCellValue(value);
		CellStyle style = createCellStyle(fontSize, workbook);
		cell.setCellStyle(style);
	}

	private void createCell(Row row, int colIndex, String value, HorizontalAlignment alignment, XSSFWorkbook workbook) {
		Cell cell = row.createCell(colIndex);
		cell.setCellValue(value);
		CellStyle style = createTableStyle(workbook);
		style.setAlignment(alignment);
		cell.setCellStyle(style);
	}

	private void createDateCell(Row row, int colIndex, Date date, XSSFWorkbook workbook) {
		Cell cell = row.createCell(colIndex);
		cell.setCellValue(date);
		CellStyle dateStyle = createCellStyle(11, workbook);
		CreationHelper creationHelper = cell.getSheet().getWorkbook().getCreationHelper();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy年MM月dd日"));
		cell.setCellStyle(dateStyle);
	}

	private void setWeekday(Cell cell, XSSFWorkbook workbook) {
		String[] weekDays = { "一", "二", "三", "四", "五", "六", "日" };
		int dayOfWeek = Integer.parseInt(new SimpleDateFormat("u").format(new Date()));
		cell.setCellValue("星期" + weekDays[dayOfWeek - 1]);
		CellStyle style = createBoldStyle(workbook);
		cell.setCellStyle(style);
	}

	private CellStyle createCellStyle(int fontSize, XSSFWorkbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) fontSize);
		font.setFontName("標楷體");
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		return style;
	}

	private CellStyle createBoldStyle(XSSFWorkbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	private CellStyle createTableStyle(XSSFWorkbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		Font font = workbook.createFont();
		font.setFontName("標楷體");
		font.setFontHeightInPoints((short) 11);
		style.setFont(font);
		return style;
	}

}
