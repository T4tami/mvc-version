package com.yesHealth.web.modules.planning.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.planning.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

public interface SeedGroupService {
	public Page<ProductSchedule> findBySeedingGroupForm(String startDate, String endDate, Pageable pageable);

	public byte[] downloadSeedExcel() throws YhNoDataException;

	public byte[] downloadWateringExcel() throws YhNoDataException;
}
