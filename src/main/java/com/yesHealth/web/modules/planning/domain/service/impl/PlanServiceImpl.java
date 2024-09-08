package com.yesHealth.web.modules.planning.domain.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.planning.domain.respository.PlanRepository;
import com.yesHealth.web.modules.planning.domain.service.PlanService;
import com.yesHealth.web.modules.product.domain.entity.ProductSchedule;

@Service
public class PlanServiceImpl implements PlanService {
	private PlanRepository planRepository;
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public PlanServiceImpl(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}

	@Override
	public Page<ProductSchedule> findAll(Pageable pageable) {
		Page<ProductSchedule> plans = planRepository.findAll(pageable);
		return plans;
	}

	@Override
	public Page<ProductSchedule> findByestHarvestDateBetween(String startDateStr, String endDateStr,
			Pageable pageable) {
		Date formateStartDate = startDateStr == null ? getStartOfNextWeek() : convertStringToDate(startDateStr);
		Date formateEndDate = endDateStr == null ? getEndOfNextWeek() : convertStringToDate(endDateStr);
		return planRepository.findByEstHarvestDateBetween(formateStartDate, formateEndDate, pageable);
	}

	// 获取下周的第一天（周一）
	private Date getStartOfNextWeek() {
		LocalDate today = LocalDate.now();
		LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		return convertToDate(nextMonday);
	}

	// 获取下周的最后一天（周五）
	private Date getEndOfNextWeek() {
		LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextFriday = nextMonday.plusDays(4);
		return convertToDate(nextFriday);
	}

	// 将 LocalDate 转换为 Date
	private Date convertToDate(LocalDate localDate) {
		LocalDateTime localDateTime = localDate.atStartOfDay(); // 默认时间是 00:00:00
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private Date convertStringToDate(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null; // 或抛出自定义异常
		}
	}
}
