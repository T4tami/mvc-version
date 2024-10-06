package com.yesHealth.web.modules.production.domain.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yesHealth.web.global.security.model.CustomUserDetails;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.production.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.production.domain.respository.PlanRepository;
import com.yesHealth.web.modules.production.domain.service.SeedGroupService;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.repository.UserRepository;
import com.yesHealth.web.modules.util.GenerateExcelUtil;
import com.yesHealth.web.modules.util.MergeCell;
import com.yesHealth.web.modules.util.CellInfo;
import com.yesHealth.web.modules.util.CellStyleInfo;
import com.yesHealth.web.modules.util.DateUtil;
import com.yesHealth.web.modules.util.ExcelCell;
import com.yesHealth.web.modules.util.ReportInfo;
import com.yesHealth.web.modules.util.UploadFileUtil;
import com.yesHealth.web.modules.util.entity.FileUploadRecords;
import com.yesHealth.web.modules.util.entity.FileUploadStatus;
import com.yesHealth.web.modules.util.exception.UplaodFileException;
import com.yesHealth.web.modules.util.repository.FileUploadRecordsRepository;

@Service
public class SeedGroupServiceImpl implements SeedGroupService {

	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";
	private PlanRepository planRepository;
	private FileUploadRecordsRepository fileUploadRecordsRepository;
	private UserRepository userRepository;

	public SeedGroupServiceImpl(PlanRepository planRepository, FileUploadRecordsRepository fileUploadRecordsRepository,
			UserRepository userRepository) {
		this.planRepository = planRepository;
		this.fileUploadRecordsRepository = fileUploadRecordsRepository;
		this.userRepository = userRepository;
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

		List<ProductSchedule> psList = planRepository.findBySeedingDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), NOT_IMPLEMENTEDSTATUS);
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
		c3.setValue(DateUtil.convertDateToString(new Date(), "yyyy年MM月dd日"));
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// L3
		CellInfo l3 = new CellInfo();
//		l3.setColIndex(11);
		l3.setCell("L3");
		l3.setValue(DateUtil.getWeekDayString());
		l3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo o3 = new CellInfo();
//		o3.setColIndex(14);
		o3.setCell("O3");
		o3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, l3, o3));
		row++;

		String[] tableHeader = getHeader("seeding");

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
		cellList.addAll(GenerateExcelUtil.fillBlankRow(psList.size(), "B", "O", row));
		reportInfo.setCellList(cellList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
	}

	@Override
	public byte[] downloadWateringExcel() throws YhNoDataException {

		List<ProductSchedule> psList = planRepository.findByWateringDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), IMPLEMENTEDSTATUS);

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
		c3.setValue(DateUtil.convertDateToString(new Date(), "yyyy年MM月dd日"));
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// j3
		CellInfo j3 = new CellInfo();
		j3.setCell("J3");
		j3.setValue(DateUtil.getWeekDayString());
		j3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo k3 = new CellInfo();
		k3.setCell("K3");
		k3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		k3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, j3, k3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = getHeader("watering");
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
		cellList.addAll(GenerateExcelUtil.fillBlankRow(psList.size(), "B", "K", row));
		reportInfo.setCellList(cellList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
	}

	@Override
	public byte[] downloadheadingOutExcel() throws YhNoDataException {
		List<ProductSchedule> psList = planRepository.findByHeadOutDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), IMPLEMENTEDSTATUS);

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
		c3.setValue(DateUtil.convertDateToString(new Date(), "yyyy年MM月dd日"));
		c3.setCellStyleInfo(CellStyleInfo.LEFT);

		// N3
		CellInfo n3 = new CellInfo();
		n3.setCell("N3");
		n3.setValue(DateUtil.getWeekDayString());
		n3.setCellStyleInfo(CellStyleInfo.CENTER);

		// k3
		CellInfo o3 = new CellInfo();
		o3.setCell("O3");
		o3.setValue("總盤數：" + calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, n3, o3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = getHeader("headOut");
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
		cellList.addAll(GenerateExcelUtil.fillBlankRow(psList.size(), "B", "O", row));
		reportInfo.setCellList(cellList);

		return GenerateExcelUtil.genDailyReport(reportInfo);
	}

	@Override
	public void upload(MultipartFile uploadFile, String type) throws UplaodFileException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		CustomUserDetails customUserDetails = null;
		if (auth.getPrincipal() instanceof CustomUserDetails) {
			customUserDetails = (CustomUserDetails) auth.getPrincipal();
		}

		UserEntity user = userRepository.findByUsername(customUserDetails.getUsername());

		File diskFile = UploadFileUtil.saveToTmpDisk(uploadFile);

		String[] expectedHeader = getHeader(type);
		List<?> dataList = null;
		if (expectedHeader.length > 0 && expectedHeader != null) {
			dataList = UploadFileUtil.readExcelFile(diskFile, expectedHeader);
		}
		saveData(dataList, type);
		try {
			File file = UploadFileUtil.saveToDisk(diskFile);
			FileUploadRecords fileUploadRecords = FileUploadRecords.builder().createTime(new Date())
					.description(getType(type)).fileName(file.getName()).fileSize(file.length())
					.fileType("applicatoin/excel").status(FileUploadStatus.UPLOAD).uploadedBy(user).build();
			fileUploadRecordsRepository.save(fileUploadRecords);
		} catch (FileNotFoundException | UplaodFileException e) {
			e.printStackTrace();
		}
	}

	private String getType(String type) throws UplaodFileException {
		String desc = "";
		switch (type) {
		case "seeding":
			desc = "播種日報表";
			break;
		case "watering":
			desc = "壓水日報表";
			break;
		case "headOut":
			desc = "見苗日報表";
			break;
		}
		if (desc.isEmpty()) {
			throw new UplaodFileException("日報表種類不符合");
		}
		return desc;
	}

	private void saveData(List<?> dataList, String type) {
		validateContent();
		switch (type) {
		case "seeding":
			// todo
		case "watering":
			// todo
		case "headOut":
			// todo
		}

	}

	private String validateContent() {
		return null;
		// TODO Auto-generated method stub

	}

	private String[] getHeader(String type) {
		String[] expectedHeader = null;
		switch (type) {
		case "seeding":
			expectedHeader = new String[] { "序", "工單號碼", "品名", "播種盤數", "播種片數", "播種日期", "播種人數", "播種時間起", "壓水時間止",
					"使用前克數", "使用後克數", "播數", "工時預估", "備註" };
			break;
		case "watering":
			expectedHeader = new String[] { "序", "工單號碼", "品名", "壓水盤數", "壓水人數", "壓水時間起", "壓水時間止", "暗房儲位", "暗移見日期",
					"備註" };
			break;
		case "headOut":
			expectedHeader = new String[] { "序", "工單號碼", "品名", "計畫盤數", "計畫儲位", "實際盤數", "實際儲位", "開燈確認", "水道無水確認", "實際人數",
					"實際時間起", "實際時間止", "育苗日期", "備註" };
			break;
		}
		return expectedHeader;
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

}
