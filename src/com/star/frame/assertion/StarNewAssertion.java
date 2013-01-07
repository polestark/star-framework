package com.star.frame.assertion;

import java.io.File;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class StarNewAssertion {
	
	private WebDriver driver = null;
	private String filePath = null;
	private String clsName = null;
	private Logger logger = null;
	private final String SMARK = "~";
	
	public StarNewAssertion(WebDriver driver, String fileWhere, String className, Logger log){
		this.driver = driver;
		this.filePath = fileWhere;
		this.clsName = className;
		this.logger = log;
	}
	
	private void recordErrorMessageOnAssertionError(String fileName) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int first = 3, last = 3;
		String mtdName = trace[first].getMethodName();

		for (int i = first; i < trace.length; i++) {
			if (trace[i].getClassName().contains(".reflect.")) {
				last = ((i - 1) <= first) ? first : (i - 1);
				break;
			}
		}
		String clsName = trace[last].getClassName() + " # " + trace[last].getLineNumber();
		logger.info(clsName + SMARK + mtdName + SMARK + "failed" + SMARK 
				+ "assert failed, screenshot is: [" + fileName + "]");
	}

	private void screenShot() throws Exception{
		String time = String.valueOf(System.currentTimeMillis());
		String fileName = filePath + clsName + "_assertion_" + time + ".png";
		RemoteWebDriver rwd = (RemoteWebDriver) new Augmenter().augment(driver);
		File file = ((TakesScreenshot) rwd).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(file, new File(fileName));
		recordErrorMessageOnAssertionError(fileName);
	}

	public void assertTrue(String message, Boolean condition) {
		try {
			org.testng.AssertJUnit.assertTrue(message, condition);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertTrue(Boolean condition) {
		assertTrue(null, condition);
	}

	public void assertFalse(String message, Boolean condition) {
		assertTrue(message, !condition);
	}

	public void assertFalse(Boolean condition) {
		assertTrue(null, !condition);
	}

	public void assertNull(String message, Object object) {
		assertTrue(message, null == object);
	}

	public void assertNull(Object object) {
		assertTrue(null, null != object);
	}

	public void assertNotNull(String message, Object object) {
		assertTrue(message, null == object);
	}

	public void assertNotNull(Object object) {
		assertTrue(null, null != object);
	}

	public void assertSame(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertSame(message, expected, actual);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}

	public void assertNotSame(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertNotSame(message, expected, actual);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertNotSame(Object expected, Object actual) {
		assertNotSame(null, expected, actual);
	}

	public void assertEquals(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	public void assertEquals(String message, String expected, String actual) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertEquals(String expected, String actual) {
		assertEquals(null, expected, actual);
	}

	public void assertEquals(String message, double expected, double actual, double delta) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual, delta);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}

	public void assertEquals(String message, float expected, float actual, float delta) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual, delta);
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				throw new RuntimeException(ae);
			} else {
				System.err.println(message);
				throw new RuntimeException(message);
			}
		}
	}

	public void assertEquals(float expected, float actual, float delta) {
		assertEquals(null, expected, actual, delta);
	}

	public void assertEquals(String message, long expected, long actual) {
		assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
	}

	public void assertEquals(long expected, long actual) {
		assertEquals(null, Long.valueOf(expected), Long.valueOf(actual));
	}

	public void assertEquals(String message, boolean expected, boolean actual) {
		assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	public void assertEquals(boolean expected, boolean actual) {
		assertEquals(null, Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	public void assertEquals(String message, byte expected, byte actual) {
		assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
	}

	public void assertEquals(byte expected, byte actual) {
		assertEquals(null, Byte.valueOf(expected), Byte.valueOf(actual));
	}

	public void assertEquals(String message, char expected, char actual) {
		assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
	}

	public void assertEquals(char expected, char actual) {
		assertEquals(null, Character.valueOf(expected), Character.valueOf(actual));
	}

	public void assertEquals(String message, short expected, short actual) {
		assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
	}

	public void assertEquals(short expected, short actual) {
		assertEquals(null, Short.valueOf(expected), Short.valueOf(actual));
	}

	public void assertEquals(String message, int expected, int actual) {
		assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
	}

	public void assertEquals(int expected, int actual) {
		assertEquals(null, Integer.valueOf(expected), Integer.valueOf(actual));
	}
}