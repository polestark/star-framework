package com.star.logging.selenium;

public abstract interface ResultsFormatter {
	public abstract void commentLogEvent(LoggingBean paramLoggingBean);

	public abstract void commandLogEvent(LoggingBean paramLoggingBean);

	public abstract void booleanCommandLogEvent(LoggingBean paramLoggingBean);

	public abstract void methodLogEvent(LoggingBean paramLoggingBean);

	public abstract void headerLogEvent(TestMetricsBean paramTestMetricsBean);

	public abstract void footerLogEvent();

	public abstract String getScreenShotBaseUri();

	public abstract void setScreenShotBaseUri(String paramString);

	public abstract String generateFilenameForAutomaticScreenshot(String paramString);

	public abstract String getAutomaticScreenshotPath();

	public abstract void setAutomaticScreenshotPath(String paramString);
}