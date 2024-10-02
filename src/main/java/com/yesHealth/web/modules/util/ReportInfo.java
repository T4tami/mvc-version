package com.yesHealth.web.modules.util;

import java.util.List;

import lombok.Data;

@Data
public class ReportInfo {
	private String sheetName;
	private int minRowCount;
	private int dataRowCount;
	private int colCount;
	private List<ExcelCell> cellList;
}
