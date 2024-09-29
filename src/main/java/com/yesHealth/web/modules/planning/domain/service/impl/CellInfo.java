package com.yesHealth.web.modules.planning.domain.service.impl;

import com.yesHealth.web.modules.util.CellInfoStyle;

import lombok.Data;

@Data
public class CellInfo {
	private CellInfoStyle cellInfoStyle;
	private String value;
	private int colIndex;
}
