package com.yesHealth.web.modules.util.service;

import com.yesHealth.web.modules.util.model.ValidContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidLuxContent extends ValidContent {
	private Long sStockId;
	private Long gStockId;
	private Long pStockId;
}
