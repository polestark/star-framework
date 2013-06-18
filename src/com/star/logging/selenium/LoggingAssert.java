package com.star.logging.selenium;

import org.hamcrest.Matcher;
import org.junit.Assert;

public final class LoggingAssert {
	public static void assertTrue(String message, boolean condition, LoggingSelenium selenium) {
		try {
			Assert.assertTrue(message, condition);
		} catch (AssertionError e) {
			selenium.logAssertion("assertTrue", message, "condition=" + condition);
			throw e;
		}
	}

	public static void assertFalse(String message, boolean condition, LoggingSelenium selenium) {
		try {
			Assert.assertFalse(message, condition);
		} catch (AssertionError e) {
			selenium.logAssertion("assertFalse", message, "condition=" + condition);
			throw e;
		}
	}

	public static void assertEquals(String message, Object expected, Object actual,
			LoggingSelenium selenium) {
		try {
			Assert.assertEquals(message, expected, actual);
		} catch (AssertionError e) {
			selenium.logAssertion("assertEquals", message, "expected=" + expected + " actual="
					+ actual);
			throw e;
		}
	}

	public static <T> void assertThat(String message, T actual, Matcher<T> matcher,
			LoggingSelenium selenium) {
		try {
			Assert.assertSame(message, actual, matcher);
		} catch (AssertionError e) {
			selenium.logAssertion("assertThat", message, e.getMessage());
			throw e;
		}
	}
}