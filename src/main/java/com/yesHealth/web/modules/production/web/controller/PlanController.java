package com.yesHealth.web.modules.production.web.controller;

import java.text.ParseException;
import java.util.List;

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

import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.product.domain.service.ProductService;
import com.yesHealth.web.modules.production.domain.exception.YhValidateException;
import com.yesHealth.web.modules.production.domain.service.PlanService;
import com.yesHealth.web.modules.production.domain.service.StockService;
import com.yesHealth.web.modules.production.web.views.CreatePlansForm;
import com.yesHealth.web.modules.production.web.views.EditPlanForm;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/production")
@Slf4j
public class PlanController {

	private PlanService planService;
	private StockService stockService;
	private ProductService productService;
	private static final String BASE_FILEPATH = "/module/plans/";
	private static final String REDIRECT_PREFIX = "redirect:/production/plans/";
	private static final String NOT_IMPLEMENTEDSTATUS = "0";
	private static final String IMPLEMENTEDSTATUS = "1";

	public PlanController(PlanService planService, StockService stockService, ProductService productService) {
		this.planService = planService;
		this.stockService = stockService;
		this.productService = productService;
	}

	@GetMapping("plans/not-implemented")
	public String findNotImplementedPlan(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String hStartDate,
			@RequestParam(required = false) String hEndDate) {
		Pageable pageable = PageRequest.of(page, size);

		Page<ProductSchedule> plans = planService.findByHarvestDateBetweenAndStatus(hStartDate, hEndDate,
				NOT_IMPLEMENTEDSTATUS, pageable);
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
		try {
			planService.saveProductSchedule(createPlansForm);
		} catch (ParseException e) {
			log.error("saveProductSchedule() CATCHS A Exception:  " + e.getMessage());
			e.printStackTrace();
		} catch (YhValidateException e) {
			log.error("saveProductSchedule() CATCHS A Exception:  " + e.getMessage());
			model.addAttribute("globalError", e.getBusinessErrors());
			model.addAttribute("createPlansForm", createPlansForm);
			return BASE_FILEPATH + "create-form";
		}

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

	// ===============================製程管理===========================================
	@GetMapping("plans/implemented")
	public String findImplementedPlan(Model model, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) String hStartDate,
			@RequestParam(required = false) String hEndDate) {
		Pageable pageable = PageRequest.of(page, size);

		Page<ProductSchedule> plans = planService.findByHarvestDateBetweenAndStatus(hStartDate, hEndDate,
				IMPLEMENTEDSTATUS, pageable);
		model.addAttribute("plans", plans);
		model.addAttribute("hStartDate", hStartDate != null ? hStartDate : "");
		model.addAttribute("hEndDate", hEndDate != null ? hEndDate : "");
		model.addAttribute("size", size != null ? size : "");
		return BASE_FILEPATH + "plan-implemented";
	}

	@GetMapping("plans/implemented/edit-form")
	@SuppressWarnings("unchecked")
	public String editImplementedPlan(@RequestParam(required = false) Long planId, HttpSession session, Model model) {
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
		return BASE_FILEPATH + "implemented/edit-form";
	}

	@PostMapping("plans/implemented/{id}")
	public String updateImplementedPlan(@PathVariable Long id, @ModelAttribute EditPlanForm editPlanForm,
			BindingResult result) {
		planService.updateProductSchedule(id, editPlanForm);
		return REDIRECT_PREFIX + "not-implemented"; // 更新後重定向到列表頁面
	}
}
