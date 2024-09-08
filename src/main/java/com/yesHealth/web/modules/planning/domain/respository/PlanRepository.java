package com.yesHealth.web.modules.planning.domain.respository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

public interface PlanRepository extends JpaRepository<ProductSchedule, Long> {
	Page<ProductSchedule> findAll(Pageable pageable);

	Page<ProductSchedule> findByEstHarvestDateBetween(Date startDate, Date endDate, Pageable pageable);
}
