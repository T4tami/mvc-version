package com.yesHealth.web.modules.util.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MergeCell extends ExcelCell {
	private String startCell;
	private String endCell;

}
