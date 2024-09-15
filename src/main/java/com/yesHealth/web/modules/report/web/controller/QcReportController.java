package com.yesHealth.web.modules.report.web.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.report.domain.service.QcReportService;

@Controller
public class QcReportController {
	private QcReportService qcReportService;

	public QcReportController(QcReportService qcReportService) {
		this.qcReportService = qcReportService;
	}

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	@GetMapping("/exportExcel")
	public void exportToExcel(@RequestParam String startDate, @RequestParam String endDate,
			HttpServletResponse response) {

		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

		byte[] excelData = null;
		try {
			excelData = qcReportService.exportToExcel(formatter.parse(startDate), formatter.parse(endDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=巡檢報表.xlsx");
		try {
			response.getOutputStream().write(excelData);
			response.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
