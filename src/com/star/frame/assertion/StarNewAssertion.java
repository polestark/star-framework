package com.star.frame.assertion;

import java.io.File;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Description: use org.testng.AssertJUnit and add user defined operations</BR>
 * 				such as record user defined messages to log and take screen shots then create link</BR></BR>
 * 作用描述：使用TestNG的所有断言方法，增加用户自定义的日志记录和截图等方法</BR>
 * 			 仅支持WebDriver + TestNG框架，可自行配置断言失败之后是否继续运行。
 *
 * @author 测试仔刘毅
 */
public class StarNewAssertion {
	
	private WebDriver driver = null;
	private String captureTo = null;
	private String className = null;
	private String sMark = null;
	private Logger logger = null;
	private boolean exitRun = true;
	private boolean needLog = false;
	
	/**
	 * Description: cunstruction with parameter initialize.
	 * 
	 * @param driver the webdriver object.
	 * @param captureToPath the log path to put the screen shot files. 
	 * @param className class name which calling this method.
	 * @param logger the Logger object that used this class.
	 * @param sMark the character that separate the log content. 
	 */
	public StarNewAssertion(WebDriver driver, String captureToPath, String className, Logger logger,
			String sMark) {
		this.driver = driver;
		this.logger = logger;
		this.captureTo = captureToPath;
		this.className = className;
		this.sMark = sMark;
	}

	/**
	 * Description: cunstruction with parameter initialize.
	 * 
	 * @param driver the webdriver object.
	 * @param captureToPath the log path to put the screen shot files. 
	 * @param className class name which calling this method.
	 * @param sMark the character that separate the log content. 
	 */
	public StarNewAssertion(WebDriver driver, String captureToPath, String className, String sMark) {
		this(driver, captureToPath, className, null, sMark);
	}
	
	/**
	 * Description: set if exit on condition assert failed.
	 *
	 * @param exitOnError the bool value to decide if exit when error occured.
	 */
	public void setExitOnAssertFailure(boolean exitOnError){
		this.exitRun = exitOnError;
	}
	
	/**
	 * Description: set if your want to record log after assert passed.
	 *
	 * @param needRecord if set true, it will record log after assert passed.
	 */
	public void setRecordOnSucceed(boolean needRecord){
		this.needLog = needRecord;
	}
	
	/**
	 * Description:record log when assert passed.
	 */
	private void recordMessageAfterAssertion(String status, String message){
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int first = 3, last = 3;
		String mtdName = trace[first].getMethodName();

		for (int i = first; i < trace.length; i++) {
			if (trace[i].getClassName().contains(".reflect.")) {
				last = ((i - 1) <= first) ? first : (i - 1);
				break;
			}
		}
		String cName = trace[last].getClassName() + " # " + trace[last].getLineNumber();
		logger.info(cName + sMark + mtdName + sMark + status + sMark + message);
	}
	
	/**
	 * Description:record log when assert passed.
	 */
	private void recordSuccessAfterAssertion(){
		if (needLog){
			recordMessageAfterAssertion("passed", "assert passed!");
		}
	}
	
	/**
	 * Description:throw RuntimeException has been caught that stops the test run.
	 *
	 * @param exception the Exception object been caught.
	 */
	private void exitOnAssertionError(AssertionError exception){
		if (exitRun){
			throw new RuntimeException(exception);
		}				
	}
	
	/**
	 * Description: throw user defined RuntimeException that stops the test run.
	 *
	 * @param message the user defined message to be recorded.
	 */
	private void exitOnAssertionError(String message){
		if (exitRun){
			throw new RuntimeException(message);
		}
	}

	/**
	 * Description: take screen shot by remotewebdriver.
	 *
	 * @throws Exception
	 */
	private void screenShot() throws Exception{
		String time = String.valueOf(System.currentTimeMillis());
		String fileName = captureTo + className + "_assertion_" + time + ".png";
		RemoteWebDriver rwd = (RemoteWebDriver) new Augmenter().augment(driver);
		File file = ((TakesScreenshot) rwd).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(file, new File(fileName));
		if (null != logger){
			recordMessageAfterAssertion("failed", "assert failed, screenshot is: [" + fileName + "]");
		}
	}

