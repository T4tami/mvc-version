package com.yesHealth.web.modules.report.web.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.report.domain.service.QcReportService;
import com.yesHealth.web.modules.report.domain.view.QcReportView;

@Controller
@RequestMapping("report")
public class QcReportController {
	private QcReportService qcReportService;

	public QcReportController(QcReportService qcReportService) {
		this.qcReportService = qcReportService;
	}

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	@GetMapping("group-techology")
	public String getTechForm(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String hStartDate,
			@RequestParam(required = false) String hEndDate) {
		Pageable pageable = PageRequest.of(page, size);

		Page<QcReportView> qcReports = qcReportService.findByestHarvestDateBetween(hStartDate, hEndDate, pageable);
		model.addAttribute("qcReports", qcReports);
		model.addAttribute("hStartDate", hStartDate != null ? hStartDate : "");
		model.addAttribute("hEndDate", hEndDate != null ? hEndDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "module/report/inspection-form";
	}

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
