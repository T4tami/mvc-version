package com.yesHealth.web.modules.planning.domain.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.yesHealth.web.modules.util.CellInfo;
import com.yesHealth.web.modules.util.CellStyleInfo;
import com.yesHealth.web.modules.util.DateUtil;
import com.yesHealth.web.modules.util.ExcelCell;
import com.yesHealth.web.modules.util.ReportInfo;

@Service
public class SeedGroupServiceImpl implements SeedGroupService {

	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";
	private PlanRepository planRepository;

	public SeedGroupServiceImpl(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}

	@Override
	public Page<ProductSchedule> findBySeedingGroupForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return planRepository.findBySeedingDateBetweenAndStatus(formateStartDate, formateEndDate, NOT_IMPLEMENTEDSTATUS,
				pageable);
	}

	@Override
	public Page<ProductSchedule> getWateringForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return planRepository.findByWateringDateBetweenAndStatus(formateStartDate, formateEndDate,
				NOT_IMPLEMENTEDSTATUS, pageable);
	}

	@Override
	public Page<ProductSchedule> getHeadOutForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return planRepository.findByHeadOutDateBetweenAndStatus(formateStartDate, formateEndDate, NOT_IMPLEMENTEDSTATUS,
				pageable);
	}

	@Override
	public byte[] downloadSeedExcel() throws YhNoDataException {

		List<ProductSchedule> psList = planRepository.findBySeedingDateBetweenAndStatus(getStartOfDay(), getEndOfDay(),
				IMPLEMENTEDSTATUS);

		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日播種計畫");
		}
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName("播種日報表");
		reportInfo.setMinRowCount(22);
		reportInfo.setDataRowCount(psList.size());
		reportInfo.setColCount(14);
		List<RowInfo> dataList = new ArrayList<>();

		// =======================公司標題====================
		RowInfo companyHeaderRow = new RowInfo();
		companyHeaderRow.setRowIndex(0);
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartRowIndex(0);
		companyHeader.setEndRowIndex(0);
		companyHeader.setStartColIndex(1);
		companyHeader.setEndColIndex(14);
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		companyHeaderRow.setRowData(new ArrayList<ExcelCell>(Arrays.asList(companyHeader)));
		dataList.add(companyHeaderRow);
		// =======================報表標題=====================
		RowInfo reportHeaderRow = new RowInfo();
		reportHeaderRow.setRowIndex(1);
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartRowIndex(1);
		reportHeader.setEndRowIndex(1);
		reportHeader.setStartColIndex(1);
		reportHeader.setEndColIndex(14);
		reportHeader.setValue("播種日報表");
		reportHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		reportHeaderRow.setRowData(new ArrayList<ExcelCell>(Arrays.asList(reportHeader)));
		dataList.add(reportHeaderRow);
		// ========================一般欄位====================
		RowInfo rowInfo = new RowInfo();
		rowInfo.setRowIndex(2);
		List<CellInfo> tableRowData = new ArrayList<>();

		// b3
		CellInfo b3 = new CellInfo();
//		b3.setColIndex(1);
		b3.setCell("B3");
		b3.setValue("日期:");
		b3.setCellStyleInfo(CellStyleInfo.CENTER);
		tableRowData.add(b3);

		// c3
		CellInfo c3 = new CellInfo();
//		c3.setColIndex(2);
		c3.setCell("C3");
		c3.setValue(getTodayString());
		c3.setCellStyleInfo(CellStyleInfo.LEFT);
		tableRowData.add(c3);

		// L3
		CellInfo l3 = new CellInfo();
//		l3.setColIndex(11);
		l3.setCell("L3");
		l3.setValue(getWeekDayString());
		l3.setCellStyleInfo(CellStyleInfo.CENTER);
		tableRowData.add(l3);

		// k3
		CellInfo o3 = new CellInfo();
