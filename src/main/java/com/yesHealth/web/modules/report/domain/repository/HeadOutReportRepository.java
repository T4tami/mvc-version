package com.yesHealth.web.modules.report.domain.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.report.domain.entity.HeadOutReport;

public interface HeadOutReportRepository extends JpaRepository<HeadOutReport, Long> {
	Page<HeadOutReport> findByWorkDateBetween(Date startDate, Date endDate, Pageable pageable);
}
