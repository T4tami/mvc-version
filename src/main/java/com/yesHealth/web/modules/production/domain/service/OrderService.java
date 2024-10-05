package com.yesHealth.web.modules.production.domain.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.production.domain.respository.PlanRepository;

@Service
public class OrderService {
	final static String PREFIX = "B511-";
	private static PlanRepository planRepository;

	public OrderService(PlanRepository planRepository) {
		OrderService.planRepository = planRepository;
	}

	public static String generateOrderNo(int index) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String formattedDate = sdf.format(new Date());
		Date currentDate = new Date();
		Date dateWithoutTime = null;
		try {
			dateWithoutTime = getDateWithoutTime(currentDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long number = planRepository.countByCreateDate(dateWithoutTime);
		number = number + index;
		String formattedNumber = String.format("%03d", number);
		return PREFIX + formattedDate + formattedNumber;
	}

	private static Date getDateWithoutTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
