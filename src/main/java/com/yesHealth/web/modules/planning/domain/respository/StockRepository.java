package com.yesHealth.web.modules.planning.domain.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.product.domain.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

}
