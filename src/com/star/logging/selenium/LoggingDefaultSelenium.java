package com.star.logging.selenium;

import com.thoughtworks.selenium.CommandProcessor;
import com.star.core.selenium.ExtendDefaultSelenium;

public class LoggingDefaultSelenium extends ExtendDefaultSelenium implements LoggingSelenium {

	public LoggingDefaultSelenium(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	public void logComment(String comment) {
		this.commandProcessor.doCommand(SeleniumExtensions.COMMAND_EXTENSION_LOG_COMMENT.getName(),
				new String[] { comment });
	}

	public void logAutomaticScreenshot(String baseName) {
		this.commandProcessor.doCommand(SeleniumExtensions.COMMAND_EXTENSION_LOG_AUTO_SCREENSHOT.getName(),
				new String[] { baseName });
	}

	public void logAssertion(String assertionName, String assertionMessage, String assertionCondition) {
		this.commandProcessor.doCommand(SeleniumExtensions.COMMAND_EXTENSION_LOG_ASSERTION.getName(),
				new String[] { assertionName, assertionMessage, assertionCondition });
	}

	public void logResource(String file, String description) {
		this.commandProcessor.doCommand(SeleniumExtensions.COMMAND_EXTENSION_LOG_RESOURCE.getName(),
				new String[] { file, description });
	}
}