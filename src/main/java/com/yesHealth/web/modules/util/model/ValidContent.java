package com.yesHealth.web.modules.util.model;

import java.util.List;

import com.yesHealth.web.modules.product.domain.entity.Product;

import lombok.Data;

@Data
public class ValidContent {
	private int index;
	private Product product;
	private List<String> globalErrorList;
}
