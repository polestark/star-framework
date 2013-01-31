package com.star.toolapi.webdriver.user;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

import com.star.logging.frame.LoggingManager;
import com.star.testdata.string.StringBufferUtils;

public class WebDriverListener extends AbstractWebDriverEventListener {
	
	private String className = WebDriverListener.class.getName();
	private String devidor = "~";
	private String filePath = "./log/";
	private Logger logger = null;
	private final StringBufferUtils STR = new StringBufferUtils();
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
	
	@Override
	public void onException(Throwable unexpected, WebDriver driver) {
		String fileName = filePath + className + STR.formatedTime("_yyyyMMdd_HHmmssSSS") + ".png";
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String methodName = trace[getTraceMethodLevel(trace)].getMethodName();
		try {
			RemoteWebDriver swd = (RemoteWebDriver) new Augmenter().augment(driver);
			File file = ((TakesScreenshot) swd).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(file, new File(fileName));
			recordError(methodName, fileName);
			throw new RuntimeException(unexpected);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
			throw new RuntimeException(e);
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
		logger.info(traceClass + devidor + methodName + devidor + "failed" + devidor 
				+ "method [" + methodName + "] failed, screenshot is: [" 
				+ fileName + "]".replace(devidor, "-").replace("&", "&"));
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