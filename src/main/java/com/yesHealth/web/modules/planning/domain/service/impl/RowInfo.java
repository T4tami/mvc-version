package com.yesHealth.web.modules.planning.domain.service.impl;

import java.util.List;

import com.yesHealth.web.modules.util.ExcelCell;

import lombok.Data;

@Data
public class RowInfo {
	private int rowIndex;
	private List<? extends ExcelCell> rowData;
}
