package com.yesHealth.web.modules.planning.domain.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.planning.domain.respository.PlanRepository;

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
		long number = planRepository.countByCreateDate(new Date());
		number = number + index;
		String formattedNumber = String.format("%03d", number);
		return PREFIX + formattedDate + formattedNumber;
	}

}
