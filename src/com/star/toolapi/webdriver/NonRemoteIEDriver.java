package com.star.toolapi.webdriver;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import com.google.common.base.Throwables;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;
import com.star.testdata.string.StringBufferUtils;
import static org.testng.AssertJUnit.assertTrue;

public class NonRemoteIEDriver {

	protected static WebDriver driver;

	private static InternetExplorerDriverService service;
	private final LoggingManager LOG = new LoggingManager(this.getClass().getName());
	private boolean needAssert = true;
	private boolean needErrShot = true;

	protected static final StringBufferUtils string = new StringBufferUtils();
	protected static final ParseProperties property = new ParseProperties("config/config.properties");
	protected static final String ROOT_DIR = System.getProperty("user.dir");
	protected static final String LOG_DIR = ROOT_DIR + "/log/";

	/**
	 * get a new distinct filename only if the file exists already
	 * 
	 * @param dir
	 *            file location
	 * @param fileName
	 *            file name to judge
	 * @param fileType
	 *            file type such as ".html"
	 * @return if file exists then add mark by time
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String distinctName(String dir, String fileName, String fileType) {
		String resultName = dir + fileName + "." + fileType;
		if (new File(resultName).exists()) {
			resultName = dir + fileName + string.formatedTime("-yyyyMMdd-HHmmssSSS") + fileType;
		}
		return resultName;
	}

	/**
	 * start WebDriver, using IEDriverServer.exe
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void startWebDriver() {
		String fileName = distinctName(LOG_DIR, this.getClass().getName(), "log");
		System.setProperty("webdriver.ie.driver", "lib\\IEDriverServer.exe");

		try {
			service = new InternetExplorerDriverService.Builder().usingAnyFreePort().withLogFile(new File(fileName))
					.withLogLevel(InternetExplorerDriverLogLevel.DEBUG).build();
			service.start();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				service.stop();
			}
		});

		LoggingPreferences logs = new LoggingPreferences();
		logs.enable(LogType.PROFILER, Level.INFO);

		DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);
		driver = new InternetExplorerDriver(service, capabilities);
	}

	/**
	 * close WebDriver, close current session opened by WebDriver.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void closeWebDriver() {
		try {
			if (driver != null && driver.getWindowHandles().size() > 0) {
				driver.close();
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * quit WebDriver, close WebDriver instance and clear all sessions.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void stopWebDriver() {
		try {
			if (service != null) {
				service.stop();
			}
			if (driver != null) {
				driver.quit();
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * set if screenshot needed when assertionerror occurred.
	 * 
	 * @param needShot
	 *            if you want to take screen shot when error occured
	 * @author PAICDOM/LIUYI027
	 **/
	protected void setErrorShot(boolean needShot) {
		this.needErrShot = needShot;
	}

	/**
	 * get if screenshot needed when assertionerror occurred.
	 * 
	 * @return if you want to take screen shot when error occured
	 * @author PAICDOM/LIUYI027
	 **/
	protected boolean getErrorShot() {
		return this.needErrShot;
	}

	/**
	 * set if Assertion needed when running testcase.
	 * 
	 * @param needAssertion
	 *            if you want to assertion when running your testcase
	 * @author PAICDOM/LIUYI027
	 **/
	protected void setAssertion(boolean needAssertion) {
		if (needAssertion) {
			setErrorShot(true);
		}
		this.needAssert = needAssertion;
	}

	/**
	 * get if Assertion needed when running testcase.
	 * 
	 * @return if you want to assertion when running your testcase
	 * @author PAICDOM/LIUYI027
	 **/
	protected boolean getAssertion() {
		return this.needAssert;
	}

