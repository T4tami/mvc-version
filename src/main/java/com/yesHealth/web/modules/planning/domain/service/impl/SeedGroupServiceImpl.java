package com.yesHealth.web.modules.planning.domain.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SeedGroupServiceImpl implements SeedGroupService {

	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";
	private static final int MinDataRowCount = 18;
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
				NOT_IMPLEMENTEDSTATUS);
		int row = 0;
		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日播種計畫");
		}
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName("播種日報表");
		List<ExcelCell> cellList = new ArrayList<>();
		// =======================公司標題====================
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartCell("B1");
		companyHeader.setEndCell("O1");
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(companyHeader);
		row++;
		// =======================報表標題=====================
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartCell("B2");
		reportHeader.setEndCell("O2");
		reportHeader.setValue("播種日報表");
		reportHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(reportHeader);
		row++;
		// ========================一般欄位====================
		// b3
		CellInfo b3 = new CellInfo();
//		b3.setColIndex(1);
		b3.setCell("B3");
		b3.setValue("日期:");
		b3.setCellStyleInfo(CellStyleInfo.CENTER);

		// c3
		CellInfo c3 = new CellInfo();
//		c3.setColIndex(2);
		c3.setCell("C3");
		c3.setValue(getTodayString());
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// L3
		CellInfo l3 = new CellInfo();
//		l3.setColIndex(11);
		l3.setCell("L3");
		l3.setValue(getWeekDayString());
		l3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo o3 = new CellInfo();
