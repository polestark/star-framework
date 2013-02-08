package com.star.toolapi.webdriver.user;

import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import com.star.logging.frame.LoggingManager;
import com.star.testdata.string.StringBufferUtils;

public class WebDriverListener extends AbstractWebDriverEventListener {

	private String className = WebDriverListener.class.getName();
	private String devidor = "~";
	private String filePath = ".\\log\\";
	private Logger logger = null;
	private final StringBufferUtils STR = new StringBufferUtils();
	private final RuntimeSupport SUPPORT = new RuntimeSupport();
	private final LoggingManager LOG = new LoggingManager(WebDriverListener.class.getName());

	public WebDriverListener() {
		throw new IllegalArgumentException("you must config the parameter correctly!");
	}

	public WebDriverListener(String location, String runClassName,Logger logger, String seperateMark) {
		this.className = runClassName;
		this.filePath = location.endsWith("/") || location.endsWith("\\") ? location : location + "/";
		this.logger = logger;
		this.devidor = seperateMark;
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
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String methodName = trace[getTraceMethodLevel(trace)].getMethodName();
		try {
			onWebDriverException(exception, driver, methodName);
		} catch (Throwable unexpected) {
			LOG.error(unexpected);
			throw new RuntimeException(unexpected);
		}
	}

	/**
	 * Description: see if exception is instanceof WebDriverException.
	 * 
	 * @param exception runtime exceptions.
	 * @param driver the webdriver instance.
	 * @param methodName the method name to be record.
	 * @throws RuntimeException.
	 */
	private void onWebDriverException(Throwable exception, WebDriver driver, String methodName){
		String fileName = filePath + className + STR.formatedTime("_yyyyMMdd_HHmmssSSS") + ".png";
		if (exception instanceof WebDriverException){
			SUPPORT.screenShot(driver, fileName);
			recordError(methodName, fileName);
			String err = exception.getMessage().split("WARNING: The")[0];
			System.out.println("==============error occurs, the message is:==============");
			waitFor(100);
			System.err.println("	" + err.substring(0, err.length() - 1));
			waitFor(100);
			System.out.println("==============the test run will abort soon!==============");
		}else{
			LOG.error(exception);
			throw new RuntimeException(exception);
		}
	}
	
	private void waitFor(long timeout){
		try {
			Thread.currentThread().join(timeout);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Description: record to logs with fail info messages.
	 * 
	 * @param methodName the method name to be record.
	 * @param fileName the file name of the screenshot.
	 */
	private void recordError(String methodName, String fileName) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int index = getTraceClassLevel(trace);

		String traceClass = trace[index].getClassName() + " # " + trace[index].getLineNumber();
		logger.info(traceClass + devidor + methodName + devidor + "failed" + devidor + "method [" + methodName 
					+ "] failed, screenshot is: [" + fileName + "]".replace(devidor, "-").replace("&", "&"));
	}

	/**
	 * get the trace level index of the running test class. 
	 * 
	 * @param trace the StackTraceElement array.
	 * @return the index of the trace deepth of class.
	 */
	private int getTraceClassLevel(StackTraceElement[] trace){
		for(int i = trace.length - 1; i > 0 ; i --){
			if (trace[i].getClassName().equals("sun.reflect.NativeMethodAccessorImpl")){
				return i - 2;
			}
		}
		return 0;
	}

	/**
	 * get the trace level index of the running test method. 
	 * 
	 * @param trace the StackTraceElement array.
	 * @return the index of the trace deepth of method.
	 */
	private int getTraceMethodLevel(StackTraceElement[] trace){
		for(int i = trace.length - 1; i > 0 ; i --){
			if (trace[i].getMethodName().equals("invoke0")){
				return i - 2;
			}
		}
		return 0;
	}
}