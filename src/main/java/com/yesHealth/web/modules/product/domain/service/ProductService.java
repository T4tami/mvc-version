package com.yesHealth.web.modules.product.domain.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yesHealth.web.modules.product.domain.entity.Product;

public interface ProductService {
	Page<Product> findAll(Pageable pageable);

	List<Product> findAll();
}
