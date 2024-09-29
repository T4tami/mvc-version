package com.yesHealth.web.modules.util;

import java.util.List;

import lombok.Data;

@Data
public class ReportInfo {
	private String sheetName;
	private List<Object> dataList;
}
