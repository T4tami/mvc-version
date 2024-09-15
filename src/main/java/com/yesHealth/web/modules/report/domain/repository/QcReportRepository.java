package com.yesHealth.web.modules.report.domain.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.report.domain.view.QcReportView;

public interface QcReportRepository extends JpaRepository<QcReportView, Long> {
	List<QcReportView> findByActTransDateBetween(Date startDate, Date endDate);

	Page<QcReportView> findByEstHarvestDateBetween(Date startDate, Date endDate, Pageable pageable);

}
