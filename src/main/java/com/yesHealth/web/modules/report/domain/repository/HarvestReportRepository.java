package com.yesHealth.web.modules.report.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.report.domain.entity.HarvestReport;

public interface HarvestReportRepository extends JpaRepository<HarvestReport, Long> {

}
