package com.star.logging.selenium;

import com.thoughtworks.selenium.Wait;

public abstract class LoggingWait extends Wait {
	private LoggingSelenium logging;

	public LoggingWait(LoggingSelenium selenium) {
		this.logging = selenium;
	}

	public void wait(String message, long timeoutInMilliseconds, long intervalInMilliseconds) {
		try {
			super.wait(message, timeoutInMilliseconds, intervalInMilliseconds);
		} catch (Wait.WaitTimedOutException e) {
			if (null != this.logging) {
				this.logging.logAssertion("WaitTimedOutException", e.getMessage(),
						"Timeout after [msec]: " + Long.valueOf(timeoutInMilliseconds).toString());
			}

			throw e;
		}
	}
}