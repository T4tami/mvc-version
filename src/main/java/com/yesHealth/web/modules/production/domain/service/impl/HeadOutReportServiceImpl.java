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
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.production.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.production.domain.model.DailyReportContent;
import com.yesHealth.web.modules.production.domain.model.FileSrcType;
import com.yesHealth.web.modules.production.domain.model.PlanStatus;
import com.yesHealth.web.modules.production.domain.respository.PlanRepository;
import com.yesHealth.web.modules.production.domain.respository.StockRepository;
import com.yesHealth.web.modules.production.domain.service.DailyReportService;
import com.yesHealth.web.modules.report.domain.entity.HeadOutReport;
import com.yesHealth.web.modules.report.domain.repository.HeadOutReportRepository;
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

@Service("headOutReportService")
public class HeadOutReportServiceImpl implements DailyReportService<HeadOutReport> {
	private PlanRepository planRepository;
	private UserRepository userRepository;
	private FileUploadRecordsRepository fileUploadRecordsRepository;
	private HeadOutReportRepository headOutReportRepository;
	private StockRepository stockRepository;

	public HeadOutReportServiceImpl(PlanRepository planRepository, UserRepository userRepository,
			FileUploadRecordsRepository fileUploadRecordsRepository, HeadOutReportRepository headOutReportRepository,
			StockRepository stockRepository) {
		this.planRepository = planRepository;
		this.userRepository = userRepository;
		this.fileUploadRecordsRepository = fileUploadRecordsRepository;
		this.headOutReportRepository = headOutReportRepository;
		this.stockRepository = stockRepository;
	}

	@Override
	public Page<HeadOutReport> getForm(String startDateStr, String endDateStr, Pageable pageable) {
		Date formateStartDate = startDateStr == null ? DateUtil.getStartOfNextWeek()
				: DateUtil.convertStringToDate(startDateStr, "yyyy-MM-dd");
		Date formateEndDate = endDateStr == null ? DateUtil.getEndOfNextWeek()
				: DateUtil.convertStringToDate(endDateStr, "yyyy-MM-dd");
		return headOutReportRepository.findByWorkDateBetween(formateStartDate, formateEndDate, pageable);
	}

	@Override
	public byte[] downloadDailyExcel() throws YhNoDataException {
		List<ProductSchedule> psList = planRepository.findByHeadOutDateBetweenAndStatus(DateUtil.getStartOfDay(),
				DateUtil.getEndOfDay(), PlanStatus.NOT_IMPLEMENTED.getStatus());

		if (psList == null || psList.isEmpty()) {
			throw new YhNoDataException("無當日暗移見計畫");
		}

		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setSheetName(DailyReportContent.HeadOut.getType());
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
		reportHeader.setValue(DailyReportContent.HeadOut.getType());
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
		o3.setValue("總盤數：" + DailyReportService.calculateTotalBoardCount(psList) + "盤");
		o3.setCellStyleInfo(CellStyleInfo.RIGHT);
		cellList.addAll(Arrays.asList(b3, c3, n3, o3));
		row++;

		List<CellInfo> thList = new ArrayList<>();
		String[] tableHeader = DailyReportContent.HeadOut.getHeader();
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
		headOutReportRepository.saveAll(mapDataListToHeadOutReportList(dataList, fileUploadRecords));

	}

	private Iterable<HeadOutReport> mapDataListToHeadOutReportList(List<Map<String, String>> dataList,
			FileUploadRecords fileUploadRecords) {
		List<HeadOutReport> headOutReport = dataList.stream().map(map -> mapToHeadOutReport(map, fileUploadRecords))
				.toList();
		return headOutReport;
	}

	private HeadOutReport mapToHeadOutReport(Map<String, String> map, FileUploadRecords fileUploadRecords) {
		HeadOutReport headOutReport = new HeadOutReport();
		String[] header = DailyReportContent.Water.getHeader();
		headOutReport.setSeqNo(map.get(header[0]));

		String manuNo = map.get(header[1]);
		if (manuNo != null) {
			ProductSchedule ps = planRepository.findByManuNo(manuNo).get(0);
			headOutReport.setPs(ps);
		}

		headOutReport.setBoardCount(Long.valueOf(map.get(header[5])));

		Stock stock = stockRepository.findByPosition(map.get(header[6]));
		headOutReport.setStock(stock);
		headOutReport.setLightStatus(header[7]);
		headOutReport.setWaterChannelStatus(header[8]);
		headOutReport.setWorkDate(DateUtil.convertStringToDate(map.get(header[4]), "dd-MM月-yyyy"));
		headOutReport.setWorkMan(Long.valueOf(map.get(header[9])));
		headOutReport.setWorkTimeStart(LocalDateTime.parse(map.get(header[10]))); // 假設格式為 "yyyy-MM-dd'T'HH:mm:ss"
		headOutReport.setWorkTimeEnd(LocalDateTime.parse(map.get(header[11])));
		headOutReport.setRemark(map.get(header[13]));
		headOutReport.setSrcType(FileSrcType.FILE.toString());
		headOutReport.setFileUploadRecords(fileUploadRecords);

		return headOutReport;
	}

	@Override
	public void update(HeadOutReport element) {
		// TODO Auto-generated method stub

	}

}
