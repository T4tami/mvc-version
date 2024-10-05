package com.yesHealth.web.modules.planning.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.yesHealth.web.modules.planning.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.util.exception.UplaodFileException;

public interface SeedGroupService {
	public Page<ProductSchedule> findBySeedingGroupForm(String startDate, String endDate, Pageable pageable);

	public byte[] downloadSeedExcel() throws YhNoDataException;

	public byte[] downloadWateringExcel() throws YhNoDataException;

	public Page<ProductSchedule> getWateringForm(String startDate, String endDate, Pageable pageable);

	public Page<ProductSchedule> getHeadOutForm(String startDate, String endDate, Pageable pageable);

	public byte[] downloadheadingOutExcel() throws YhNoDataException;

	public void upload(MultipartFile uploadFile, String type) throws UplaodFileException;

}
