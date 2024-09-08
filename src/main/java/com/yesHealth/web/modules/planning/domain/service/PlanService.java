package com.yesHealth.web.modules.planning.domain.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

public interface PlanService {
	public Page<ProductSchedule> findAll(Pageable pageable);

	public Page<ProductSchedule> findByestHarvestDateBetween(String hStartDate, String hEndDate,
			Pageable pageable);
}