	/**
	 * Description: JUnit/TestNG assertTrue method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param condition the condition to be judged.
	 */
	public void assertTrue(String message, Boolean condition) {
		try {
			org.testng.AssertJUnit.assertTrue(message, condition);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertTrue method.
	 *
	 * @param condition the condition to be judged.
	 */
	public void assertTrue(Boolean condition) {
		assertTrue(null, condition);
	}

	/**
	 * Description: JUnit/TestNG assertFalse method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param condition the condition to be judged.
	 */
	public void assertFalse(String message, Boolean condition) {
		assertTrue(message, !condition);
	}

	/**
	 * Description: JUnit/TestNG assertFalse method.
	 *
	 * @param condition the condition to be judged.
	 */
	public void assertFalse(Boolean condition) {
		assertTrue(null, !condition);
	}

	/**
	 * Description: JUnit/TestNG assertNull method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param object the object to be judged if is null.
	 */
	public void assertNull(String message, Object object) {
		assertTrue(message, null == object);
	}

	/**
	 * Description: JUnit/TestNG assertNull method.
	 *
	 * @param object the object to be judged if is null.
	 */
	public void assertNull(Object object) {
		assertTrue(null, null != object);
	}

	/**
	 * Description: JUnit/TestNG assertNotNull method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param object the object to be judged if is null.
	 */
	public void assertNotNull(String message, Object object) {
		assertTrue(message, null == object);
	}

	/**
	 * Description: JUnit/TestNG assertNotNull method.
	 *
	 * @param object the object to be judged if is null.
	 */
	public void assertNotNull(Object object) {
		assertTrue(null, null != object);
	}

	/**
	 * Description: JUnit/TestNG assertSame method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertSame(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertSame(message, expected, actual);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertSame method.
	 *
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}

	/**
	 * Description: JUnit/TestNG assertNotSame method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertNotSame(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertNotSame(message, expected, actual);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertNotSame method.
	 *
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertNotSame(Object expected, Object actual) {
		assertNotSame(null, expected, actual);
	}

	/**
	 * Description: JUnit/TestNG assertEquals method.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertEquals(String message, Object expected, Object actual) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertEquals method.
	 *
	 * @param expected the expected object.
	 * @param actual the actual object.
	 */
	public void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for string type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, String expected, String actual) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for string type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String expected, String actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for double type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 * @param delta the delta value for compare.
	 */
	public void assertEquals(String message, double expected, double actual, double delta) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual, delta);
			recordSuccessAfterAssertion();
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for double type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 * @param delta the delta value for compare.
	 */
	public void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for float type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 * @param delta the delta value for compare.
	 */
	public void assertEquals(String message, float expected, float actual, float delta) {
		try {
			org.testng.AssertJUnit.assertEquals(message, expected, actual, delta);
			logger.info(className + sMark + "Test-Assertion" + sMark  + "passed" + sMark + "assert passed!");
		} catch (AssertionError ae) {
			try {
				screenShot();
			} catch (Exception e) {
				throw new RuntimeException("take screen shot failed with unexpected Exception!");
			}
			if (null == message) {
				ae.printStackTrace();
				exitOnAssertionError(ae);
			} else {
				System.err.println(message);
				exitOnAssertionError(message);
			}
		}
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for float type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 * @param delta the delta value for compare.
	 */
	public void assertEquals(float expected, float actual, float delta) {
		assertEquals(null, expected, actual, delta);
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for long type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, long expected, long actual) {
		assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for long type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(long expected, long actual) {
		assertEquals(null, Long.valueOf(expected), Long.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for bool type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, boolean expected, boolean actual) {
		assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for bool type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(boolean expected, boolean actual) {
		assertEquals(null, Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for byte type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, byte expected, byte actual) {
		assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for byte type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(byte expected, byte actual) {
		assertEquals(null, Byte.valueOf(expected), Byte.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for char type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, char expected, char actual) {
		assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for char type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(char expected, char actual) {
		assertEquals(null, Character.valueOf(expected), Character.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for short type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, short expected, short actual) {
		assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for short type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(short expected, short actual) {
		assertEquals(null, Short.valueOf(expected), Short.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for int type value compare.
	 *
	 * @param message user defined error message to throw in Exception.
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(String message, int expected, int actual) {
		assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
	}

	/**
	 * Description: JUnit/TestNG assertEquals method for int type value compare.
	 *
	 * @param expected the expected value.
	 * @param actual the actual value.
	 */
	public void assertEquals(int expected, int actual) {
		assertEquals(null, Integer.valueOf(expected), Integer.valueOf(actual));
	}
}