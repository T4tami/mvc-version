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
import java.util.ArrayList;
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
import com.yesHealth.web.modules.util.GenerateExcelUtil;
import com.yesHealth.web.modules.util.MergeCell;
import com.yesHealth.web.modules.util.CellInfoStyle;
import com.yesHealth.web.modules.util.ReportInfo;

@Service
public class SeedGroupServiceImpl implements SeedGroupService {
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";
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

		List<ProductSchedule> psList = planRepository.findBySeedingDateBetweenAndStatus(getStartOfDay(), getEndOfDay(),
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

	@Override
	public byte[] downloadWateringExcel() throws YhNoDataException {

		List<ProductSchedule> psList = planRepository.findByWateringDateBetweenAndStatus(getStartOfDay(), getEndOfDay(),
				IMPLEMENTEDSTATUS);

		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日壓水計畫");
		}

		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName("壓水日報表");
		List<Object> dataList = new ArrayList<>();

		// =========================標題style==================
		CellInfoStyle headerStyle = new CellInfoStyle();
		headerStyle.setFontName("標楷體");
		headerStyle.setFontSize((short) 18);
		headerStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		// =======================公司標題====================
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartRowIndex(0);
		companyHeader.setEndRowIndex(0);
		companyHeader.setStartColIndex(1);
		companyHeader.setEndColIndex(10);
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellInfoStyle(headerStyle);

		dataList.add(companyHeader);
		// =======================報表標題=====================
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartRowIndex(1);
		reportHeader.setEndRowIndex(1);
		reportHeader.setStartColIndex(1);
		reportHeader.setEndColIndex(10);
		reportHeader.setValue("壓水日報表");
		reportHeader.setCellInfoStyle(headerStyle);
		dataList.add(reportHeader);
		// ========================一般欄位====================
		RowInfo rowInfo = new RowInfo();
		rowInfo.setRowIndex(4);
		List<CellInfo> tableRowData = new ArrayList<>();
		// =========================一般欄位Style===============
		// 置中
		CellInfoStyle center = new CellInfoStyle();
		center.setFontName("標楷體");
		center.setFontSize((short) 11);
		center.setHorizontalAlignment(HorizontalAlignment.CENTER);
		center.setVerticalAlignment(VerticalAlignment.CENTER);
		// 靠左
		CellInfoStyle left = new CellInfoStyle();
		left.setFontName("標楷體");
		left.setFontSize((short) 11);
		left.setHorizontalAlignment(HorizontalAlignment.LEFT);
		left.setVerticalAlignment(VerticalAlignment.CENTER);
		// 靠左
		CellInfoStyle right = new CellInfoStyle();
		right.setFontName("標楷體");
		right.setFontSize((short) 11);
		right.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		right.setVerticalAlignment(VerticalAlignment.CENTER);
		// b3
		CellInfo b3 = new CellInfo();
		b3.setColIndex(1);
		b3.setValue("日期:");
		b3.setCellInfoStyle(center);
		tableRowData.add(b3);

		// c3
		CellInfo c3 = new CellInfo();
		c3.setColIndex(2);
		c3.setValue(getTodayString());
		c3.setCellInfoStyle(left);
		tableRowData.add(c3);

		// j3
		CellInfo j3 = new CellInfo();
		j3.setColIndex(2);
		j3.setValue(getWeekDayString());
		j3.setCellInfoStyle(center);
		tableRowData.add(j3);

		// k3
		CellInfo k3 = new CellInfo();
		k3.setColIndex(2);
		k3.setValue("總盤數:" + calculateTotalBoardCount(psList) + "盤");
		k3.setCellInfoStyle(right);
		tableRowData.add(k3);
		dataList.add(tableRowData);

		List<CellInfo> thList = new ArrayList<>();

		CellInfoStyle thCenterStyle = new CellInfoStyle();
		thCenterStyle.setFontName("標楷體");
		thCenterStyle.setFontSize((short) 11);
		thCenterStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
		thCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		thCenterStyle.setBorderTop(BorderStyle.THIN);
		thCenterStyle.setBorderBottom(BorderStyle.THIN);
		thCenterStyle.setBorderLeft(BorderStyle.THIN);
		thCenterStyle.setBorderRight(BorderStyle.THIN);
		thCenterStyle.setWrapText(Boolean.TRUE);
		String[] tableHeader = { "序", "工單號碼", "品名", "壓水盤數", "壓水人數", "壓水時間起", "壓水時間止", "暗房儲位", "暗移見日期", "備註" };
		for (int i = 0; i < tableHeader.length; i++) {
			CellInfo thCell = new CellInfo();
			thCell.setColIndex(i + 1);
			thCell.setValue(tableHeader[i]);

			thCell.setCellInfoStyle(thCenterStyle);

			thList.add(thCell);
		}
		dataList.add(thList);

		// table_detail
		CellInfoStyle tdCenterStyle = new CellInfoStyle();
		tdCenterStyle.setFontName("標楷體");
		tdCenterStyle.setFontSize((short) 11);
		tdCenterStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
		tdCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		tdCenterStyle.setBorderTop(BorderStyle.THIN);
		tdCenterStyle.setBorderBottom(BorderStyle.THIN);
		tdCenterStyle.setBorderLeft(BorderStyle.THIN);
		tdCenterStyle.setBorderRight(BorderStyle.THIN);

		CellInfoStyle tdLeftStyle = new CellInfoStyle();
		tdLeftStyle.setFontName("標楷體");
		tdLeftStyle.setFontSize((short) 11);
		tdLeftStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
		tdLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		tdLeftStyle.setBorderTop(BorderStyle.THIN);
		tdLeftStyle.setBorderBottom(BorderStyle.THIN);
		tdLeftStyle.setBorderLeft(BorderStyle.THIN);
		tdLeftStyle.setBorderRight(BorderStyle.THIN);

		for (int i = 0; i < psList.size(); i++) {
			RowInfo tdRowInfo = new RowInfo();
			tdRowInfo.setRowIndex(4 + i + 1);

			List<CellInfo> tdList = new ArrayList<>();
			CellInfo bCol = new CellInfo();
			bCol.setColIndex(i + 1);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellInfoStyle(tdCenterStyle);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setColIndex(i + 2);
			cCol.setValue(ps.getManuNo());
			cCol.setCellInfoStyle(tdCenterStyle);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setColIndex(i + 3);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellInfoStyle(tdLeftStyle);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setColIndex(i + 4);
			eCol.setValue(ps.getWateringBoardCount().toString());
			eCol.setCellInfoStyle(tdCenterStyle);
			tdList.add(eCol);

			CellInfo jCol = new CellInfo();
			jCol.setColIndex(i + 9);
			jCol.setValue(convertHeadOutDate(ps.getHeadOutDate()));
			jCol.setCellInfoStyle(tdCenterStyle);
			tdList.add(jCol);

			tdRowInfo.setRowData(tdList);
			dataList.add(tdRowInfo);
		}

		reportInfo.setDataList(dataList);

		return GenerateExcelUtil.genDailyWateringReport(reportInfo);
	}

	private Integer calculateTotalBoardCount(List<ProductSchedule> psList) {
		Integer sum = 0;
		for (ProductSchedule ps : psList) {
			Integer count = ps.getWateringBoardCount();
			if (count != null) {
				sum += count;
			}
		}
		return sum;
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


	private String getTodayString() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
		return formatter.format(new Date());
	}

	private String getWeekDayString() {
		String[] weekDays = { "一", "二", "三", "四", "五", "六", "日" };
		int dayOfWeek = Integer.parseInt(new SimpleDateFormat("u").format(new Date()));
		return "星期" + weekDays[dayOfWeek - 1];
	}

	private Date getStartOfDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private Date getEndOfDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}

	private String convertHeadOutDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
		return formatter.format(date);
	}
}
