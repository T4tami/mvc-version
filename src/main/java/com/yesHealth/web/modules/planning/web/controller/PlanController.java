package com.yesHealth.web.modules.planning.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

@Controller
@RequestMapping("/production")
public class PlanController {

	private PlanService planService;

	public PlanController(PlanService planService) {
		this.planService = planService;
	}

	@GetMapping("planing")
	public String findAll(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String hStartDate,
			@RequestParam(required = false) String hEndDate) {
		Pageable pageable = PageRequest.of(page, size);

		Page<ProductSchedule> plans = planService.findByestHarvestDateBetween(hStartDate, hEndDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("hStartDate", hStartDate != null ? hStartDate : "");
		model.addAttribute("hEndDate", hEndDate != null ? hEndDate : "");
		model.addAttribute("size", size != null ? size : "");
		return "/module/plans/plan";
	}

}
