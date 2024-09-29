package com.yesHealth.web.modules.planning.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.planning.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.planning.domain.service.SeedGroupService;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

@Controller
@RequestMapping("/production")
public class SeedGroupController {
	private SeedGroupService seedGroupService;

	public SeedGroupController(SeedGroupService seedGroupService) {
		this.seedGroupService = seedGroupService;
	}

	@GetMapping("group-seed")
	public String getIndex(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductSchedule> plans = seedGroupService.findBySeedingGroupForm(startDate, endDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("startDate", startDate != null ? startDate : "");
		model.addAttribute("endDate", endDate != null ? endDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "module/production/group-seed-info.html";
	}

	@GetMapping("download/seedDailyExcel")
	public void downloadSeedExcel(HttpServletResponse response) throws YhNoDataException {

		byte[] excelData = null;
		excelData = seedGroupService.downloadSeedExcel();
		String fileName = "播種日報表.xlsx";
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
	}
}
