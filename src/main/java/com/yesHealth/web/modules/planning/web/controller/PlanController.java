package com.yesHealth.web.modules.planning.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.planning.domain.service.StockService;
import com.yesHealth.web.modules.planning.web.views.CreatePlansForm;
import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.product.domain.service.ProductService;

@Controller
@RequestMapping("/production")
public class PlanController {

	private PlanService planService;
	private StockService stockService;
	private ProductService productService;

	public PlanController(PlanService planService, StockService stockService, ProductService productService) {
		this.planService = planService;
		this.stockService = stockService;
		this.productService = productService;
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

	@GetMapping("create-form")
	public String createForm(HttpSession session, Model model) {
		List<Stock> stockList = stockService.findAll();
		List<Product> products = productService.findAll();
		session.setAttribute("stockList", stockList);
		session.setAttribute("products", products);
		CreatePlansForm createPlansForm = new CreatePlansForm();
		model.addAttribute("createPlansForm", createPlansForm);
		return "/module/plans/create-form";
	}

	@PostMapping("create-plans")
	public String createPlan(@ModelAttribute @Valid CreatePlansForm createPlansForm, Model model,
			BindingResult result) {

		if (result.hasErrors()) {
			model.addAttribute("createPlansForm", createPlansForm);
			return "module/plans/create-form"; // 返回失敗的創建表單
		}
		Map<String, Object> businessErrors = planService.validateCreatePlan(createPlansForm);
		if (!businessErrors.isEmpty()) {
			model.addAttribute("businessErrors", businessErrors);
			model.addAttribute("createPlansForm", createPlansForm);
			return "module/plans/create-form"; // 返回失敗的創建表單
		}
		model.addAttribute("createPlansForm", createPlansForm);
		return "redirect:/module/plans/plan";
	}

}
