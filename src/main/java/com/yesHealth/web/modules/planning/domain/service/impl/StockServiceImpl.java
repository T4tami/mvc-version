package com.yesHealth.web.modules.planning.domain.service.impl;

import java.util.List;

import com.yesHealth.web.modules.planning.domain.respository.StockRepository;
import com.yesHealth.web.modules.planning.domain.service.StockService;
import com.yesHealth.web.modules.product.domain.entity.Stock;

public class StockServiceImpl implements StockService {
	private StockRepository stockRepository;

	public StockServiceImpl(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Override
	public List<Stock> findAll() {
		return stockRepository.findAll();
	}

}
