package com.yesHealth.web.modules.production.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yesHealth.web.modules.production.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.production.domain.service.DailyReportService;
import com.yesHealth.web.modules.report.domain.entity.HeadOutReport;
import com.yesHealth.web.modules.report.domain.entity.SeedReport;
import com.yesHealth.web.modules.report.domain.entity.WaterReport;
import com.yesHealth.web.modules.util.exception.UplaodFileException;
import com.yesHealth.web.modules.util.service.DateUtil;

@Controller
@RequestMapping("/production")
public class SeedGroupController {
	@Qualifier("seedReportService")
	private DailyReportService<SeedReport> seedReportService;
	@Qualifier("waterReportService")
	private DailyReportService<WaterReport> waterReportService;
	@Qualifier("headOutReportService")
	private DailyReportService<HeadOutReport> headOutReportService;

	public SeedGroupController(DailyReportService<SeedReport> seedReportService,
			DailyReportService<WaterReport> waterReportService,
			DailyReportService<HeadOutReport> headOutReportService) {
		super();
		this.seedReportService = seedReportService;
		this.waterReportService = waterReportService;
		this.headOutReportService = headOutReportService;
	}

	@GetMapping("/group-seed")
	public String getIndexTab(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "redirect:/production/group-seed/tabs/seeding?startDate=" + startDate + "&endDate=" + endDate;
	}

	@GetMapping("group-seed/tabs/{tabType}")
	public String getTab(Model model, @PathVariable String tabType, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Pageable pageable = PageRequest.of(page, size);

		// 這裡的屬性可以統一設置
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		switch (tabType) {
		case "seeding":
			Page<SeedReport> seedReports = seedReportService.getForm(startDate, endDate, pageable);
			model.addAttribute("seedReports", seedReports);
			return "module/production/group-seed/seed-tab";
		case "watering":
			Page<WaterReport> waterReports = waterReportService.getForm(startDate, endDate, pageable);
			model.addAttribute("waterReports", waterReports);
			return "module/production/group-seed/watering-tab";
		case "head-out":
			Page<HeadOutReport> headOutReports = headOutReportService.getForm(startDate, endDate, pageable);
			model.addAttribute("headOutReports", headOutReports);
			return "module/production/group-seed/head-out-tab";
		default:
			// 可以根據需要處理未知類型的情況
			return "redirect:/error"; // 假設有一個錯誤頁面
		}
	}

	@GetMapping("download/{reportType}Excel")
	public String downloadExcel(@PathVariable String reportType, HttpServletResponse response,
			RedirectAttributes redirectAttributes) {
		byte[] excelData;
		String fileName;

		try {
			switch (reportType) {
			case "seedDaily":
				excelData = seedReportService.downloadDailyExcel();
				fileName = DateUtil.convertDateToString(new Date(), "yyyyMMdd") + "_播種日報表.xlsx";
				break;
			case "wateringDaily":
				excelData = waterReportService.downloadDailyExcel();
				fileName = DateUtil.convertDateToString(new Date(), "yyyyMMdd") + "_壓水日報表.xlsx";
				break;
			case "headingOutDaily":
				excelData = headOutReportService.downloadDailyExcel();
				fileName = DateUtil.convertDateToString(new Date(), "yyyyMMdd") + "_暗移見日報表.xlsx";
				break;
			default:
				redirectAttributes.addFlashAttribute("errorMessage", "未知的報表類型");
				return "redirect:/production/group-seed/tabs/" + reportType;
			}
		} catch (YhNoDataException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "下載失敗：" + e.getMessage());
			return "redirect:/production/group-seed/tabs/" + reportType; // 根據報表類型進行重定向
		}

		try {
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

			response.getOutputStream().write(excelData);
			response.getOutputStream().flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@PostMapping("upload/dailyReport/{reportType}")
	public String uploadDailyReport(@RequestParam("uploadFile") MultipartFile uploadFile,
			@PathVariable String reportType, RedirectAttributes redirectAttributes) {
		try {

			switch (reportType) {
			case "seed":
				seedReportService.uploadDailyExcel(uploadFile);
				break;
			case "water":
				waterReportService.uploadDailyExcel(uploadFile);
				break;
			case "head-out":
				headOutReportService.uploadDailyExcel(uploadFile);
				break;
			default:
				redirectAttributes.addFlashAttribute("errorMessage", "未知的報表類型");
				return "redirect:/production/group-seed/tabs/" + reportType;
			}
		} catch (UplaodFileException e) {

		} catch (YhNoDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/production/group-seed";
	}
}
