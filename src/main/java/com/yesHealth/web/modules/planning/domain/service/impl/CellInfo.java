package com.yesHealth.web.modules.planning.domain.service.impl;

import com.yesHealth.web.modules.util.ExcelCell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CellInfo extends ExcelCell {
	private String value;
	private int colIndex;

}
