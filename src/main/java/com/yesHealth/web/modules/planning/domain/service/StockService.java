package com.yesHealth.web.modules.planning.domain.service;

import java.util.List;

import com.yesHealth.web.modules.product.domain.entity.Stock;

public interface StockService {
	List<Stock> findAll();
}
