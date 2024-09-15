package com.yesHealth.web.modules.report.domain.service;

import java.util.Date;

public interface QcReportService {
	byte[] exportToExcel(Date startDate, Date endDate);
}