//		o3.setColIndex(14);
		o3.setCell("O3");
		o3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, l3, o3));
		row++;

		String[] tableHeader = { "序", "工單號碼", "品名", "播種盤數", "播種片數", "播種日期", "播種人數", "播種時間起", "壓水時間止", "使用前克數", "使用後克數",
				"播數", "工時預估", "備註" };

		List<CellInfo> thList = IntStream.range(0, tableHeader.length).mapToObj(i -> {
			CellInfo thCell = new CellInfo();
			char letter = (char) ('B' + i);
			thCell.setCell(letter + "4");
			thCell.setValue(tableHeader[i]);
			thCell.setCellStyleInfo(CellStyleInfo.TH_CENTER);
			return thCell;
		}).collect(Collectors.toList());
		cellList.addAll(thList);
		row++;

		List<CellInfo> tdList = new ArrayList<>();
		for (int i = 0; i < psList.size(); i++) {
			CellInfo bCol = new CellInfo();
			int relocationRow = i + row + 1;
			bCol.setCell("B" + relocationRow);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setCell("C" + relocationRow);
			cCol.setValue(ps.getManuNo());
			cCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setCell("D" + relocationRow);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellStyleInfo(CellStyleInfo.TD_LEFT);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setCell("E" + relocationRow);
			eCol.setValue(ps.getSeedingBoardCount().toString());
			eCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(eCol);

			CellInfo fCol = new CellInfo();
			fCol.setCell("F" + relocationRow);
			fCol.setValue(Integer.toString(ps.getSeedingBoardCount() * 3));
			fCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(fCol);

			CellInfo gCol = new CellInfo();
			gCol.setCell("G" + relocationRow);
			gCol.setValue("");
			gCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(gCol);

			CellInfo hCol = new CellInfo();
			hCol.setCell("H" + relocationRow);
			hCol.setValue("");
			hCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(hCol);

			CellInfo iCol = new CellInfo();
			iCol.setCell("I" + relocationRow);
			iCol.setValue("");
			iCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(iCol);

			CellInfo jCol = new CellInfo();
			jCol.setCell("J" + relocationRow);
			jCol.setValue("");
			jCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(jCol);

			CellInfo kCol = new CellInfo();
			kCol.setCell("K" + relocationRow);
			kCol.setValue("");
			kCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(kCol);

			CellInfo lCol = new CellInfo();
			lCol.setCell("L" + relocationRow);
			lCol.setValue("");
			lCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(lCol);

			CellInfo mCol = new CellInfo();
			mCol.setCell("M" + relocationRow);
			mCol.setValue("");
			mCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(mCol);

			CellInfo nCol = new CellInfo();
			nCol.setCell("N" + relocationRow);
			nCol.setValue("");
			nCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(nCol);

			CellInfo oCol = new CellInfo();
			oCol.setCell("O" + relocationRow);
			oCol.setValue("");
			oCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(oCol);

		}
		cellList.addAll(tdList);
		cellList.addAll(fillBlankRow(psList.size(), "B", "O", row));
		reportInfo.setCellList(cellList);

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
		List<ExcelCell> cellList = new ArrayList<>();
		int row = 0;
		// =======================公司標題====================
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartCell("A1");
		companyHeader.setEndCell("K1");
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(companyHeader);
		row++;
		// =======================報表標題=====================
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartCell("A2");
		reportHeader.setEndCell("K2");
		reportHeader.setValue("壓水日報表");
		reportHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(reportHeader);
		row++;
		// ========================一般欄位====================

		// b3
		CellInfo b3 = new CellInfo();
		b3.setCell("B3");
		b3.setValue("日期:");
		b3.setCellStyleInfo(CellStyleInfo.CENTER);

		// c3
		CellInfo c3 = new CellInfo();
		c3.setCell("C3");
		c3.setValue(getTodayString());
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// j3
		CellInfo j3 = new CellInfo();
		j3.setCell("J3");
		j3.setValue(getWeekDayString());
		j3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo k3 = new CellInfo();
		k3.setCell("K3");
		k3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		k3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, j3, k3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = { "序", "工單號碼", "品名", "壓水盤數", "壓水人數", "壓水時間起", "壓水時間止", "暗房儲位", "暗移見日期", "備註" };
		for (int i = 0; i < tableHeader.length; i++) {
			CellInfo thCell = new CellInfo();
			int charIndex = 66;
			char letter = (char) (charIndex + i);
			thCell.setCell(letter + "4");
			thCell.setValue(tableHeader[i]);

			thCell.setCellStyleInfo(CellStyleInfo.TH_CENTER);

			thList.add(thCell);
		}
		cellList.addAll(thList);
		row++;

		List<CellInfo> tdList = new ArrayList<>();
		for (int i = 0; i < psList.size(); i++) {
			int relocationRow = i + row + 1;
			CellInfo bCol = new CellInfo();
			bCol.setCell("B" + relocationRow);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setCell("C" + relocationRow);
			cCol.setValue(ps.getManuNo());
			cCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setCell("D" + relocationRow);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellStyleInfo(CellStyleInfo.TD_LEFT);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setCell("E" + relocationRow);
			eCol.setValue(ps.getWateringBoardCount().toString());
			eCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(eCol);

			CellInfo fCol = new CellInfo();
			fCol.setCell("F" + relocationRow);
			fCol.setValue("");
			fCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(fCol);

			CellInfo gCol = new CellInfo();
			gCol.setCell("G" + relocationRow);
			gCol.setValue("");
			gCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(gCol);

			CellInfo hCol = new CellInfo();
			hCol.setCell("H" + relocationRow);
			hCol.setValue("");
			hCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(hCol);

			CellInfo iCol = new CellInfo();
			iCol.setCell("I" + relocationRow);
			iCol.setValue("");
			iCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(iCol);

			CellInfo jCol = new CellInfo();
			jCol.setCell("J" + relocationRow);
			jCol.setValue(DateUtil.convertDateToString(ps.getHeadOutDate(), "MM/dd"));
			jCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(jCol);

			CellInfo kCol = new CellInfo();
			kCol.setCell("K" + relocationRow);
			kCol.setValue("");
			kCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(kCol);

		}
		cellList.addAll(tdList);
		cellList.addAll(fillBlankRow(psList.size(), "B", "K", row));
		reportInfo.setCellList(cellList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
	}

	@Override
	public byte[] downloadheadingOutExcel() throws YhNoDataException {
		List<ProductSchedule> psList = planRepository.findByHeadOutDateBetweenAndStatus(getStartOfDay(), getEndOfDay(),
				IMPLEMENTEDSTATUS);

		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日暗移見計畫");
		}

		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName("暗移見苗日報表");
		List<ExcelCell> cellList = new ArrayList<>();
		int row = 0;
		// =======================公司標題====================
		MergeCell companyHeader = new MergeCell();
		companyHeader.setStartCell("B1");
		companyHeader.setEndCell("O1");
		companyHeader.setValue("菜好吃股份有限公司");
		companyHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(companyHeader);
		row++;
		// =======================報表標題=====================
		MergeCell reportHeader = new MergeCell();
		reportHeader.setStartCell("B2");
		reportHeader.setEndCell("O2");
		reportHeader.setValue("暗移見苗日報表");
		reportHeader.setCellStyleInfo(CellStyleInfo.HEADER);
		cellList.add(reportHeader);
		row++;
		// ========================一般欄位====================

		// b3
		CellInfo b3 = new CellInfo();
		b3.setCell("B3");
		b3.setValue("日期:");
		b3.setCellStyleInfo(CellStyleInfo.CENTER);

		// c3
		CellInfo c3 = new CellInfo();
		c3.setCell("C3");
		c3.setValue(getTodayString());
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// N3
		CellInfo n3 = new CellInfo();
		n3.setCell("N3");
		n3.setValue(getWeekDayString());
		n3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo o3 = new CellInfo();
		o3.setCell("O3");
		o3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, n3, o3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = { "序", "工單號碼", "品名", "計畫盤數", "計畫儲位", "實際盤數", "實際儲位", "開燈確認", "水道無水確認", "實際人數", "實際時間起",
				"實際時間止", "育苗日期", "備註" };
		for (int i = 0; i < tableHeader.length; i++) {
			CellInfo thCell = new CellInfo();
			int charIndex = 66;
			char letter = (char) (charIndex + i);
			thCell.setCell(letter + "4");
			thCell.setValue(tableHeader[i]);

			thCell.setCellStyleInfo(CellStyleInfo.TH_CENTER);

			thList.add(thCell);
		}
		cellList.addAll(thList);
		row++;

		List<CellInfo> tdList = new ArrayList<>();
		for (int i = 0; i < psList.size(); i++) {
			int relocationRow = i + row + 1;
			CellInfo bCol = new CellInfo();
			bCol.setCell("B" + relocationRow);
			String formatIndex = String.format("%03d", i + 1);
			bCol.setValue(formatIndex);
			bCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(bCol);

			ProductSchedule ps = psList.get(i);
			CellInfo cCol = new CellInfo();
			cCol.setCell("C" + relocationRow);
			cCol.setValue(ps.getManuNo());
			cCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(cCol);

			CellInfo dCol = new CellInfo();
			dCol.setCell("D" + relocationRow);
			dCol.setValue(ps.getProduct().getSpecs());
			dCol.setCellStyleInfo(CellStyleInfo.TD_LEFT);
			tdList.add(dCol);

			CellInfo eCol = new CellInfo();
			eCol.setCell("E" + relocationRow);
			eCol.setValue(ps.getHeadOutBoardCount().toString());
			eCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(eCol);

			CellInfo fCol = new CellInfo();
			fCol.setCell("F" + relocationRow);
			fCol.setValue("");
			fCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(fCol);

			CellInfo gCol = new CellInfo();
			gCol.setCell("G" + relocationRow);
			gCol.setValue("");
			gCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(gCol);

			CellInfo hCol = new CellInfo();
			hCol.setCell("H" + relocationRow);
			hCol.setValue("");
			hCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(hCol);

			CellInfo iCol = new CellInfo();
			iCol.setCell("I" + relocationRow);
			iCol.setValue("");
			iCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(iCol);

			CellInfo jCol = new CellInfo();
			jCol.setCell("J" + relocationRow);
			jCol.setValue("");
			jCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(jCol);

			CellInfo kCol = new CellInfo();
			kCol.setCell("K" + relocationRow);
			kCol.setValue("");
			kCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(kCol);

			CellInfo lCol = new CellInfo();
			lCol.setCell("L" + relocationRow);
			lCol.setValue("");
			lCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(lCol);

			CellInfo mCol = new CellInfo();
			mCol.setCell("M" + relocationRow);
			mCol.setValue("");
			mCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(mCol);

			CellInfo nCol = new CellInfo();
			nCol.setCell("N" + relocationRow);
			nCol.setValue(DateUtil.convertDateToString(ps.getGrowingDate(), "MM/dd"));
			nCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(nCol);

			CellInfo oCol = new CellInfo();
			oCol.setCell("O" + relocationRow);
			oCol.setValue("");
			oCol.setCellStyleInfo(CellStyleInfo.TD_CENTER);
			tdList.add(oCol);

		}
		cellList.addAll(tdList);
		cellList.addAll(fillBlankRow(psList.size(), "B", "O", row));
		reportInfo.setCellList(cellList);

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

	private List<? extends ExcelCell> fillBlankRow(int dataSize, String startCol, String endCol, int skipRow) {
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
