package com.star.core.webdriver.helper;

public class FilteredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FilteredException() {
		super();
	}

	public FilteredException(String message) {
		super(message);
	}

	public FilteredException(Throwable cause) {
		super(cause);
	}
}