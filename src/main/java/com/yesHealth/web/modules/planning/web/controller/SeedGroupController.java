package com.yesHealth.web.modules.planning.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
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

import com.yesHealth.web.modules.planning.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.planning.domain.service.SeedGroupService;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.util.DateUtil;
import com.yesHealth.web.modules.util.exception.UplaodFileException;

@Controller
@RequestMapping("/production")
public class SeedGroupController {
	private SeedGroupService seedGroupService;

	public SeedGroupController(SeedGroupService seedGroupService) {
		this.seedGroupService = seedGroupService;
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

	@GetMapping("group-seed/tabs/seeding")
	public String getSeedingTab(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductSchedule> plans = seedGroupService.findBySeedingGroupForm(startDate, endDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "module/production/group-seed/seed-tab";
	}

	@GetMapping("group-seed/tabs/watering")
	public String getWateringTab(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductSchedule> plans = seedGroupService.getWateringForm(startDate, endDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "module/production/group-seed/watering-tab";
	}

	@GetMapping("group-seed/tabs/darkroom-moving-out")
	public String getDarkMovingOutTab(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductSchedule> plans = seedGroupService.getHeadOutForm(startDate, endDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "module/production/group-seed/darkroom-moving-out-tab";
	}

	@GetMapping("download/seedDailyExcel")
	public String downloadSeedExcel(HttpServletResponse response, RedirectAttributes redirectAttributes) {

		byte[] excelData = null;
		try {
			excelData = seedGroupService.downloadSeedExcel();
		} catch (YhNoDataException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "下載失敗：" + e.getMessage());
			return "redirect:/production/group-seed/tabs/seeding";
		}
		String dateStr = DateUtil.convertDateToString(new Date(), "yyyyMMdd");
		String fileName = dateStr + "_播種日報表.xlsx";
		try {
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			response.getOutputStream().write(excelData);
			response.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping("download/wateringDailyExcel")
	public String downloadWateringExcel(HttpServletResponse response, RedirectAttributes redirectAttributes) {

		byte[] excelData = null;
		try {
			excelData = seedGroupService.downloadWateringExcel();
		} catch (YhNoDataException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "下載失敗：" + e.getMessage());
			return "redirect:/production/group-seed/tabs/watering";
		}
		String dateStr = DateUtil.convertDateToString(new Date(), "yyyyMMdd");
		String fileName = dateStr + "_壓水日報表.xlsx";
		try {
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			response.getOutputStream().write(excelData);
			response.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping("download/headingOutDailyExcel")
	public String downloadheadingOutDailyExcel(HttpServletResponse response, RedirectAttributes redirectAttributes) {

		byte[] excelData = null;
		try {
			excelData = seedGroupService.downloadheadingOutExcel();
		} catch (YhNoDataException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "下載失敗：" + e.getMessage());
			return "redirect:/production/group-seed/tabs/watering";
		}
		String dateStr = DateUtil.convertDateToString(new Date(), "yyyyMMdd");
		String fileName = dateStr + "_暗移見日報表.xlsx";
		try {
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			response.getOutputStream().write(excelData);
			response.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("upload/dailyReport/{type}")
	public String uploadDailyReport(@RequestParam("uploadFile") MultipartFile uploadFile, @PathVariable String type) {
		try {
			seedGroupService.upload(uploadFile, type);
		} catch (UplaodFileException e) {

		}
		return "redirect:/production/group-seed/tabs/seeding";
	}
}
