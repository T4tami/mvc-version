package com.yesHealth.web.modules.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public enum CellStyleInfo {
	TD_LEFT("標楷體", (short) 11, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN,
			BorderStyle.THIN, BorderStyle.THIN, false),
	TD_CENTER("標楷體", (short) 11, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, BorderStyle.THIN,
			BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, false),
	TH_CENTER("標楷體", (short) 11, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, BorderStyle.THIN,
			BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, true),
	HEADER("標楷體", (short) 18, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, null, null, null, null, false),
	CENTER("標楷體", (short) 11, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, null, null, null, null, false),
	LEFT("標楷體", (short) 11, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, null, null, null, null, false),
	RIGHT("標楷體", (short) 11, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER, null, null, null, null, false);

	private final short fontSize;
	private final String fontName;
	private final HorizontalAlignment horizontalAlignment;
	private final VerticalAlignment verticalAlignment;
	private final BorderStyle borderTop;
	private final BorderStyle borderBottom;
	private final BorderStyle borderLeft;
	private final BorderStyle borderRight;
	private final boolean wrapText;

	CellStyleInfo(String fontName, short fontSize, HorizontalAlignment horizontalAlignment,
			VerticalAlignment verticalAlignment, BorderStyle borderTop, BorderStyle borderBottom,
			BorderStyle borderLeft, BorderStyle borderRight, boolean wrapText) {
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.borderTop = borderTop;
		this.borderBottom = borderBottom;
		this.borderLeft = borderLeft;
		this.borderRight = borderRight;
		this.wrapText = wrapText;
	}

	public short getFontSize() {
		return fontSize;
	}

	public String getFontName() {
		return fontName;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public BorderStyle getBorderTop() {
		return borderTop;
	}

	public BorderStyle getBorderBottom() {
		return borderBottom;
	}

	public BorderStyle getBorderLeft() {
		return borderLeft;
	}

	public BorderStyle getBorderRight() {
		return borderRight;
	}

	public boolean isWrapText() {
		return wrapText;
	}

}
