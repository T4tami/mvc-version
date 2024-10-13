package com.yesHealth.web.modules.production.domain.exception;

import java.util.Map;

import lombok.Data;

@Data
public class YhValidateException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3555796799932621184L;

	public YhValidateException(String message, Map<String, Object> businessErrors) {
		this.message = message;
		this.businessErrors = businessErrors;
	}

	private String message;
	private Map<String, Object> businessErrors;
}
