package com.yesHealth.web.modules.production.domain.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.yesHealth.web.modules.report.domain.entity.WaterReport;
import com.yesHealth.web.modules.report.domain.repository.WaterReportRepository;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.repository.UserRepository;
import com.yesHealth.web.modules.util.entity.FileUploadRecords;
import com.yesHealth.web.modules.util.entity.FileUploadStatus;
import com.yesHealth.web.modules.util.exception.UplaodFileException;
import com.yesHealth.web.modules.util.model.CellInfo;
import com.yesHealth.web.modules.util.model.CellStyleInfo;
import com.yesHealth.web.modules.util.model.ExcelCell;
import com.yesHealth.web.modules.util.model.MergeCell;
import com.yesHealth.web.modules.util.model.ReportInfo;
import com.yesHealth.web.modules.util.repository.FileUploadRecordsRepository;
import com.yesHealth.web.modules.util.service.DateUtil;
import com.yesHealth.web.modules.util.service.GenerateExcelUtil;
import com.yesHealth.web.modules.util.service.UploadFileUtil;

@Service("waterReportService")
public class WaterReportServiceImpl implements DailyReportService<WaterReport> {
	private PlanRepository planRepository;
	private UserRepository userRepository;
	private FileUploadRecordsRepository fileUploadRecordsRepository;
	private WaterReportRepository waterReportRepository;

	public WaterReportServiceImpl(PlanRepository planRepository, UserRepository userRepository,
			FileUploadRecordsRepository fileUploadRecordsRepository, WaterReportRepository waterReportRepository) {
		this.planRepository = planRepository;
		this.userRepository = userRepository;
		this.fileUploadRecordsRepository = fileUploadRecordsRepository;
		this.waterReportRepository = waterReportRepository;
	}

	@Override
	public Page<WaterReport> getForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return waterReportRepository.findByWorkDateBetween(formateStartDate, formateEndDate, pageable);
	}

	@Override
	public byte[] downloadDailyExcel() throws YhNoDataException {
		List<ProductSchedule> psList = planRepository.findByWateringDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), PlanStatus.NOT_IMPLEMENTED.getStatus());

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
		k3.setValue("總盤數：" + DailyReportService.calculateTotalBoardCount(psList) + "盤");
		k3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, j3, k3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = DailyReportContent.Water.getHeader();
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
		waterReportRepository.saveAll(mapDataListToWaterReportList(dataList, fileUploadRecords));
	}

	@Override
	public void update(WaterReport waterReport) {
		// TODO Auto-generated method stub

	}

	private Iterable<WaterReport> mapDataListToWaterReportList(List<Map<String, String>> dataList,
			FileUploadRecords fileUploadRecords) {
		List<WaterReport> waterReport = dataList.stream().map(map -> mapToWaterReport(map, fileUploadRecords)).toList();
		return waterReport;
	}

	private WaterReport mapToWaterReport(Map<String, String> map, FileUploadRecords fileUploadRecords) {
		WaterReport waterReport = new WaterReport();
		String[] header = DailyReportContent.Water.getHeader();
		waterReport.setSeqNo(map.get(header[0]));

		String manuNo = map.get(header[1]);
		if (manuNo != null) {
			ProductSchedule ps = planRepository.findByManuNo(manuNo).get(0);
			waterReport.setPs(ps);
		}
		waterReport.setBoardCount(Long.valueOf(map.get(header[3])));
		waterReport.setWorkDate(new Date());
		waterReport.setWorkMan(Long.valueOf(map.get(header[4])));
		waterReport.setWorkTimeStart(LocalDateTime.parse(map.get(header[5])));
		waterReport.setWorkTimeEnd(LocalDateTime.parse(map.get(header[6])));
		waterReport.setDarkRoomPosition(header[7]);

		waterReport.setRemark(map.get(header[9]));
		waterReport.setSrcType(FileSrcType.FILE.toString());

		waterReport.setFileUploadRecords(fileUploadRecords);

		return waterReport;
	}

}
