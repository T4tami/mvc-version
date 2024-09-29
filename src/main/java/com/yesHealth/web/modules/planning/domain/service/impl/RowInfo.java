package com.yesHealth.web.modules.planning.domain.service.impl;

import java.util.List;

import lombok.Data;

@Data
public class RowInfo {
	private int rowIndex;
	private List<CellInfo> rowData;
}
