package com.star.logging.selenium;

import com.star.core.selenium.ExtendSelenium;

public abstract interface LoggingSelenium extends ExtendSelenium {
	public abstract void logComment(String paramString);

	public abstract void logAutomaticScreenshot(String paramString);

	public abstract void logAssertion(String paramString1, String paramString2, String paramString3);

	public abstract void logResource(String paramString1, String paramString2);
}