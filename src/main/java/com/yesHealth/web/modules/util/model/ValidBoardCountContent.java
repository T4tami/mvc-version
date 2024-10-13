package com.yesHealth.web.modules.util.model;

import java.util.List;

import com.yesHealth.web.modules.product.domain.entity.Stock;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidBoardCountContent extends ValidContent {
	private Long gStockId;
	private Long pStockId;
	private String growingDate;
	private String matureDate;
	private List<Stock> stocks;
}
