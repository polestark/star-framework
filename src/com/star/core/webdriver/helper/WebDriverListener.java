package com.star.core.webdriver.helper;

import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;
import com.star.logging.frame.LoggingManager;
import com.star.logging.webdriver.LoggerModeChoice;
import com.star.testdata.string.StringBufferUtils;
import com.star.tools.ReadConfiguration;
import com.star.tools.StackTraceUtils;

public class WebDriverListener implements WebDriverEventListener {
	private final ReadConfiguration config = new ReadConfiguration(
			"/com/star/core/webdriver/webdirver_config.properties");
	
	private final String CAPTURE_MESSAGE = config.get("CAPTURE_MESSAGE");

	private String className = WebDriverListener.class.getName();
	private String filePath = ".\\log\\";
	private final StringBufferUtils STR = new StringBufferUtils();
	private final RuntimeSupport SUPPORT = new RuntimeSupport();
	private final LoggingManager LOG = new LoggingManager(WebDriverListener.class.getName());
	private StackTraceUtils stack = new StackTraceUtils();
	private Map<String,String> infoMap;
	private LoggerModeChoice logHelper;
	private StackTraceElement[] traces;
	private StackTraceElement trace;

	public WebDriverListener() {
		throw new IllegalArgumentException("you must config the parameter correctly!");
	}

	public WebDriverListener(String location, String runClassName) {
		this.className = runClassName;
		this.filePath = location.endsWith("/") || location.endsWith("\\") ? location : location + "/";
		this.logHelper = new LoggerModeChoice(runClassName,location,"GBK");
	}
	
	private void exceptionFilter(Throwable exception, WebDriver driver){
		if (exception instanceof FilteredException){
		}else{
			onWebDriverException(exception, driver);
		}		
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
			String message = "run " + CAPTURE_MESSAGE + " [" + fileName + "]";
			infoMap = stack.traceRecord(Thread.currentThread().getStackTrace(), "failed", message);
			logHelper.LogWrite(infoMap);
		}
		traces = exception.getStackTrace();
		trace = traces[stack.getTraceClassLevel(traces)];
		String info = trace.getClassName() + ", method: " + trace.getMethodName() + ", line: " + trace.getLineNumber();
		System.err.println("Exception Occured: \n" + getError(exception) + "\n" + info);			
	}
	
	/**
	 * Description: the user defined error message.
	 *
	 * @param exception Throwables.
	 * @return the user defined message string.
	 */
	private String getError(Throwable exception){
		String err = exception.getMessage().split("WARNING: The")[0];
		return err.substring(0, err.length() - 1);
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
		//infoMap = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(infoMap);
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
		//infoMap=stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(infoMap);
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver) {
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver) {
		//String message = "click on element [ " + SUPPORT.getElementXpath(driver, element) + " ] completed.";
		//infoMap = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(infoMap);
	}
	
	public void afterClickOn(By by, WebDriver driver) {
		//String message = "click on element [ " + by.toString() + " ] completed.";
		//infoMap = stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message,1);
		//logwritter.write(infoMap);
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
			exceptionFilter(exception, driver);
		} catch (Throwable unexpected) {
			LOG.error(unexpected);
			throw new RuntimeException(unexpected);
		}
	}
}