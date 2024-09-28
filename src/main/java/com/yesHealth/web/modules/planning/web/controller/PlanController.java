package com.yesHealth.web.modules.planning.web.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.planning.domain.service.StockService;
import com.yesHealth.web.modules.planning.web.views.CreatePlansForm;
import com.yesHealth.web.modules.planning.web.views.EditPlanForm;
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
	private static final String BASE_FILEPATH = "/module/plans/";
	private static final String REDIRECT_PREFIX = "redirect:/production/plans/";

	public PlanController(PlanService planService, StockService stockService, ProductService productService) {
		this.planService = planService;
		this.stockService = stockService;
		this.productService = productService;
	}

	@GetMapping("plans/not-implemented")
	public String findAll(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String hStartDate,
			@RequestParam(required = false) String hEndDate) {
		Pageable pageable = PageRequest.of(page, size);

		Page<ProductSchedule> plans = planService.findByestHarvestDateBetween(hStartDate, hEndDate, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("hStartDate", hStartDate != null ? hStartDate : "");
		model.addAttribute("hEndDate", hEndDate != null ? hEndDate : "");
		model.addAttribute("size", size != null ? size : "");
		return BASE_FILEPATH + "plan";
	}

	@GetMapping("plans/not-implemented/create-form")
	public String createForm(HttpSession session, Model model) {
		List<Stock> stockList = stockService.findAll();
		List<Product> products = productService.findAll();
		session.setAttribute("stockList", stockList);
		session.setAttribute("products", products);
		CreatePlansForm createPlansForm = new CreatePlansForm();
		model.addAttribute("createPlansForm", createPlansForm);
		return BASE_FILEPATH + "create-form";
	}

	@PostMapping("plans/not-implemented")
	public String createPlan(@ModelAttribute @Valid CreatePlansForm createPlansForm, Model model,
			BindingResult result) {

		if (result.hasErrors()) {
			model.addAttribute("createPlansForm", createPlansForm);
			return "module/plans/create-form"; // 返回失敗的創建表單
		}
		Map<String, Object> businessErrors = planService.validateCreatePlan(createPlansForm);
		ArrayList<?> list = null;
		if (businessErrors.containsKey("globalError")) {
			list = (ArrayList<?>) businessErrors.get("globalError");
			if (!list.isEmpty()) {
				model.addAttribute("globalError", list);
				model.addAttribute("createPlansForm", createPlansForm);
				return BASE_FILEPATH + "create-form"; // 返回失敗的創建表單
			}
		}

		planService.saveProductSchedule(createPlansForm);
		return REDIRECT_PREFIX + "not-implemented";
	}

	@GetMapping("plans/not-implemented/edit-form")
	@SuppressWarnings("unchecked")
	public String editPlan(@RequestParam(required = false) Long planId, HttpSession session, Model model) {
		List<Product> products = (List<Product>) session.getAttribute("products");
		if (products == null) {
			products = productService.findAll();
			session.setAttribute("products", products);
		}
		List<Stock> stockList = (List<Stock>) session.getAttribute("stockList");
		if (stockList == null) {
			stockList = stockService.findAll();
			session.setAttribute("stockList", stockList);
		}
		model.addAttribute("plan", planService.findbyId(planId));
		return BASE_FILEPATH + "edit-form";
	}

	@PostMapping("plans/not-implemented/{id}")
	public String updateProduction(@PathVariable Long id, @ModelAttribute EditPlanForm editPlanForm,
			BindingResult result) {
		planService.updateProductSchedule(id, editPlanForm);
		return REDIRECT_PREFIX + "not-implemented"; // 更新後重定向到列表頁面
	}

	@GetMapping("plans/implemented")
	public String getManuInfo(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductSchedule> ps = planService.findAll(pageable);
		model.addAttribute("productSchedule", ps);
		return BASE_FILEPATH + "/manufacture-info"; // 更新後重定向到列表頁面
	}
}
