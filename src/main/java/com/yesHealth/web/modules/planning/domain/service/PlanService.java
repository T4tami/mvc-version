package com.yesHealth.web.modules.planning.domain.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.planning.web.views.CreatePlansForm;
import com.yesHealth.web.modules.planning.web.views.EditPlanForm;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

public interface PlanService {
	public Page<ProductSchedule> findAll(Pageable pageable);

	public Page<ProductSchedule> findByHarvestDateBetweenAndStatus(String hStartDate, String hEndDate, String Status,
			Pageable pageable);

	public Map<String, Object> validateCreatePlan(CreatePlansForm createPlansForm);

	public EditPlanForm findbyId(Long planId);

	public void saveProductSchedule(CreatePlansForm createPlansForm);

	public void updateProductSchedule(Long id, EditPlanForm editPlanForm);
}
