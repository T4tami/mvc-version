package com.yesHealth.web.modules.util;

import java.util.List;

import com.yesHealth.web.modules.planning.domain.service.impl.RowInfo;

import lombok.Data;

@Data
public class ReportInfo {
	private String sheetName;
	private List<RowInfo> rowList;
	private int minRowCount;
	private int dataRowCount;
	private int colCount;
}
