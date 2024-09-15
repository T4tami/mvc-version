package com.yesHealth.web.modules.report.domain.service;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.report.domain.view.QcReportView;

public interface QcReportService {
	byte[] exportToExcel(Date startDate, Date endDate);

	Page<QcReportView> findByestHarvestDateBetween(String hStartDate, String hEndDate, Pageable pageable);
}
