package com.star.core.webdriver.helper;

import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;
import com.star.logging.frame.LoggingManager;
import com.star.logging.webdriver.HTMLLogWriter;
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
	private Map<String, String> infoMap;
	private HTMLLogWriter logHelper;

	public WebDriverListener() {
		throw new IllegalArgumentException("you must config the parameter correctly!");
	}

	public WebDriverListener(String location, String runClassName) {
		this.className = runClassName;
		this.filePath = location.endsWith("/") || location.endsWith("\\") ? location : location + "/";
		this.logHelper = new HTMLLogWriter(runClassName, location, "GBK");
	}

	/**
	 * Description: see if exception is instanceof WebDriverException.
	 * 
	 * @param exception runtime exceptions.
	 * @param driver the webdriver instance.
	 * @throws RuntimeException.
	 */
	private void onWebDriverException(Throwable exception, WebDriver driver) {
		String fileName = filePath + className + STR.formatedTime("_yyyyMMdd_HHmmssSSS") + ".png";
		if (exception instanceof WebDriverException) {
			SUPPORT.screenShot(driver, fileName);
			String message = "run " + CAPTURE_MESSAGE + " [" + fileName + "]";
			infoMap = stack.logRecord(Thread.currentThread().getStackTrace(), "failed", message);
			logHelper.logWrite(infoMap);
		}
		exception.printStackTrace();
	}

	@Override
	public void beforeNavigateTo(String url, WebDriver driver) {
	}

	@Override
	public void afterNavigateTo(String url, WebDriver driver) {
	}

	@Override
	public void beforeNavigateBack(WebDriver driver) {
	}

	@Override
	public void afterNavigateBack(WebDriver driver) {
	}

	@Override
	public void beforeNavigateForward(WebDriver driver) {
	}

	@Override
	public void afterNavigateForward(WebDriver driver) {
	}

	@Override
	public void beforeFindBy(By by, WebElement element, WebDriver driver) {
	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver) {
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver) {
	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver) {
	}

	public void afterClickOn(By by, WebDriver driver) {
	}

	@Override
	public void beforeChangeValueOf(WebElement element, WebDriver driver) {
	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver) {
	}

	public void afterChangeValueOf(By by, WebDriver driver) {
	}

	@Override
	public void beforeScript(String script, WebDriver driver) {
	}

	@Override
	public void afterScript(String script, WebDriver driver) {
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