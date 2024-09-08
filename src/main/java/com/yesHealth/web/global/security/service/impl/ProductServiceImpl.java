package com.yesHealth.web.global.security.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.product.domain.entity.Product;
import com.yesHealth.web.modules.product.domain.repository.ProductRepository;
import com.yesHealth.web.modules.product.domain.service.ProductService;
@Service
public class ProductServiceImpl implements ProductService {
	private ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public Page<Product> findAll(Pageable pageable) {
		Page<Product> product = productRepository.findAll(pageable);
		return product;
	}

}
