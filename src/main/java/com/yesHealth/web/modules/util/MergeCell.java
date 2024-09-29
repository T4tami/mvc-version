package com.yesHealth.web.modules.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MergeCell extends ExcelCell {
	private int startRowIndex;
	private int endRowIndex;
	private int startColIndex;
	private int endColIndex;

}
