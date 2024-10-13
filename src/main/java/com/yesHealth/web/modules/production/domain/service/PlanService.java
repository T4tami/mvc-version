package com.yesHealth.web.modules.production.domain.service;

import java.text.ParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.production.domain.exception.YhValidateException;
import com.yesHealth.web.modules.production.web.views.CreatePlansForm;
import com.yesHealth.web.modules.production.web.views.EditPlanForm;

public interface PlanService {
	public Page<ProductSchedule> findAll(Pageable pageable);

	public Page<ProductSchedule> findByHarvestDateBetweenAndStatus(String hStartDate, String hEndDate, String Status,
			Pageable pageable);

	public EditPlanForm findbyId(Long planId);

	public void saveProductSchedule(CreatePlansForm createPlansForm) throws ParseException, YhValidateException;

	public void updateProductSchedule(Long id, EditPlanForm editPlanForm);
}
