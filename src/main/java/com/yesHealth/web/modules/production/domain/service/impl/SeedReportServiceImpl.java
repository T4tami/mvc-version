package com.yesHealth.web.modules.production.domain.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.yesHealth.web.modules.production.domain.model.DailyReportContent;
import com.yesHealth.web.modules.production.domain.model.FileSrcType;
import com.yesHealth.web.modules.production.domain.model.PlanStatus;
import com.yesHealth.web.modules.production.domain.respository.PlanRepository;
import com.yesHealth.web.modules.production.domain.service.DailyReportService;
import com.yesHealth.web.modules.report.domain.entity.SeedReport;
import com.yesHealth.web.modules.report.domain.repository.SeedReportRepository;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.repository.UserRepository;
import com.yesHealth.web.modules.util.DateUtil;
import com.yesHealth.web.modules.util.GenerateExcelUtil;
import com.yesHealth.web.modules.util.UploadFileUtil;
import com.yesHealth.web.modules.util.entity.FileUploadRecords;
import com.yesHealth.web.modules.util.entity.FileUploadStatus;
import com.yesHealth.web.modules.util.exception.UplaodFileException;
import com.yesHealth.web.modules.util.model.CellInfo;
import com.yesHealth.web.modules.util.model.CellStyleInfo;
import com.yesHealth.web.modules.util.model.ExcelCell;
import com.yesHealth.web.modules.util.model.MergeCell;
import com.yesHealth.web.modules.util.model.ReportInfo;
import com.yesHealth.web.modules.util.repository.FileUploadRecordsRepository;

@Service("seedReportService")
public class SeedReportServiceImpl implements DailyReportService<SeedReport> {
	private PlanRepository planRepository;
	private UserRepository userRepository;
	private FileUploadRecordsRepository fileUploadRecordsRepository;
	private SeedReportRepository seedReportRepository;

	public SeedReportServiceImpl(PlanRepository planRepository, UserRepository userRepository,
			FileUploadRecordsRepository fileUploadRecordsRepository, SeedReportRepository seedReportRepository) {
		this.planRepository = planRepository;
		this.userRepository = userRepository;
		this.fileUploadRecordsRepository = fileUploadRecordsRepository;
		this.seedReportRepository = seedReportRepository;
	}

	@Override
	public Page<SeedReport> getForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return seedReportRepository.findByWorkDateBetween(formateStartDate, formateEndDate, pageable);
	}

	@Override
	public byte[] downloadDailyExcel() throws YhNoDataException {
		List<ProductSchedule> psList = planRepository.findBySeedingDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), PlanStatus.NOT_IMPLEMENTED.getStatus());
		int row = 0;
		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日播種計畫");
		}
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName(DailyReportContent.Seed.getType());
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
		reportHeader.setValue(DailyReportContent.Seed.getType());
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
		o3.setValue("總盤數：" + DailyReportService.calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, l3, o3));
		row++;

		String[] tableHeader = DailyReportContent.Seed.getHeader();

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
	public void uploadDailyExcel(MultipartFile uploadFile) throws YhNoDataException, UplaodFileException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		CustomUserDetails customUserDetails = null;
		if (auth.getPrincipal() instanceof CustomUserDetails) {
			customUserDetails = (CustomUserDetails) auth.getPrincipal();
		}

		UserEntity user = userRepository.findByUsername(customUserDetails.getUsername());

		File diskFile = UploadFileUtil.saveToTmpDisk(uploadFile);

		String[] expectedHeader = DailyReportContent.Seed.getHeader();
		List<Map<String, String>> dataList = null;
		if (expectedHeader.length > 0 && expectedHeader != null) {
			dataList = UploadFileUtil.readExcelFile(diskFile, expectedHeader);
		}
		FileUploadRecords fileUploadRecords = null;
		try {
			File file = UploadFileUtil.saveToDisk(diskFile);
			fileUploadRecords = FileUploadRecords.builder().createTime(new Date())
					.description(DailyReportContent.Seed.getType()).fileName(file.getName()).fileSize(file.length())
					.fileType(DailyReportContent.Seed.getFileType()).status(FileUploadStatus.UPLOAD).uploadedBy(user)
					.build();
			fileUploadRecordsRepository.save(fileUploadRecords);
		} catch (FileNotFoundException | UplaodFileException e) {
			e.printStackTrace();
		}
		seedReportRepository.saveAll(mapDataListToSeedReportList(dataList, fileUploadRecords));
	}

	@Override
	public void update(SeedReport seedReport) {
		// TODO Auto-generated method stub

	}

	private Iterable<SeedReport> mapDataListToSeedReportList(List<Map<String, String>> dataList,
			FileUploadRecords fileUploadRecords) {
		List<SeedReport> seedReports = dataList.stream().map(map -> mapToSeedReport(map, fileUploadRecords)).toList();
		return seedReports;
	}

	private SeedReport mapToSeedReport(Map<String, String> map, FileUploadRecords fileUploadRecords) {
		SeedReport seedReport = new SeedReport();
		String[] header = DailyReportContent.Seed.getHeader();
		seedReport.setSeqNo(map.get(header[0]));

		String manuNo = map.get(header[1]);
		if (manuNo != null) {
			ProductSchedule ps = planRepository.findByManuNo(manuNo).get(0);
			seedReport.setPs(ps);
		}

		seedReport.setBoardCount(Long.valueOf(map.get(header[3])));
		seedReport.setBoardPiece(Long.valueOf(map.get(header[4])));

		// 使用 Date
		seedReport.setWorkDate(DateUtil.convertStringToDate(map.get(header[5]), "dd-MM月-yyyy"));

		seedReport.setWorkMan(Math.round(Double.parseDouble(map.get(header[6]))));

		seedReport.setWorkTimeStart(map.get(header[7])); // 假設格式為 "yyyy-MM-dd'T'HH:mm:ss"
		seedReport.setWorkTimeEnd(map.get(header[8]));

		seedReport.setGramBeforeUse(Double.valueOf(map.get(header[9])));
		seedReport.setGramAfterUse(Double.valueOf(map.get(header[10])));
		seedReport.setCountPerHole((int) Math.round(Double.parseDouble(map.get(header[11]))));
		seedReport.setEstWorkTime(Double.valueOf(map.get(header[12])));
		seedReport.setRemark(map.get(header[13]));
		seedReport.setSrcType(FileSrcType.FILE.toString());

		seedReport.setFileUploadRecords(fileUploadRecords);

		return seedReport;
	}

}
