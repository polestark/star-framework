package com.star.core.webdriver.user;

import java.util.Map;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.star.frame.tools.StackTraceUtils;
import com.star.logging.frame.LoggingManager;
import com.star.logging.webdriver.LoggingModeHelper;
import com.star.testdata.string.StringBufferUtils;

public class WebDriverListener implements WebDriverEventListener {

	private String className = WebDriverListener.class.getName();
	private String filePath = ".\\log\\";
	private final StringBufferUtils STR = new StringBufferUtils();
	private final RuntimeSupport SUPPORT = new RuntimeSupport();
	private final LoggingManager LOG = new LoggingManager(WebDriverListener.class.getName());
	private StackTraceUtils stack;
	private Map<String,String> map;
	private LoggingModeHelper logHelper;

	public WebDriverListener() {
		throw new IllegalArgumentException("you must config the parameter correctly!");
	}

	public WebDriverListener(String location, String runClassName,Logger logger) {
		this.className = runClassName;
		this.filePath = location.endsWith("/") || location.endsWith("\\") ? location : location + "/";
		this.stack = new StackTraceUtils();
		this.logHelper = new LoggingModeHelper(runClassName,location,"GBK");
	}

	/**
	 * Description: see if exception is instanceof WebDriverException.
	 * 
	 * @param exception runtime exceptions.
	 * @param driver the webdriver instance.
	 * @throws RuntimeException.
	 */
	private void onWebDriverException(Throwable exception, WebDriver driver){
		String fileName = filePath + className + STR.formatedTime("_yyyyMMdd_HHmmssSSS") + ".png";
		if (exception instanceof WebDriverException){
			SUPPORT.screenShot(driver, fileName);
			String message = "run failed, screenshot is: [" + fileName + "]";
			map = stack.traceRecord(Thread.currentThread().getStackTrace(), "failed", message);
			logHelper.LogWrite(map);
			exception.printStackTrace();
		}
		throw new RuntimeException(exception);
	}

	/**
	 * Description: wait milliseconds.
	 * 
	 * @param timeout time to wait, in millisecond
	 */
	public void waitFor(long timeout){
		try {
			Thread.currentThread().join(timeout);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void beforeNavigateTo(String url, WebDriver driver) {
	}

	@Override
	public void afterNavigateTo(String url, WebDriver driver) {
		//String message = "navigate to [ " + url + " ] completed.";
		//map = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(map);
	}

	@Override
	public void beforeNavigateBack(WebDriver driver) {
	}

	@Override
	public void afterNavigateBack(WebDriver driver) {
		//String message = "navigate back completed.";
		//stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message);
	}

	@Override
	public void beforeNavigateForward(WebDriver driver) {
	}

	@Override
	public void afterNavigateForward(WebDriver driver) {
		//String message = "navigate forward completed.";
		//stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message);
	}

	@Override
	public void beforeFindBy(By by, WebElement element, WebDriver driver) {
	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver) {
		//String message = "find element by [ " + by.toString() + " ] completed.";
		//map=stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(map);
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver) {
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver) {
		//String message = "click on element [ " + SUPPORT.getElementXpath(driver, element) + " ] completed.";
		//map = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(map);
	}
	
	public void afterClickOn(By by, WebDriver driver) {
		//String message = "click on element [ " + by.toString() + " ] completed.";
		//map = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(map);
	}

	@Override
	public void beforeChangeValueOf(WebElement element, WebDriver driver) {
	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver) {
		//String message = "element [ " + SUPPORT.getElementXpath(driver, element) + " ] value change completed.";
		//stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message);
	}
	
	public void afterChangeValueOf(By by, WebDriver driver) {
		//String message = "element [ " + by.toString() + " ] value change completed.";
		//stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message);
	}

	@Override
	public void beforeScript(String script, WebDriver driver) {
	}

	@Override
	public void afterScript(String script, WebDriver driver) {
		//String message = "javascript [ " + script + " ] execute completed.";
		//stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message);
	}

	/**
	 * Description: override the onException method of WebDriverEventListener.
	 * 
	 * @param exception runtime exceptions.
	 * @param driver the webdriver instance.
	 * @throws RuntimeException.
	 */
	@Override
	public void onException(Throwable exception, WebDriver driver) {
		try {
			onWebDriverException(exception, driver);
		} catch (Throwable unexpected) {
			LOG.error(unexpected);
			throw new RuntimeException(unexpected);
		}
	}
}