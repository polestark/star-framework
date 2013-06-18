package com.star.logging.selenium;

public abstract interface LoggingNotifier {
	public abstract Boolean errorLogging(Object paramObject, String paramString1,
			String[] paramArrayOfString, String paramString2, Throwable paramThrowable,
			long paramLong);

	public abstract Boolean makeScreenshot(Object paramObject, String paramString);
}