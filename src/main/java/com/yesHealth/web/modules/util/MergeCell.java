package com.yesHealth.web.modules.util;

import lombok.Data;

@Data
public class MergeCell {
	private int startRowIndex;
	private int endRowIndex;
	private int startColIndex;
	private int endColIndex;
	private Object value;
	private CellInfoStyle cellInfoStyle;
}
