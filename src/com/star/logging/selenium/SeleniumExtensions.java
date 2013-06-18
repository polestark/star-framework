package com.star.logging.selenium;

public enum SeleniumExtensions {
	COMMAND_EXTENSION_LOG_COMMENT("X-1and1-logComment"), COMMAND_EXTENSION_LOG_AUTO_SCREENSHOT(
			"X-1and1-logAutoScreenshot"), COMMAND_EXTENSION_LOG_ASSERTION("X-1and1-logAssertion"), COMMAND_EXTENSION_LOG_RESOURCE(
			"X-1and1-logResource");

	private String name;

	private SeleniumExtensions(String newName) {
		this.name = newName;
	}

	public String getName() {
		return this.name;
	}
}