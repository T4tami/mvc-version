package com.yesHealth.web.modules.product.domain.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
	Page<Product> findAll(Pageable pageable);
}
