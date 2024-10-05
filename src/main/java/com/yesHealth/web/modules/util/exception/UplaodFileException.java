package com.yesHealth.web.modules.util.exception;

public class UplaodFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UplaodFileException(String message) {
		super();
		this.message = message;
	}

}