	/**
	 * public method for handle assertions and screenshot.
	 * 
	 * @param isSucceed
	 *            if your operation success
	 * @param methodName
	 *            the name of the reproted mathod which using this method
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void testAssertions(boolean isSucceed, String methodName) {
		if (getAssertion()) {
			try {
				assertTrue(isSucceed);
			} catch (AssertionError ae) {
				LOG.error(ae, "method [" + methodName + "] run failed! "
						+ "you can see more details nearly above this error message");
				if (getErrorShot()) {
					takeScreenShot();
				}
				throw new RuntimeException("Assert Failed:" + ae.getMessage());
			}
		}
	}

	/**
	 * take a screen shot and save the file by path and name.
	 * 
	 * @param fileName
	 *            the file path&name of the screenshot to be saved
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void takeScreenShot(String fileName) {
		try {
			RemoteWebDriver swd = (RemoteWebDriver) new Augmenter().augment(driver);
			File file = ((TakesScreenshot) swd).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(file, new File(fileName));
		} catch (Exception e) {
			throw new RuntimeException("unexpected Exception occured: " + e.getMessage());
		}
	}

	/**
	 * override the screenShot method, using default path and name.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void takeScreenShot() {
		String className = this.getClass().getName();
		String time = string.formatedTime("-yyyyMMdd-HHmmssSSS");
		takeScreenShot(LOG_DIR + className + time + ".png");
	}

	/**
	 * switch to active element.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void switchToActiveElement() {
		boolean isSucceed = false;
		try {
			driver.switchTo().activeElement();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "switchToActiveElement");
	}

	/**
	 * make the alert dialog not to appears.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void ensrueBeforeAlert() {
		boolean isSucceed = false;
		try {
			((JavascriptExecutor) driver).executeScript("window.alert = function() {}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "ensrueBeforeAlert");
	}

	/**
	 * make the warn dialog not to appears when window.close().
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void ensureBeforeWinClose() {
		boolean isSucceed = false;
		String js = "window.close = function(){" + " window.opener=null; " + " window.open('','_self');"
				+ " window.close();}";
		try {
			((JavascriptExecutor) driver).executeScript(js);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "ensureBeforeWinClose");
	}

	/**
	 * make the confirm dialog not to appears choose default option OK.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseOKOnConfirm() {
		boolean isSucceed = false;
		try {
			((JavascriptExecutor) driver).executeScript("window.confirm = function() {return true}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseOKOnConfirm");
	}

	/**
	 * make the confirm dialog not to appears choose default option Cancel.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseCancelOnConfirm() {
		boolean isSucceed = false;
		try {
			((JavascriptExecutor) driver).executeScript("window.confirm = function() {return false}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseCancelOnConfirm");
	}

	/**
	 * make the prompt dialog not to appears choose default option OK.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseOKOnPrompt() {
		boolean isSucceed = false;
		try {
			((JavascriptExecutor) driver).executeScript("window.prompt = function() {return true}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseOKOnPrompt");
	}

	/**
	 * make the prompt dialog not to appears choose default option Cancel.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseCancelOnPrompt() {
		boolean isSucceed = false;
		try {
			((JavascriptExecutor) driver).executeScript("window.prompt = function() {return false}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseCancelOnPrompt");
	}

	/**
	 * choose OK/Cancel button's OK on alerts.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseOKOnAlert() {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().accept();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseConfirmOnAlert");
	}

	/**
	 * choose Cancel on alerts.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void chooseCancelOnAlert() {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().dismiss();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "chooseCancelOnAlert");
	}

	/**
	 * get the text of the alerts.
	 * 
	 * @return alert text string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getTextOfAlert() {
		boolean isSucceed = false;
		String alerts = null;
		try {
			alerts = driver.switchTo().alert().getText();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getTextOfAlert");
		return alerts;
	}

	/**
	 * set text on alerts.
	 * 
	 * @param text
	 *            the text string you want to input on alerts
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void setTextOnAlert(String text) {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().sendKeys(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "setTextOnAlert");
	}

	/**
	 * switch to new window supporting, by deleting first hanlder.
	 * 
	 * @param firstHandler
	 *            the first window handle
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectNewWindow(String firstHandler) {
		boolean isSucceed = false;
		Set<String> handlers = driver.getWindowHandles();
		handlers.remove(firstHandler);
		Iterator<String> it = handlers.iterator();
		driver.switchTo().activeElement();
		try {
			if (it.hasNext()) {
				driver.switchTo().window(it.next());
				isSucceed = true;
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectWindowByHandler");
	}

	/**
	 * switch to window by title, supporting non-modal windows.
	 * 
	 * @param windowTitle
	 *            the title of the window to be switched to
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectWindow(String windowTitle) {
		boolean isSucceed = false;
		Set<String> windowHandles = driver.getWindowHandles();
		try {
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String title = driver.getTitle();
				if (windowTitle.equals(title)) {
					isSucceed = true;
					break;
				}
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectWindowByTitle");
	}

	/**
	 * select a frame by index.
	 * 
	 * @param index
	 *            the index of the frame to select
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectFrame(int index) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(index);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectFrame");
	}

	/**
	 * select a frame by name or id.
	 * 
	 * @param nameOrId
	 *            the name or id of the frame to select
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectFrame(String nameOrId) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(nameOrId);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectFrame");
	}

	/**
	 * select a frame by frameElement.
	 * 
	 * @param frameElement
	 *            the frame element to select
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectFrame(WebElement frameElement) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(frameElement);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectFrame");
	}

	/**
	 * select a frame by frame element locator: By.
	 * 
	 * @param by
	 *            the frame element locator
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectFrame(By by) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(driver.findElement(by));
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectFrame");
	}

	/**
	 * edit a content editable iframe.
	 * 
	 * @param by
	 *            the frame element locaotr
	 * @param text
	 *            the text string to be input
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void editFrameText(By by, String text) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(driver.findElement(by));
			driver.switchTo().activeElement().sendKeys(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "editFrameText");
	}

	/**
	 * select a frame by frame element locator: By.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void switchToDefaultFrame() {
		boolean isSucceed = false;
		try {
			driver.switchTo().defaultContent();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "switchToDefaultFrame");
	}

	/**
	 * maximize browser window: support ie, ff3.6 and lower.
	 * 
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void maximizeWindow() {
		boolean isSucceed = false;
		String js = "if(document.all) { " + "self.moveTo(0, 0); "
				+ "self.resizeTo(screen.availWidth, screen.availHeight); " + "}";
		try {
			((JavascriptExecutor) driver).executeScript(js);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "maximizeWindow");
	}

	/**
	 * override the click method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void click(WebElement element) {
		boolean isSucceed = false;
		try {
			element.click();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "click");
	}

	/**
	 * override the click method, click on the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void click(By by) {
		boolean isSucceed = false;
		try {
			driver.findElement(by).click();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "click");
	}

	/**
	 * override the submit method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void submit(WebElement element) {
		boolean isSucceed = false;
		try {
			element.submit();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "submit");
	}

	/**
	 * override the submit method, submit on the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void submit(By by) {
		boolean isSucceed = false;
		try {
			driver.findElement(by).submit();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "submit");
	}

	/**
	 * override the sendKeys method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @param text
	 *            the text you want to input to element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void sendKeys(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			element.sendKeys(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "sendKeys");
	}

	/**
	 * override the sendKeys method, sendKeys on the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param text
	 *            the text you want to input to element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void sendKeys(By by, String text) {
		boolean isSucceed = false;
		try {
			driver.findElement(by).sendKeys(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "sendKeys");
	}

	/**
	 * select an item from a picklist by index.
	 * 
	 * @param element
	 *            the picklist element
	 * @param index
	 *            the index of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByIndex(WebElement element, int index) {
		boolean isSucceed = false;
		try {
			Select select = new Select(element);
			select.selectByIndex(index);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "ByIndex");
	}

	/**
	 * select an item from a picklist by index.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param index
	 *            the index of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByIndex(By by, int index) {
		boolean isSucceed = false;
		try {
			Select select = new Select(driver.findElement(by));
			select.selectByIndex(index);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "ByIndex");
	}

	/**
	 * select an item from a picklist by item value.
	 * 
	 * @param element
	 *            the picklist element
	 * @param itemValue
	 *            the item value of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByValue(WebElement element, String itemValue) {
		boolean isSucceed = false;
		try {
			Select select = new Select(element);
			select.selectByValue(itemValue);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectByValue");
	}

	/**
	 * select an item from a picklist by item value.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param itemValue
	 *            the item value of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByValue(By by, String itemValue) {
		boolean isSucceed = false;
		try {
			Select select = new Select(driver.findElement(by));
			select.selectByValue(itemValue);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectByValue");
	}

	/**
	 * select an item from a picklist by item value.
	 * 
	 * @param element
	 *            the picklist element
	 * @param text
	 *            the item value of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByVisibleText(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			Select select = new Select(element);
			select.selectByVisibleText(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectByVisibleText");
	}

	/**
	 * select an item from a picklist by item value.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param text
	 *            the item value of the item to be selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void selectByVisibleText(By by, String text) {
		boolean isSucceed = false;
		try {
			Select select = new Select(driver.findElement(by));
			select.selectByVisibleText(text);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectByVisibleText");
	}

	/**
	 * set the checkbox on or off
	 * 
	 * @param element
	 *            the checkbox element
	 * @param onOrOff
	 *            on or off to set the checkbox
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void setCheckBox(WebElement element, String onOrOff) {
		boolean isSucceed = false;
		try {
			WebElement checkElement = element.findElement(By.tagName("input"));
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				element.click();
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "select");
	}

	/**
	 * set the checkbox on or off
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param onOrOff
	 *            on or off to set the checkbox
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void setCheckBox(By by, String onOrOff) {
		boolean isSucceed = false;
		try {
			WebElement checkBox = driver.findElement(by);
			WebElement checkElement = checkBox.findElement(By.tagName("input"));
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				checkBox.click();
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "select");
	}

	/**
	 * override the clear method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void clear(WebElement element) {
		boolean isSucceed = false;
		try {
			element.clear();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "clear");
	}

	/**
	 * override the clear method, clear on the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void clear(By by) {
		boolean isSucceed = false;
		try {
			driver.findElement(by).clear();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "clear");
	}

	/**
	 * override the get method, adding user defined log.
	 * 
	 * @param url
	 *            the url you want to open
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void get(String url) {
		boolean isSucceed = false;
		try {
			driver.get(url);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "get");
	}

	/**
	 * override the getTitle method, adding user defined log.
	 * 
	 * @return the title on your current session
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getTitle() {
		boolean isSucceed = false;
		String title = null;
		try {
			title = driver.getTitle();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getTitle");
		return title;
	}

	/**
	 * override the getCurrentUrl method, adding user defined log.
	 * 
	 * @return the url on your current session
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getCurrentUrl() {
		boolean isSucceed = false;
		String url = null;
		try {
			url = driver.getCurrentUrl();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getCurrentUrl");
		return url;
	}

	/**
	 * override the findElements method, adding user defined log.
	 * 
	 * @param by
	 *            the locator of the elements to be find
	 * @return the webelements you want to find
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected List<WebElement> findElements(By by) {
		boolean isSucceed = false;
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(by);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "findElements");
		return elements;
	}

	/**
	 * override the findElement method, adding user defined log.
	 * 
	 * @param by
	 *            the locator of the element to be find
	 * @return the webelement you want to find
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected WebElement findElement(By by) {
		boolean isSucceed = false;
		WebElement element = null;
		try {
			element = driver.findElement(by);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "findElement");
		return element;
	}

	/**
	 * find webelement and transfer to select webelment, then return.
	 * 
	 * @param by
	 *            the locator of the element to be find
	 * @return the select webelement you want to find
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected Select selectElement(By by) {
		boolean isSucceed = false;
		Select element = null;
		try {
			element = new Select(driver.findElement(by));
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "selectElement");
		return element;
	}

	/**
	 * find elements displayed on the page.
	 * 
	 * @param by
	 *            the way to locate webelements
	 * @return displayed webelement list
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected List<WebElement> findDisplayedElments(By by) {
		boolean isSucceed = false;
		List<WebElement> elementList = new ArrayList<WebElement>();
		List<WebElement> elements = null;
		WebElement element;
		try {
			elements = driver.findElements(by);
			Iterator<WebElement> it = elements.iterator();
			while ((element = it.next()) != null && element.isDisplayed()) {
				elementList.add(element);
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "findDisplayedElments");
		return elementList;
	}

	/**
	 * find elements displayed on the page.
	 * 
	 * @param by
	 *            the way to locate webelement
	 * @return the first displayed webelement
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected WebElement findDisplayedElment(By by) {
		boolean isSucceed = false;
		WebElement element, retElement = null;
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(by);
			Iterator<WebElement> it = elements.iterator();
			while ((element = it.next()) != null && element.isDisplayed()) {
				retElement = element;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "findDisplayedElment");
		return retElement;
	}

	/**
	 * override the getWindowHandles method, adding user defined log.
	 * 
	 * @return the window handlers set
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected Set<String> getWindowHandles() {
		boolean isSucceed = false;
		Set<String> handler = null;
		try {
			handler = driver.getWindowHandles();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getWindowHandles");
		return handler;
	}

	/**
	 * override the getWindowHandle method, adding user defined log.
	 * 
	 * @return the window handler string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getWindowHandle() {
		boolean isSucceed = false;
		String handler = null;
		try {
			handler = driver.getWindowHandle();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getWindowHandle");
		return handler;
	}

	/**
	 * override the getPageSource method, adding user defined log.
	 * 
	 * @return the page source string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getPageSource() {
		boolean isSucceed = false;
		String source = null;
		try {
			source = driver.getPageSource();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getPageSource");
		return source;
	}

	/**
	 * override the getTagName method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @return the tagname string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getTagName(WebElement element) {
		boolean isSucceed = false;
		String tagName = null;
		try {
			tagName = element.getTagName();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getTagName");
		return tagName;
	}

	/**
	 * override the getTagName method, find the element by By and get its tag
	 * name.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @return the tagname string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getTagName(By by) {
		boolean isSucceed = false;
		String tagName = null;
		try {
			tagName = driver.findElement(by).getTagName();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getTagName");
		return tagName;
	}

	/**
	 * override the getAttribute method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @param attributeName
	 *            the name of the attribute you want to get
	 * @return the attribute value string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getAttribute(WebElement element, String attributeName) {
		boolean isSucceed = false;
		String attributeValue = null;
		try {
			attributeValue = element.getAttribute(attributeName);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getAttribute");
		return attributeValue;
	}

	/**
	 * override the getAttribute method, find the element by By and get its
	 * attribute value.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param attributeName
	 *            the name of the attribute you want to get
	 * @return the attribute value string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getAttribute(By by, String attributeName) {
		boolean isSucceed = false;
		String attributeValue = null;
		try {
			attributeValue = driver.findElement(by).getAttribute(attributeName);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getAttribute");
		return attributeValue;
	}

	/**
	 * override the isSelected method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @return the bool value of whether is the WebElement selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isSelected(WebElement element) {
		boolean isSucceed = false;
		boolean isSelected = false;
		try {
			isSelected = element.isSelected();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isSelected");
		return isSelected;
	}

	/**
	 * override the isSelected method, the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @return the bool value of whether is the WebElement selected
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isSelected(By by) {
		boolean isSucceed = false;
		boolean isSelected = false;
		try {
			isSelected = driver.findElement(by).isSelected();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isSelected");
		return isSelected;
	}

	/**
	 * override the isEnabled method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @return the bool value of whether is the WebElement enabled
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isEnabled(WebElement element) {
		boolean isSucceed = false;
		boolean isEnabled = false;
		try {
			isEnabled = element.isEnabled();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isEnabled");
		return isEnabled;
	}

	/**
	 * override the isEnabled method, the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @return the bool value of whether is the WebElement enabled
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isEnabled(By by) {
		boolean isSucceed = false;
		boolean isEnabled = false;
		try {
			isEnabled = driver.findElement(by).isEnabled();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isEnabled");
		return isEnabled;
	}

	/**
	 * override the getText method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getText(WebElement element) {
		boolean isSucceed = false;
		String text = null;
		try {
			text = element.getText();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getText");
		return text;
	}

	/**
	 * override the getText method, find the element by By and get its own text.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @return the text string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getText(By by) {
		boolean isSucceed = false;
		String text = null;
		try {
			text = driver.findElement(by).getText();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getText");
		return text;
	}

	/**
	 * override the isDisplayed method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @return the bool value of whether is the WebElement displayed
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isDisplayed(WebElement element) {
		boolean isSucceed = false;
		boolean isDisplayed = false;
		try {
			isDisplayed = element.isDisplayed();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isDisplayed");
		return isDisplayed;
	}

	/**
	 * override the isDisplayed method, the element to be find by By.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @return the bool value of whether is the WebElement displayed
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean isDisplayed(By by) {
		boolean isSucceed = false;
		boolean isDisplayed = false;
		try {
			isDisplayed = driver.findElement(by).isDisplayed();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "isDisplayed");
		return isDisplayed;
	}

	/**
	 * override the getCssValue method, adding user defined log.
	 * 
	 * @param element
	 *            the webelement you want to operate
	 * @param propertyName
	 *            the name of the property you want to get
	 * @return the css property value string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getCssValue(WebElement element, String propertyName) {
		boolean isSucceed = false;
		String cssValue = null;
		try {
			cssValue = element.getCssValue(propertyName);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getCssValue");
		return cssValue;
	}

	/**
	 * override the getCssValue method, find the element by By and get its css
	 * property value.
	 * 
	 * @param by
	 *            the locator you want to find the element
	 * @param propertyName
	 *            the name of the property you want to get
	 * @return the css property value string
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected String getCssValue(By by, String propertyName) {
		boolean isSucceed = false;
		String cssValue = null;
		try {
			cssValue = driver.findElement(by).getCssValue(propertyName);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "getCssValue");
		return cssValue;
	}

	/**
	 * get row count of a webtable
	 * 
	 * @param xpathLocator
	 *            the webtable locator like
	 *            "@id='abc' and contains(@name='123')"
	 * @return the row count of the table
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected int tableRowCount(String xpathLocator) {
		boolean isSucceed = false;
		int rowCount = 0;
		String countLocator = xpathLocator;
		String tableLocator = xpathLocator;
		if (!xpathLocator.startsWith("//")) {
			countLocator = "//table[" + xpathLocator + "]/tbody/descendant::tr";
			tableLocator = "//table[" + xpathLocator + "]/tbody";
		} else {
			countLocator = xpathLocator + "]/tbody/descendant::tr";
		}
		try {
			driver.findElement(By.xpath(tableLocator));// judge if table exists
			rowCount = driver.findElements(By.xpath(countLocator)).size();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "rowCountOfTable");
		return rowCount;
	}

	/**
	 * get column count of a specified webtable row.
	 * 
	 * @param xpathLocator
	 *            the webtable locator like
	 *            "@id='abc' and contains(@name='123')"
	 * @param rowNum
	 *            row index of your table to count
	 * @return the column count of the row in table
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected int tableColCount(String xpathLocator, int rowNum) {
		boolean isSucceed = false;
		int colCount = 0;
		String countLocator = xpathLocator;
		String tableLocator = xpathLocator;
		if (!xpathLocator.startsWith("//")) {
			countLocator = "//table[" + xpathLocator + "]/tbody/tr[" + rowNum + "]/descendant::td";
			tableLocator = "//table[" + xpathLocator + "]/tbody";
		} else {
			countLocator = xpathLocator + "]/tbody/descendant::tr";
		}
		try {
			driver.findElement(By.xpath(tableLocator));// judge if table exists
			colCount = driver.findElements(By.xpath(countLocator)).size();
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "colCountOfTable");
		return colCount;
	}

	/**
	 * wait for the element visiable in timeout setting.
	 * 
	 * @param by
	 *            the element locator By
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean waitForElementVisiable(By by, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.visibilityOfElementLocated(by)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "waitForElementVisiable");
		return isExists;
	}

	/**
	 * wait for the element clickable in timeout setting.
	 * 
	 * @param by
	 *            the element locator By
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean waitForElementClickable(By by, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.elementToBeClickable(by)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "waitForElementClickable");
		return isExists;
	}

	/**
	 * wait for text appears on element in timeout setting.
	 * 
	 * @param by
	 *            the element locator By
	 * @param text
	 *            the text to be found of element
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean waitForTextOnElement(By by, String text, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.textToBePresentInElement(by, text)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "waitForTextOnElement");
		return isExists;
	}

	/**
	 * wait for text appears in element attributes in timeout setting.
	 * 
	 * @param by
	 *            the element locator By
	 * @param text
	 *            the text to be found in element attributes
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean waitForTextOfElementAttr(By by, String text, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.textToBePresentInElementValue(by, text)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "waitForTextOfElementAttr");
		return isExists;
	}

	/**
	 * judge if the alert is present in specified seconds.
	 * 
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean alertExists(int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.alertIsPresent()) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "alertExists");
		return isExists;
	}

	/**
	 * judge if the element is present in specified seconds.
	 * 
	 * @param by
	 *            the element locator By
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean elementExists(By by, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.presenceOfElementLocated(by)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "elementExists");
		return isExists;
	}

	/**
	 * judge if the browser is present by title reg pattern in specified
	 * seconds.
	 * 
	 * @param title
	 *            part of the title to see if browser exists
	 * @param senconds
	 *            timeout in senconds
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected boolean browserExists(String title, int senconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, senconds);
		try {
			if (wait.until(ExpectedConditions.titleContains(title)) != null) {
				isExists = true;
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "browserExists");
		return isExists;
	}

	/**
	 * use js to make the element to be un-hidden.
	 * 
	 * @param element
	 *            the element to be operate
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void makeElementUnHidden(WebElement element) {
		boolean isSucceed = false;
		final String js = "arguments[0].style.visibility = 'visible'; " + "arguments[0].style.height = '1px'; "
				+ "arguments[0].style.width = '1px'; " + "arguments[0].style.opacity = 1";
		try {
			((JavascriptExecutor) driver).executeScript(js, element);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "makeElementVisiable");
	}

	/**
	 * use js to make the element to be un-hidden.
	 * 
	 * @param by
	 *            the By locator to find the element
	 * @author PAICDOM/LIUYI027
	 * @throws RuntimeException
	 **/
	protected void makeElementUnHidden(By by) {
		boolean isSucceed = false;
		final String js = "arguments[0].style.visibility = 'visible'; " + "arguments[0].style.height = '1px'; "
				+ "arguments[0].style.width = '1px'; " + "arguments[0].style.opacity = 1";
		try {
			WebElement element = driver.findElement(by);
			((JavascriptExecutor) driver).executeScript(js, element);
			isSucceed = true;
		} catch (WebDriverException e) {

			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		testAssertions(isSucceed, "makeElementVisiable");
	}
}