//		o3.setColIndex(14);
		o3.setCell("O3");
		o3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		tableRowData.add(o3);
		rowInfo.setRowData(tableRowData);
		dataList.add(rowInfo);

		RowInfo thRowInfo = new RowInfo();
		thRowInfo.setRowIndex(3);
		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = { "序", "工單號碼", "品名", "播種盤數", "播種片數", "播種日期", "播種人數", "播種時間起", "壓水時間止", "使用前克數", "使用後克數",
				"播數", "工時預估", "備註" };
		for (int i = 0; i < tableHeader.length; i++) {
			CellInfo thCell = new CellInfo();
			int charIndex = 65;
			char letter = (char) (charIndex + i);
			thCell.setCell(letter + "4");
			thCell.setValue(tableHeader[i]);

			thCell.setCellStyleInfo(CellStyleInfo.TH_CENTER);

			thList.add(thCell);
		}
		thRowInfo.setRowData(thList);
		dataList.add(thRowInfo);

		RowInfo tdRowInfo = new RowInfo();
		List<CellInfo> tdList = new ArrayList<>();
		for (int i = 0; i < psList.size(); i++) {
			tdRowInfo.setRowIndex(4 + i);

			CellInfo bCol = new CellInfo();
			bCol.setCell("B" + i);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setCell("C" + i);
			cCol.setValue(ps.getManuNo());
			cCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setCell("D" + i);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellStyleInfo(CellStyleInfo.TD_LEFT);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setCell("E" + i);
			eCol.setValue(ps.getSeedingBoardCount().toString());
			eCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(eCol);

			CellInfo fCol = new CellInfo();
			fCol.setCell("F" + i);
			fCol.setValue(Integer.toString(ps.getSeedingBoardCount() * 3));
			fCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(fCol);

			CellInfo gCol = new CellInfo();
			gCol.setCell("G" + i);
			gCol.setValue("");
			gCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(gCol);

			CellInfo hCol = new CellInfo();
			hCol.setCell("H" + i);
			hCol.setValue("");
			hCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(hCol);

			CellInfo iCol = new CellInfo();
			iCol.setCell("I" + i);
			iCol.setValue("");
			iCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(iCol);

			CellInfo jCol = new CellInfo();
			jCol.setCell("J" + i);
			jCol.setValue("");
			jCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(jCol);

			CellInfo kCol = new CellInfo();
			kCol.setCell("K" + i);
			kCol.setValue("");
			kCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(kCol);

			CellInfo lCol = new CellInfo();
			lCol.setCell("L" + i);
			lCol.setValue("");
			lCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(lCol);

			CellInfo mCol = new CellInfo();
			mCol.setCell("M" + i);
			mCol.setValue("");
			mCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(mCol);

			CellInfo nCol = new CellInfo();
			nCol.setCell("N" + i);
			nCol.setValue("");
			nCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(nCol);

			CellInfo oCol = new CellInfo();
			oCol.setCell("O" + i);
			oCol.setValue("");
			oCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(oCol);

			dataList.add(tdRowInfo);
		}
		tdRowInfo.setRowData(tdList);
		dataList.add(tdRowInfo);
		reportInfo.setRowList(dataList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
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
		reportInfo.setMinRowCount(22);
		reportInfo.setDataRowCount(psList.size());
		reportInfo.setColCount(10);
		List<RowInfo> dataList = new ArrayList<>();

		// =======================公司標題====================
		RowInfo companyHeaderRow = new RowInfo();
		companyHeaderRow.setRowIndex(0);
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartRowIndex(0);
		companyHeader.setEndRowIndex(0);
		companyHeader.setStartColIndex(1);
		companyHeader.setEndColIndex(10);
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		companyHeaderRow.setRowData(new ArrayList<ExcelCell>(Arrays.asList(companyHeader)));
		dataList.add(companyHeaderRow);
		// =======================報表標題=====================
		RowInfo reportHeaderRow = new RowInfo();
		reportHeaderRow.setRowIndex(1);
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartRowIndex(1);
		reportHeader.setEndRowIndex(1);
		reportHeader.setStartColIndex(1);
		reportHeader.setEndColIndex(10);
		reportHeader.setValue("壓水日報表");
		reportHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		reportHeaderRow.setRowData(new ArrayList<ExcelCell>(Arrays.asList(reportHeader)));
		dataList.add(reportHeaderRow);
		// ========================一般欄位====================
		RowInfo rowInfo = new RowInfo();
		rowInfo.setRowIndex(2);
		List<CellInfo> tableRowData = new ArrayList<>();

		// b3
		CellInfo b3 = new CellInfo();
		b3.setCell("B3");
		b3.setValue("日期:");
		b3.setCellStyleInfo(CellStyleInfo.CENTER);
		tableRowData.add(b3);

		// c3
		CellInfo c3 = new CellInfo();
		c3.setCell("C3");
		c3.setValue(getTodayString());
		c3.setCellStyleInfo(CellStyleInfo.LEFT);
		tableRowData.add(c3);

		// j3
		CellInfo j3 = new CellInfo();
		j3.setCell("J3");
		j3.setValue(getWeekDayString());
		j3.setCellStyleInfo(CellStyleInfo.CENTER);
		tableRowData.add(j3);

		// k3
		CellInfo k3 = new CellInfo();
		k3.setCell("K3");
		k3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		k3.setCellStyleInfo(CellStyleInfo.RIGHT);
		tableRowData.add(k3);
		rowInfo.setRowData(tableRowData);
		dataList.add(rowInfo);

		RowInfo thRowInfo = new RowInfo();
		thRowInfo.setRowIndex(3);
		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = { "序", "工單號碼", "品名", "壓水盤數", "壓水人數", "壓水時間起", "壓水時間止", "暗房儲位", "暗移見日期", "備註" };
		for (int i = 0; i < tableHeader.length; i++) {
			CellInfo thCell = new CellInfo();
			int charIndex = 65;
			char letter = (char) (charIndex + i);
			thCell.setCell(letter + "4");
			thCell.setValue(tableHeader[i]);

			thCell.setCellStyleInfo(CellStyleInfo.TH_CENTER);

			thList.add(thCell);
		}
		thRowInfo.setRowData(thList);
		dataList.add(thRowInfo);

		RowInfo tdRowInfo = new RowInfo();
		List<CellInfo> tdList = new ArrayList<>();
		for (int i = 0; i < psList.size(); i++) {
			tdRowInfo.setRowIndex(4 + i);

			CellInfo bCol = new CellInfo();
			bCol.setCell("B" + i);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setCell("C" + i);
			cCol.setValue(ps.getManuNo());
			cCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setCell("D" + i);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellStyleInfo(CellStyleInfo.TD_LEFT);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setCell("E" + i);
			eCol.setValue(ps.getWateringBoardCount().toString());
			eCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(eCol);

			CellInfo fCol = new CellInfo();
			fCol.setCell("F" + i);
			fCol.setValue("");
			fCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(fCol);

			CellInfo gCol = new CellInfo();
			gCol.setCell("G" + i);
			gCol.setValue("");
			gCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(gCol);

			CellInfo hCol = new CellInfo();
			hCol.setCell("H" + i);
			hCol.setValue("");
			hCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(hCol);

			CellInfo iCol = new CellInfo();
			iCol.setCell("I" + i);
			iCol.setValue("");
			iCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(iCol);

			CellInfo jCol = new CellInfo();
			jCol.setCell("J" + i);
			jCol.setValue(convertHeadOutDate(ps.getHeadOutDate()));
			jCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(jCol);

			CellInfo kCol = new CellInfo();
			kCol.setCell("K" + i);
			kCol.setValue("");
			kCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(kCol);

			dataList.add(tdRowInfo);
		}
		tdRowInfo.setRowData(tdList);
		dataList.add(tdRowInfo);
		reportInfo.setRowList(dataList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
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
