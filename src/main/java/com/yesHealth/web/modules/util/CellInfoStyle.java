package com.yesHealth.web.modules.util;

import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import lombok.Data;

@Data
public class CellInfoStyle {
	private short fontSize;
	private String fontName;
	private HorizontalAlignment horizontalAlignment;
	private VerticalAlignment verticalAlignment;
	private BorderStyle borderTop;
	private BorderStyle borderBottom;
	private BorderStyle borderLeft;
	private BorderStyle borderRight;
	private boolean wrapText;
}
