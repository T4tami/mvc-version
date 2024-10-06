package com.yesHealth.web.modules.production.domain.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;
import com.yesHealth.web.modules.production.domain.exception.YhNoDataException;
import com.yesHealth.web.modules.util.exception.UplaodFileException;

public interface DailyReportService<T> {
	public Page<ProductSchedule> getForm(String startDateStr, String endDateStr, Pageable pageable);

	public byte[] downloadDailyExcel() throws YhNoDataException;

	public void uploadDailyExcel(MultipartFile uploadFile) throws YhNoDataException, UplaodFileException;

	public void update(T element);

	public static Integer calculateTotalBoardCount(List<ProductSchedule> psList) {
		Integer sum = 0;
		for (ProductSchedule ps : psList) {
			Integer count = ps.getWateringBoardCount();
			if (count != null) {
				sum += count;
			}
		}
		return sum;
	}
}
