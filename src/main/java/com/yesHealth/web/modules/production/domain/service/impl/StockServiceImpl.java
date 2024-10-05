package com.yesHealth.web.modules.production.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.product.domain.entity.Stock;
import com.yesHealth.web.modules.production.domain.respository.StockRepository;
import com.yesHealth.web.modules.production.domain.service.StockService;

@Service
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
