package com.star.toolapi.webdriver;

/**
 * 封装整体思路：
 * 1、封装常用方法，每个方法对WebDriverException进行捕获，其余的直接抛出RuntimeException；
 * 2、对于封装过的方法，存在WebDriverException的操作为失败，否则默认为成功，失败的操作在
 * 	  operationCheck中进行截图、报告错误、抛出RuntimeException操作，强制出错则停止运行；
 * 3、部分方法是使用javascript执行来达到目的，建议非不得已尽量避免使用。
 *  
 * @author 测试仔刘毅
 */

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.apache.commons.io.FileUtils;
import com.star.logging.frame.LoggingManager;
import com.star.toolapi.user.web.JSCollection;
import com.star.toolapi.user.web.WebTable;

public class WebDriverWebPublic extends WebDriverController {

	private static final LoggingManager LOG = new LoggingManager(WebDriverWebPublic.class.getName());
	protected static final String FORMATTER = "_yyyyMMddHHmmssSSS";
	private static int maxWaitfor = 10;
	private static long sleepUnit = 500;
	protected static By tabFinder = null;
	protected static WebTable webTable = null;

	/**
	 * set sleep interval for loop wait.
	 * 
	 * @param 	interval milliseconds for each sleep
	 */
	protected void setSleepInterval(long interval) {
		WebDriverWebPublic.sleepUnit = interval;
	}

	/**
	 * config timeout setting for each step, default is 10 seconds</BR>
	 * 配置单个步骤运行的最大超时时间，默认是10秒钟。
	 * 
	 * @param 	timeout max wait time setting in seconds
	 */
	protected void setMaxWaitTime(int timeout) {
		WebDriverWebPublic.maxWaitfor = timeout;
	}

	/**
	 * wait util the element visible in max wait time setting</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations</BR>
	 * 在指定时间内循环等待，直到对象可见，超时之后直接抛出对象不可见异常信息。
	 * 
	 * @param element the WebElement to be judged
	 * @param timeout timeout setting in seconds
	 * @throws ElementNotVisibleException
	 */
	protected void waitUtilElementVisible(WebElement element, int timeout) {
		long start = System.currentTimeMillis();
		boolean isDisplayed = false;
		while (!isDisplayed && ((System.currentTimeMillis() - start) < timeout * 1000)) {
			isDisplayed = (element == null)? false : element.isDisplayed();
			pause(sleepUnit);
		}
		if (!isDisplayed){
			throw new ElementNotVisibleException("the element is not visible in " + timeout + "seconds!");
		}
	}

	/**
	 * wait util the element visible in max wait time setting</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations</BR>
	 * 在指定时间内循环等待，直到对象可见，使用用户指定的默认超时设置。
	 * 
	 * @param element the WebElement to be judged
	 * @throws ElementNotVisibleException
	 */
	protected void waitUtilElementVisible(WebElement element) {
		waitUtilElementVisible(element, maxWaitfor);
	}

	/**
	 * wait util the element visible in max wait time setting</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations</BR>
	 * 在指定时间内循环等待，直到对象可见，使用用户指定的默认超时设置。
	 * 
	 * @param by the WebElement locator
	 * @param timeout timeout setting in seconds
	 * @throws ElementNotVisibleException
	 */
	protected void waitUtilElementVisible(By by, int timeout) {
		waitUtilElementVisible(driver.findElement(by), timeout);
	}

	/**
	 * wait util the element visible in max wait time setting</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations</BR>
	 * 在指定时间内循环等待，直到对象可见，使用用户指定的默认超时设置。
	 * 
	 * @param by the WebElement locator
	 * @throws ElementNotVisibleException
	 */
	protected void waitUtilElementVisible(By by) {
		waitUtilElementVisible(driver.findElement(by));
	}

	/**
	 * take screenshot and report to users when operation fails</BR>
	 * 在发生操作异常之后进行截图和日志记录操作。
	 */
	private void failValidation() {
		String method = Thread.currentThread().getStackTrace()[2].getMethodName();
		String file = LOG_REL + this.getClass().getName() + STRUTIL.formatedTime(FORMATTER) + ".png";
		String errorMessage = null;
		try{
			takeScreenShot(file);
			fail("method [" + method + "] failed, screenshot is: [" + file + "]");
		}catch(UnhandledAlertException alert){
			try {
				errorMessage = driver.switchTo().alert().getText();
				fail("method [" + method + "] failed, there is modal dialog present: [" + errorMessage + "]");
				driver.switchTo().alert().accept();
			}catch(Exception e){
				throw new RuntimeException("unable to handle modal dialog: " + e.getMessage());
			}
		}
	}

	/**
	 * execute js functions to do something</BR>
	 * 使用remote webdriver执行JS函数。
	 * 
	 * @param js js function string
	 * @param report text content to be reported
	 * @param args js execute parameters
	 * 
	 * @throws RuntimeException
	 */
	protected void jsExecutor(String js, String report, Object args){
		try {
			driver.executeScript(js, args);
			pass(report);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * execute js functions to do something</BR>
	 * 使用remote webdriver执行JS函数。
	 * 
	 * @param js js function string
	 * @param report text content to be reported
	 * 
	 * @throws RuntimeException
	 */
	protected void jsExecutor(String js, String report){
		try {
			driver.executeScript(js);
			pass(report);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * take a screen shot and save the file by path and name</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param fileName the file path&name of the screenshot to be saved
	 * 
	 * @throws RuntimeException
	 */
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
	 * override the screenShot method, using default path and name</BR>
	 * 网页截图操作，默认路径为工程日志目录，文件名为运行的class名和时间戳拼接而成。
	 * 
	 * @throws RuntimeException
	 */
	protected void takeScreenShot() {
		String time = STRUTIL.formatedTime(FORMATTER);
		String fileName = LOG_ABS + this.getClass().getName() + time + ".png";
		takeScreenShot(fileName);
		pass("screenshot saved, you can see: " + fileName);
	}

	/**
	 * judge if the alert is existing</BR>
	 * 判断弹出的对话框（Dialog）是否存在。
	 * 
	 * @throws RuntimeException
	 */
	protected boolean alertExists() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException ne) {
			warn("no alert is present");
			return false;
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * judge if the alert is present in specified seconds</BR>
	 * 在指定的时间内判断弹出的对话框（Dialog）是否存在。
	 * 
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean alertExists(int seconds) {
		long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < seconds * 1000) {
			try {
				driver.switchTo().alert();
				return true;
			} catch (NoAlertPresentException ne) {
				pause(sleepUnit);
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	/**
	 * judge if the element is existing</BR>
	 * 判断指定的对象是否存在。
	 * 
	 * @param by the element locator By
	 */
	protected boolean elementExists(By by) {
		try {
			return (driver.findElements(by).size() > 0) ? true : false;
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * judge if the element is present in specified seconds</BR>
	 * 在指定的时间内判断指定的对象是否存在。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 */
	protected boolean elementExists(final By by, int seconds) {
		long start = System.currentTimeMillis();
		boolean exists = false;
		while (!exists && ((System.currentTimeMillis() - start) < seconds * 1000)) {
			try {
				exists = driver.findElements(by).size() > 0;
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException(e);
			}
		}
		return exists;
	}

	/**
	 * judge if the browser is existing, using part of the page title</BR>
	 * 按照网页标题判断页面是否存在，标题可使用部分内容匹配。
	 * 
	 * @param browserTitle part of the title to see if browser exists
	 * @throws RuntimeException
	 */
	protected boolean browserExists(String browserTitle) {
		String defaultHandler = driver.getWindowHandle();
		Set<String> windowHandles = null;
		try {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String currentTitle = driver.getTitle();
				if (currentTitle.contains(browserTitle)){
					return true;
				}
			}
		} catch (Exception e) {
		} finally {
			driver.switchTo().window(defaultHandler);
		}
		return false;
	}

	/**
	 * judge if the browser is present by title reg pattern in specified seconds</BR>
	 * 在指定时间内按照网页标题判断页面是否存在，标题可使用部分内容匹配。
	 * 
	 * @param browserTitle part of the title to see if browser exists
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean browserExists(String browserTitle, int seconds) {
		long start = System.currentTimeMillis();
		boolean isExist = false;
		while (!isExist && (System.currentTimeMillis() - start) < seconds * 1000) {
			isExist = browserExists(browserTitle);
		}
		return isExist;
	}

	/**
	 * maximize browser window: support ie, ff3.6 and lower</BR>
	 * 网页窗口最大化操作。
	 */
	protected void maximizeWindow() {
		jsExecutor(JSCollection.MAXIMIZEWINDOW.getName(), "current window maximized");
	}

	/**
	 * maximize browser window</BR>
	 * 网页窗口最大化操作。
	 */
	protected void windowMaximize() {
		driver.manage().window().maximize();
	}

	/**
	 * select default window and default frame</BR>
	 * 在当前页面中自动选择默认的页面框架（frame）。
	 * 
	 * @throws RuntimeException
	 */
	protected void selectDefaultWindowFrame() {
		try {
			driver.switchTo().defaultContent();
			pass("switch to default frame on window");
		}  catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * switch to active element</BR>
	 * 在当前操作的页面和对象时自动选择已被激活的对象。
	 * 
	 * @throws RuntimeException
	 */
	protected void focusOnActiveElement() {
		try {
			driver.switchTo().activeElement();
			pass("switch to active element");
		}  catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * switch to new window supporting, by deleting first hanlder</BR>
	 * 选择最新弹出的窗口，需要预存第一个窗口的handler。
	 * 
	 * @param firstHandler the first window handle
	 * @throws RuntimeException
	 */
	protected void selectNewWindow(String firstHandler) {
		Set<String> handlers = null;
		Iterator<String> it = null;
		try {
			handlers = driver.getWindowHandles();
			handlers = driver.getWindowHandles();
			handlers.remove(firstHandler);
			it = handlers.iterator();
			while (it.hasNext()) {
				driver.switchTo().window(it.next());
			}
			pass("switch to new window");
		}  catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * switch to window by title</BR>
	 * 按照网页标题选择窗口，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be switched to
	 * @throws RuntimeException
	 */
	protected void selectWindow(String windowTitle) {
		Set<String> windowHandles = null;
		try {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String title = driver.getTitle();
				if (windowTitle.equals(title)) {
					pass("switch to window [ " + windowTitle + " ]");
					return;
				}
			}
			LOG.error("there is no window named [ " + windowTitle + " ]");
			failAndExit("there is no window named [ " + windowTitle + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * close window by window title and its index if has the same title, by string full pattern</BR>
	 * 按照网页标题选择并且关闭窗口，重名窗口按照指定的重名的序号关闭，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be closed.
	 * @param index the index of the window which shared the same title, begins with 1.
	 * @throws RuntimeException
	 */
	protected void closeWindow(String windowTitle, int index) {
		Object[] winArray = null;
		List<String> winList = new ArrayList<String>();
		try {
			winArray = driver.getWindowHandles().toArray();
			winArray = driver.getWindowHandles().toArray();
			for (int i = 0; i < winArray.length - 1; i++) {
				driver.switchTo().window(winArray[i].toString());
				if (windowTitle.equals(driver.getTitle())) {
					winList.add(winArray[i].toString());
				}
			}
			driver.switchTo().window(winList.get(index - 1));
			driver.switchTo().defaultContent();
			driver.close();
			pass("window [ " + windowTitle + " ] closed by index [" + index + "]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * close the last window by the same window title, by string full pattern</BR>
	 * 按照网页标题选择窗口，适用于无重名的窗口，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be closed.
	 * @throws RuntimeException
	 */
	protected void closeWindow(String windowTitle) {
		Object[] winArray = null;
		try {
			winArray = driver.getWindowHandles().toArray();
			winArray = driver.getWindowHandles().toArray();
			for (int i = winArray.length - 1; i > 0; i--) {
				driver.switchTo().window(winArray[i].toString());
				if (windowTitle.equals(driver.getTitle())) {
					driver.switchTo().defaultContent();
					driver.close();
					break;
				}
			}
			pass("window [ " + windowTitle + " ] closed ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * close windows except specified window title, by string full pattern</BR>
	 * 关闭除了指定标题页面之外的所有窗口，适用于例外窗口无重名的情况，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window not to be closed
	 * @throws RuntimeException
	 */
	protected void closeWindowExcept(String windowTitle) {
		Set<String> windowHandles = null;
		try {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String title = driver.getTitle();
				if (!windowTitle.equals(title)) {
					driver.switchTo().defaultContent();
					driver.close();
				}
			}
			pass("all windows closed except [ " + windowTitle + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * close windows except specified window title, by string full pattern</BR>
	 * 关闭除了指定标题页面之外的所有窗口，例外窗口如果重名，按照指定的重名顺序关闭，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window not to be closed
	 * @param index the index of the window to keep shared the same title with others, begins with 1.
	 * @throws RuntimeException
	 */
	protected void closeWindowExcept(String windowTitle, int index) {
		Set<String> windowHandles = null;
		Object[] winArray = null;
		try {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String title = driver.getTitle();
				if (!windowTitle.equals(title)) {
					driver.switchTo().defaultContent();
					driver.close();
				}
			}

			winArray = driver.getWindowHandles().toArray();
			winArray = driver.getWindowHandles().toArray();
			for (int i = 0; i < winArray.length; i++) {
				driver.switchTo().window(winArray[i].toString());
				if (i + 1 != index) {
					driver.switchTo().defaultContent();
					driver.close();
				}
			}
			pass("keep only window [ " + windowTitle + " ] by title index [ " + index + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for new window which has no title in few seconds</BR>
	 * 判断在指定的时间内是否有新的窗口弹出，无论其是否有标题。
	 * 
	 * @param browserCount windows count before new window appears.
	 * @param seconds time unit in seconds.
	 */
	protected boolean isNewWindowExits(int browserCount, int seconds) {
		Set<String> windowHandles = null;
		boolean isExist = false;
		long begins = System.currentTimeMillis();
		while ((System.currentTimeMillis() - begins < seconds * 1000) && !isExist) {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			isExist = (windowHandles.size() > browserCount) ? true : false;
		}
		return isExist;
	}

	/**
	 * wait for new window which has no title in few seconds</BR>
	 * 判断在指定的时间内是否有新的窗口弹出，无论其是否有标题。
	 * 
	 * @param oldHandlers windows handler Set before new window appears.
	 * @param seconds time unit in seconds.
	 */
	protected boolean isNewWindowExits(Set<String> oldHandlers, int seconds) {
		boolean isExist = false;
		Set<String> windowHandles = null;
		long begins = System.currentTimeMillis();
		while ((System.currentTimeMillis() - begins < seconds * 1000) && !isExist) {
			windowHandles = driver.getWindowHandles();
			windowHandles = driver.getWindowHandles();
			isExist = (windowHandles.size() > oldHandlers.size()) ? true : false;
		}
		return isExist;
	}

	/**
	 * select a frame by index</BR>
	 * 按照序号选择框架（frame）。
	 * 
	 * @param index the index of the frame to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(int index) {
		try {
			driver.switchTo().frame(index);
			pass("select frame by index [ " + index + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select a frame by name or id</BR>
	 * 按照名称或者ID选择框架（frame）。
	 * 
	 * @param nameOrId the name or id of the frame to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(String nameOrId) {
		try {
			driver.switchTo().frame(nameOrId);
			pass("select frame by name or id [ " + nameOrId + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select a frame by frameElement</BR>
	 * 按照框架对象本身选择框架（frame）。
	 * 
	 * @param frameElement the frame element to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(WebElement frameElement) {
		try {
			driver.switchTo().frame(frameElement);
			pass("select frame by frameElement");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select a frame by frame element locator: By</BR>
	 * 按照指定的元素定位方式选择框架（frame）。
	 * 
	 * @param by the frame element locator
	 * @throws RuntimeException
	 */
	protected void selectFrame(By by) {
		try {
			driver.switchTo().frame(driver.findElement(by));
			pass("select frame by frame locator [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * edit a content editable iframe</BR>
	 * 编辑指定框架（frame）内的最直接展示文本内容。
	 * 
	 * @param by the frame element locaotr
	 * @param text the text string to be input
	 * @throws RuntimeException
	 */
	protected void editFrameText(By by, String text) {
		try {
			driver.switchTo().frame(driver.findElement(by));
			driver.switchTo().activeElement().sendKeys(text);
			pass("input text [ " + text + " ] to frame [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the get method, adding user defined log</BR>
	 * 地址跳转方法，与WebDriver原生get方法内容完全一致。
	 * 
	 * @param url the url you want to open
	 * @throws RuntimeException
	 */
	protected void get(String url) {
		try {
			driver.get(url);
			pass("navigate to url [ " + url + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * navigate to some where by url</BR>
	 * 地址跳转方法，与WebDriver原生navigate.to方法内容完全一致。
	 * 
	 * @param url the url you want to open
	 * @throws RuntimeException
	 */
	protected void navigateTo(String url){
		try {
			driver.navigate().to(url);
			pass("navigate to url [ " + url + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * navigate back</BR>
	 * 地址跳转方法，与WebDriver原生navigate.back方法内容完全一致。
	 * 
	 * @throws RuntimeException
	 */
	protected void navigateBack(){
		try {
			driver.navigate().back();
			pass("navigate back");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * navigate forward</BR>
	 * 地址跳转方法，与WebDriver原生navigate.forward方法内容完全一致。
	 * 
	 * @throws RuntimeException
	 */
	protected void navigateForward(){
		try {
			driver.navigate().forward();
			pass("navigate forward");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}	
	}

	/**
	 * override the click method, adding user defined log</BR>
	 * 在等到对象可见之后点击指定的对象。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void click(WebElement element) {
		try {
			waitUtilElementVisible(element);
			element.click();
			pass("click on WebElement");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the click method, click on the element to be find by By</BR>
	 * 在等到对象可见之后点击指定的对象。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void click(By by) {
		try {
			waitUtilElementVisible(driver.findElement(by));
			driver.findElement(by).click();
			pass("click on element [ " + by.toString() + " ] ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * forcely click, by executing javascript</BR>
	 * 在等到对象可见之后点击指定的对象，使用JavaScript执行的方式去操作，</BR>
	 * 这种方法使用过后一般需要调用一次selectDefaultWindowFrame以确保运行稳定。
	 * 
	 * @param element the webelement you want to operate
	 */
	protected void clickByJavaScript(WebElement element) {
		waitUtilElementVisible(element);
		jsExecutor(JSCollection.CLICKBYJAVASCRIPT.getName(), "click on element", element);
	}

	/**
	 * forcely click, by executing javascript</BR>
	 * 在等到对象可见之后点击指定的对象，使用JavaScript执行的方式去操作，</BR>
	 * 这种方法使用过后一般需要调用一次selectDefaultWindowFrame以确保运行稳定。
	 * 
	 * @param by the locator you want to find the element
	 */
	protected void clickByJavaScript(By by) {
		waitUtilElementVisible(driver.findElement(by));
		jsExecutor(JSCollection.CLICKBYJAVASCRIPT.getName(), 
				"click on element [ " + by.toString() + " ] ", driver.findElement(by));
	}

	/**
	 * doubleclick on the element to be find by By</BR>
	 * 在等到对象可见之后双击指定的对象.
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void doubleClick(WebElement element) {
		try {
			waitUtilElementVisible(element);
			actionDriver.doubleClick(element);
			actionDriver.perform();
			pass("doubleClick on element ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * doubleclick on the element</BR>
	 * 在等到对象可见之后双击指定的对象.
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void doubleClick(By by) {
		try {
			waitUtilElementVisible(driver.findElement(by));
			actionDriver.doubleClick(findElement(by));
			actionDriver.perform();
			pass("doubleClick on element [ " + by.toString() + " ] ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * right click on the element to be find by By</BR>
	 * 在等到对象可见之后鼠标右键点击指定的对象.
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void rightClick(WebElement element) {
		try {
			waitUtilElementVisible(element);
			actionDriver.contextClick(element);
			actionDriver.perform();
			pass("rightClick on element ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * right click on the element</BR>
	 * 在等到对象可见之后鼠标右键点击指定的对象。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void rightClick(By by) {
		try {
			waitUtilElementVisible(driver.findElement(by));
			actionDriver.contextClick(findElement(by));
			actionDriver.perform();
			pass("rightClick on element [ " + by.toString() + " ] ");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the submit method, adding user defined log</BR>
	 * 在等到指定对象可见之后在该对象上做确认/提交的操作。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void submit(WebElement element) {
		try {
			waitUtilElementVisible(element);
			element.submit();
			pass("submit on element");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the submit method, submit on the element to be find by By</BR>
	 * 在等到指定对象可见之后在该对象上做确认/提交的操作。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void submit(By by) {
		try {
			waitUtilElementVisible(driver.findElement(by));
			driver.findElement(by).submit();
			pass("submit on element [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the clear method, adding user defined log</BR>
	 * 在等到指定对象可见之后在该对象上做清理操作，一般用于输入框和选择框。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void clear(WebElement element) {
		try {
			waitUtilElementVisible(element);
			element.clear();
			pass("element cleared");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the clear method, clear on the element to be find by By</BR>
	 * 在等到指定对象可见之后在该对象上做清理操作，一般用于输入框和选择框。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void clear(By by) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.clear();
			pass("element [ " + by.toString() + " ] cleared");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the sendKeys method, adding user defined log</BR>
	 * 以追加文本的模式在指定可编辑对象中输入文本，操作之前自动等待到对象可见。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysAppend(WebElement element, String text) {
		try {
			waitUtilElementVisible(element);
			element.sendKeys(text);
			pass("send text [ " + text + " ] to element");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the sendKeys method, sendKeys on the element to be find by By</BR>
	 * 以追加文本的模式在指定可编辑对象中输入文本，操作之前自动等待到对象可见。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysAppend(By by, String text) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.sendKeys(text);
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the sendKeys method, adding user defined log</BR>
	 * 清理指定对象中已经输入的内容重新输入，操作之前自动等待到对象可见。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeys(WebElement element, String text) {
		try {
			waitUtilElementVisible(element);
			element.clear();
			element.sendKeys(text);
			pass("send text [ " + text + " ] to WebEdit");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * override the sendKeys method, sendKeys on the element to be find by By</BR>
	 * 清理指定对象中已经输入的内容重新输入，操作之前自动等待到对象可见。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeys(By by, String text) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.clear();
			element.sendKeys(text);
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * readonly text box or richtext box input</BR>
	 * 使用DOM（Documnet Object Modal）修改页面中对象的文本属性值，使用ID定位对象则返回唯一对象，其余返回数组。
	 * 
	 * @param by the attribute of the element, default support is TagName/Name/Id
	 * @param byValue the attribute value of the element
	 * @param text the text you want to input to element
	 * @param index the index of the elements shared the same attribute value
	 * @throws IllegalArgumentException
	 */
	protected void sendKeysByDOM(String by, String byValue, String text, int index) {
		String js = null;

		if (by.equalsIgnoreCase("tagname")) {
			js = "document.getElementsByTagName('" + byValue + "')[" + index + "].value='" + text + "'";
		} else if (by.equalsIgnoreCase("name")) {
			js = "document.getElementsByName('" + byValue + "')[" + index + "].value='" + text + "'";
		} else if (by.equalsIgnoreCase("id")) {
			js = "document.getElementById('" + byValue + "').value='" + text + "'";
		} else {
			throw new IllegalArgumentException("only can find element by TagName/Name/Id");
		}

		jsExecutor(js, "input text [ " + text + " ] to element [ " + by + " ]");
	}

	/**
	 * readonly text box or richtext box input, finding elements by element id</BR>
	 * 按照ID定位页面中对象，并使用DOM（Documnet Object Modal）修改其文本属性值。
	 * 
	 * @param elementId the id of the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 * @throws IllegalArgumentException
	 */
	protected void sendKeysById(String elementId, String text) {
		sendKeysByDOM("Id", elementId, text, 0);
	}

	/**
	 * readonly text box or richtext box input, finding elements by element name</BR>
	 * 按照名称（Name）和序号定位页面中对象，并使用DOM（Documnet Object Modal）修改其文本属性值。
	 * 
	 * @param elementName the name of the element
	 * @param text the text you want to input to element
	 * @param elementIndex the index of the elements shared the same name, begins with 0
	 * @throws RuntimeException
	 * @throws IllegalArgumentException
	 */
	protected void sendKeysByName(String elementName, String text, int elementIndex) {
		sendKeysByDOM("Name", elementName, text, elementIndex);
	}

	/**
	 * readonly text box or richtext box input, finding elements by element tag name</BR>
	 * 按照标签名称（TagName）和序号定位页面中对象，并使用DOM（Documnet Object Modal）修改其文本属性值。
	 * 
	 * @param elementTagName the tag name of the element
	 * @param text the text you want to input to element
	 * @param elementIndex the index of the elements shared the same tag name, begins with 0
	 * @throws RuntimeException
	 * @throws IllegalArgumentException
	 */
	protected void sendKeysByTagName(String elementTagName, String text, int elementIndex) {
		sendKeysByDOM("TagName", elementTagName, text, elementIndex);
	}

	/**
	 * sendKeys by using keybord event on element</BR>
	 * 使用键盘模拟的方法在指定的对象上输入指定的文本。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysByKeybord(WebElement element, String text) {
		try {
			waitUtilElementVisible(element);
			actionDriver.sendKeys(element, text);
			actionDriver.perform();
			pass("send text [ " + text + " ] to WebEdit");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * sendKeys by using keybord event on element to be found by By</BR>
	 * 使用键盘模拟的方法在指定的对象上输入指定的文本。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysByKeybord(By by, String text) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			actionDriver.sendKeys(element, text);
			actionDriver.perform();
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * edit rich text box created by kindeditor</BR>
	 * 使用JS调用KindEditor对象本身的接口，在页面KindEditor对象中输入指定的文本。
	 * 
	 * @param editorId kindeditor id
	 * @param text the text you want to input to element
	 */
	protected void sendKeysOnKindEditor(String editorId, String text) {
		String javascript = "KE.html('" + editorId + "','<p>" + text + "</p>');";
		jsExecutor(javascript, "input text [ " + text + " ] to kindeditor");
	}

	/**
	 * select an item from a picklist by index</BR>
	 * 按照指定序号选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param index the index of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByIndex(WebElement element, int index) {
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByIndex(index);
			pass("item selected by index [ " + index + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select an item from a picklist by index</BR>
	 * 按照指定序号选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param index the index of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByIndex(By by, int index) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByIndex(index);
			pass("item selected by index [ " + index + " ] on [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select an item from a picklist by item value</BR>
	 * 按照指定选项的实际值（不是可见文本值，而是对象的“value”属性的值）选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param itemValue the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByValue(WebElement element, String itemValue) {
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByValue(itemValue);
			pass("item selected by item value [ " + itemValue + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select an item from a picklist by item value</BR>
	 * 按照指定选项的实际值（不是可见文本值，而是对象的“value”属性的值）选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param itemValue the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByValue(By by, String itemValue) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByValue(itemValue);
			pass("item selected by item value [ " + itemValue + " ] on [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select an item from a picklist by item value</BR>
	 * 按照指定选项的可见文本值（用户直接可以看到的文本）选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param text the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByVisibleText(WebElement element, String text) {
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByVisibleText(text);
			pass("item selected by visible text [ " + text + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * select an item from a picklist by item value</BR>
	 * 按照指定选项的可见文本值（用户直接可以看到的文本）选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByVisibleText(By by, String text) {
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByVisibleText(text);
			pass("item selected by visible text [ " + text + " ] on [ " + by.toString() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * set the checkbox on or off</BR>
	 * 将指定的复选框对象设置为选中或者不选中状态。
	 * 
	 * @param element the checkbox element
	 * @param onOrOff on or off to set the checkbox
	 * @throws RuntimeException
	 */
	protected void setCheckBox(WebElement element, String onOrOff) {
		try {
			WebElement checkElement = element.findElement(By.tagName("input"));
			waitUtilElementVisible(checkElement);
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				element.click();
			}
			pass("the checkbox is set to [ " + onOrOff.toUpperCase() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * set the checkbox on or off</BR>
	 * 将指定的复选框对象设置为选中或者不选中状态。
	 * 
	 * @param by the locator you want to find the element
	 * @param onOrOff on or off to set the checkbox
	 * @throws RuntimeException
	 */
	protected void setCheckBox(By by, String onOrOff) {
		try {
			WebElement checkBox = driver.findElement(by);
			waitUtilElementVisible(checkBox);
			WebElement checkElement = checkBox.findElement(By.tagName("input"));
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				checkBox.click();
			}
			pass("the checkbox [ " + by.toString() + " ] is set to [ " 
					+ onOrOff.toUpperCase() + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * find elements displayed on the page</BR>
	 * 按照指定的定位方式寻找所有可见的对象。
	 * 
	 * @param by the way to locate webelements
	 * @return displayed webelement list
	 * @throws RuntimeException
	 */
	protected List<WebElement> findDisplayedElments(By by) {
		List<WebElement> elementList = new ArrayList<WebElement>();
		List<WebElement> elements = null;
		WebElement element;
		try {
			elements = driver.findElements(by);
			Iterator<WebElement> it = elements.iterator();
			while ((element = it.next()) != null && element.isDisplayed()) {
				elementList.add(element);
			}
			int eleNum = elementList.size();
			if (eleNum > 0){
				pass("got" + eleNum + "displayed elements [ " + by.toString() + " ]");
			}else{
				warn("there is not displayed element found by [" + by.toString() + " ]");
			}
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return elementList;
	}

	/**
	 * find elements displayed on the page</BR>
	 * 按照指定的定位方式寻找第一可见的对象。
	 * 
	 * @param by the way to locate webelement
	 * @return the first displayed webelement
	 * @throws RuntimeException
	 */
	protected WebElement findDisplayedElment(By by) {
		WebElement element, retElement = null;
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(by);
			Iterator<WebElement> it = elements.iterator();
			while ((element = it.next()) != null && element.isDisplayed()) {
				retElement = element;
			}
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return retElement;
	}

	/**
	 * override the findElements method, adding user defined log</BR>
	 * 按照指定的定位方式寻找象。
	 * 
	 * @param by the locator of the elements to be find
	 * @return the webelements you want to find
	 * @throws RuntimeException
	 */
	protected List<WebElement> findElements(By by) {
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(by);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return elements;
	}

	/**
	 * override the findElement method, adding user defined log</BR>
	 * 按照指定的定位方式寻找象。
	 * 
	 * @param by the locator of the element to be find
	 * @return the first element accord your locator
	 * @throws RuntimeException
	 */
	protected WebElement findElement(By by) {
		WebElement element = null;
		try {
			List<WebElement> elements = driver.findElements(by);
			if (elements.size() > 0) {
				element = elements.get(0);
			}
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return element;
	}

	/**
	 * store the WebDriverWebTable object, it only changes on By changing</BR>
	 * 缓存WebTable对象，在WebTable对象不为空的情况下（为空则直接新建对象），</BR>
	 * 如果定位方式相同则直接返回原有对象，否则重新创建WebTable对象。
	 * 
	 * @param tabBy the element locator By
	 */
	private synchronized WebTable tableCache(By tabBy) {
		waitUtilElementVisible(tabBy);
		if (tabFinder == null) {
			tabFinder = tabBy;
			return new WebTable(driver, tabBy);
		} else {
			if (tabBy.toString().equals(tabFinder.toString())) {
				return webTable;
			} else {
				tabFinder = tabBy;
				return new WebTable(driver, tabBy);
			}
		}
	}

	/**
	 * refresh the webtable on the same locator, only if it changes</BR>
	 * 如果同一定位方式的WebTable内容发生变化需要重新定位，则需要刷新WebTable。
	 */
	protected synchronized void tableRefresh(){
		WebDriverWebPublic.tabFinder = null;
		WebDriverWebPublic.webTable = null;		
	}

	/**
	 * get row count of a webtable</BR>
	 * 返回一个WebTable的行的总数。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @return the row count of the webTable
	 * @throws RuntimeException
	 */
	protected int tableRowCount(By tabBy) {
		int rowCount = 0;
		try {
			webTable = tableCache(tabBy);
			rowCount = webTable.rowCount();
			pass("the webTable " + tabBy.toString() + "has row count: [ " + rowCount + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return rowCount;
	}

	/**
	 * get column count of a specified webtable row</BR>
	 * 返回一个WebTable在制定行上的列的总数。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @param rowNum row index of your webTable to count
	 * @return the column count of the row in webTable
	 * @throws RuntimeException
	 */
	protected int tableColCount(By tabBy, int rowNum) {
		int colCount = 0;
		try {
			webTable = tableCache(tabBy);
			colCount = webTable.colCount(rowNum);
			pass("count columns of the webTable " + tabBy.toString() 
				+ " on the row [ " + rowNum + " ], got: [ " + colCount + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return colCount;
	}

	/**
	 * get the element in the webTable cell by row and col index</BR>
	 * 返回WebTable中指定行、列和类型的子元素，如按钮、链接、输入框等。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @param row row index of the webTable.
	 * @param col column index of the webTable.
	 * @param type the element type, such as "img"/"a"/"input"
	 * @param index element index in the specified cell, begins with 1.
	 * @return the webTable cell WebElement
	 * @throws RuntimeException
	 */
	protected WebElement tableChildElement(By tabBy, int row, int col, String type, int index) {
		try {
			webTable = tableCache(tabBy);
			return webTable.childItem(row, col, type, index);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * get the cell text of the webTable on specified row and column</BR>
	 * 返回WebTable的指定行和列的中的文本内容。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @param row row index of the webTable.
	 * @param col column index of the webTable.
	 * @return the cell text
	 * @throws RuntimeException
	 */
	protected String tableCellText(By tabBy, int row, int col) {
		String text = null;
		try {
			webTable = tableCache(tabBy);
			text = webTable.cellText(row, col);
			pass("the text of cell[" + row + "," + col + "] is: [ " + text + " ]");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return text;
	}

	/**
	 * wait for the element visiable in timeout setting</BR>
	 * 在指定时间内等待，直到对象可见。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForElementVisible(By by, int seconds) {
		WebDriverWait wait = new WebDriverWait(driver, seconds);
		try {
			return wait.until(ExpectedConditions.visibilityOfElementLocated(by)) != null;
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for the element visiable in timeout setting</BR>
	 * 在指定时间内等待，直到对象可见。
	 * 
	 * @param element the element to be found.
	 * @param seconds timeout in seconds.
	 * @throws RuntimeException.
	 */
	protected boolean waitForElementVisible(WebElement element, int seconds) {
		WebDriverWait wait = new WebDriverWait(driver, seconds);
		try {
			return wait.until(ExpectedConditions.visibilityOf(element)) != null;
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for the element clickable in timeout setting</BR>
	 * 在指定时间内等待，直到对象能够被点击。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForElementClickable(By by, int seconds) {
		WebDriverWait wait = new WebDriverWait(driver, seconds);
		try {
			return wait.until(ExpectedConditions.elementToBeClickable(by)) != null;
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for text appears on element in timeout setting</BR>
	 * 在指定时间内等待，直到指定对象上出现指定文本。
	 * 
	 * @param by the element locator By
	 * @param text the text to be found of element
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForTextOnElement(By by, String text, int seconds) {
		WebDriverWait wait = new WebDriverWait(driver, seconds);
		try {
			return wait.until(ExpectedConditions.textToBePresentInElement(by, text)) != null;
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for text appears in element attributes in timeout setting</BR>
	 * 在指定时间内等待，直到指定对象的某个属性值等于指定文本。
	 * 
	 * @param by the element locator By
	 * @param text the text to be found in element attributes
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForTextOfElementAttr(By by, String text, int seconds) {
		WebDriverWait wait = new WebDriverWait((WebDriver) driver, seconds);
		try {
			return wait.until(ExpectedConditions.textToBePresentInElementValue(by, text)) != null;
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * wait for alert disappears in the time united by seconds</BR>
	 * 在指定时间内等待，对话框（Dialog）消失，用以缓冲运行，增加健壮性。
	 * 
	 * @throws RuntimeException
	 */
	protected boolean waitForAlertDisappear(int seconds) {
		long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < seconds * 1000) {
			try {
				driver.switchTo().alert();
			} catch (NoAlertPresentException ne) {
				return true;
			} catch (Exception e) {
				failValidation();
				LOG.error(e);
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	/**
	 * make the alert dialog not to appears</BR>
	 * 通过JS函数重载，在对话框（Alert）出现之前点击掉它，或者说等价于不让其出现。
	 */
	protected void ensrueBeforeAlert() {
		jsExecutor(JSCollection.ENSRUEBEFOREALERT.getName(),
				"override js to ensure alert before it appears");
	}

	/**
	 * make the warn dialog not to appears when window.close()</BR>
	 * 通过JS函数重载，在浏览器窗口关闭之前除去它的告警提示。
	 */
	protected void ensureBeforeWinClose() {
		jsExecutor(JSCollection.ENSUREBEFOREWINCLOSE.getName(),
				"override js to ensure window close event");
	}

	/**
	 * make the confirm dialog not to appears choose default option OK</BR>
	 * 通过JS函数重载，在确认框（Confirm）出现之前点击确认，或者说等价于不让其出现而直接确认。
	 */
	protected void ensureBeforeConfirm() {
		jsExecutor(JSCollection.ENSUREBEFORECONFIRM.getName(),
				"override js to ensure confirm before it appears");
	}

	/**
	 * make the confirm dialog not to appears choose default option Cancel</BR>
	 * 通过JS函数重载，在确认框（Confirm）出现之前点击取消，或者说等价于不让其出现而直接取消。
	 */
	protected void dismissBeforeConfirm() {
		jsExecutor(JSCollection.DISMISSBEFORECONFIRM.getName(),
				"override js to dismiss confirm before it appears");
	}

	/**
	 * make the prompt dialog not to appears choose default option OK</BR>
	 * 通过JS函数重载，在提示框（Prompt）出现之前点击确认，或者说等价于不让其出现而直接确认。
	 */
	protected void ensureBeforePrompt() {
		jsExecutor(JSCollection.ENSUREBEFOREPROMPT.getName(),
				"override js to ensure prompt before it appears");
	}

	/**
	 * make the prompt dialog not to appears choose default option Cancel</BR>
	 * 通过JS函数重载，在提示框（Prompt）出现之前点击取消，或者说等价于不让其出现而直接取消。
	 */
	protected void dismisBeforePrompt() {
		jsExecutor(JSCollection.DISMISBEFOREPROMPT.getName(),
				"override js to dismiss prompt before it appears");
	}

	/**
	 * choose OK/Cancel button's OK on alerts</BR>
	 * 在弹出的对话框（Dialog）上点击确认/是等接受性按钮。
	 * 
	 * @throws RuntimeException
	 */
	protected void chooseOKOnAlert() {
		try {
			driver.switchTo().alert().accept();
			pass("click OK button on alert");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * choose Cancel on alerts</BR>
	 * 在弹出的对话框（Dialog）上点击取消/否等拒绝性按钮。
	 * 
	 * @throws RuntimeException
	 */
	protected void chooseCancelOnAlert() {
		try {
			driver.switchTo().alert().dismiss();
			pass("click Cancel on alert dialog");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * get the text of the alerts</BR>
	 * 返回对话框（Dialog）上的提示信息文本内容。
	 * 
	 * @return alert text string
	 * @throws RuntimeException
	 */
	protected String getTextOfAlert() {
		String alerts = null;
		try {
			alerts = driver.switchTo().alert().getText();
			pass("the text of the alert is: " + alerts);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return alerts;
	}

	/**
	 * set text on alerts</BR>
	 * 向对话框（InputBox）中输入文本。
	 * 
	 * @param text the text string you want to input on alerts
	 * @throws RuntimeException
	 */
	protected void setTextOnAlert(String text) {
		try {
			driver.switchTo().alert().sendKeys(text);
			pass("set text [ " + text + " ] on alert");
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * use js to make the element to be un-hidden</BR>
	 * 使用JS执行的方法强制让某些隐藏的控件显示出来。
	 * 
	 * @param element the element to be operate
	 */
	protected void makeElementUnHidden(WebElement element) {
		jsExecutor(JSCollection.MAKEELEMENTUNHIDDEN.getName(), 
				"override js to make elements to be visible", element);
	}

	/**
	 * use js to make the element to be un-hidden</BR>
	 * 使用JS执行的方法强制让某些隐藏的控件显示出来。
	 * 
	 * @param by the By locator to find the element
	 */
	protected void makeElementUnHidden(By by) {
		jsExecutor(JSCollection.MAKEELEMENTUNHIDDEN.getName(), 
				"override js to make elements to be visible", driver.findElement(by));
	}

	/**
	 * override the getTitle method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the title on your current session
	 * @throws RuntimeException
	 */
	protected String getWindowTitle() {
		String title = null;
		try {
			title = driver.getTitle();
			pass("current window title is :" + title);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return title;
	}

	/**
	 * override the getCurrentUrl method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the url on your current session
	 * @throws RuntimeException
	 */
	protected String getCurrentUrl() {
		String url = null;
		try {
			url = driver.getCurrentUrl();
			pass("current session url is :" + url);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return url;
	}

	/**
	 * override the getWindowHandles method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the window handlers set
	 * @throws RuntimeException
	 */
	protected Set<String> getWindowHandles() {
		Set<String> handler = null;
		try {
			handler = driver.getWindowHandles();
			handler = driver.getWindowHandles();
			pass("window handlers are: " + handler.toString());
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return handler;
	}

	/**
	 * override the getWindowHandle method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the window handler string
	 * @throws RuntimeException
	 */
	protected String getWindowHandle() {
		String handler = null;
		try {
			handler = driver.getWindowHandle();
			pass("current window handler is:" + handler);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return handler;
	}

	/**
	 * override the getPageSource method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the page source string
	 * @throws RuntimeException
	 */
	protected String getPageSource() {
		String source = null;
		try {
			source = driver.getPageSource();
			pass("page source begins with: " + source.substring(0, 50));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return source;
	}

	/**
	 * override the getSessionId method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return current session id string
	 * @throws RuntimeException
	 */
	protected String getSessionId() {
		String sessionId = null;
		try {
			sessionId = driver.getSessionId().toString();
			pass("current sessionid is:" + sessionId);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return sessionId;
	}

	/**
	 * override the getTagName method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the tagname string
	 * @throws RuntimeException
	 */
	protected String getTagName(WebElement element) {
		String tagName = null;
		try {
			tagName = element.getTagName();
			pass("element's TagName is: " + tagName);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return tagName;
	}

	/**
	 * override the getTagName method, find the element by By and get its tag name</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the tagname string
	 * @throws RuntimeException
	 */
	protected String getTagName(By by) {
		String tagName = null;
		try {
			tagName = driver.findElement(by).getTagName();
			pass("element [ " + by.toString() + " ]'s TagName is: " + tagName);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return tagName;
	}

	/**
	 * override the getAttribute method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 * @throws RuntimeException
	 */
	protected String getAttribute(WebElement element, String attributeName) {
		String value = null;
		try {
			value = element.getAttribute(attributeName);
			pass("element's " + attributeName + "is: " + value);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return value;
	}

	/**
	 * override the getAttribute method, find the element by By and get its attribute value</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 * @throws RuntimeException
	 */
	protected String getAttribute(By by, String attributeName) {
		String value = null;
		try {
			value = driver.findElement(by).getAttribute(attributeName);
			pass("element [ " + by.toString() + " ]'s " + attributeName + "is: " + value);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return value;
	}

	/**
	 * override the isSelected method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement selected
	 * @throws RuntimeException
	 */
	protected boolean isSelected(WebElement element) {
		boolean isSelected = false;
		try {
			isSelected = element.isSelected();
			pass("element selected? " + String.valueOf(isSelected));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isSelected;
	}

	/**
	 * override the isSelected method, the element to be find by By</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement selected
	 * @throws RuntimeException
	 */
	protected boolean isSelected(By by) {
		boolean isSelected = false;
		try {
			isSelected = driver.findElement(by).isSelected();
			pass("element [ " + by.toString() + " ] selected? "	+ String.valueOf(isSelected));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isSelected;
	}

	/**
	 * override the isEnabled method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement enabled
	 * @throws RuntimeException
	 */
	protected boolean isEnabled(WebElement element) {
		boolean isEnabled = false;
		try {
			isEnabled = element.isEnabled();
			pass("element enabled? " + String.valueOf(isEnabled));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isEnabled;
	}

	/**
	 * override the isEnabled method, the element to be find by By</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement enabled
	 * @throws RuntimeException
	 */
	protected boolean isEnabled(By by) {
		boolean isEnabled = false;
		try {
			isEnabled = driver.findElement(by).isEnabled();
			pass("element [ " + by.toString() + " ] enabled? " + String.valueOf(isEnabled));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isEnabled;
	}

	/**
	 * override the getText method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected String getText(WebElement element) {
		String text = null;
		try {
			text = element.getText();
			pass("element text is:" + text);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return text;
	}

	/**
	 * override the getText method, find the element by By and get its own text</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the text string
	 * @throws RuntimeException
	 */
	protected String getText(By by) {
		String text = null;
		try {
			text = driver.findElement(by).getText();
			pass("element [ " + by.toString() + " ]'s text is: " + text);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return text;
	}

	/**
	 * override the isDisplayed method, adding user defined log</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement displayed
	 * @throws RuntimeException
	 */
	protected boolean isDisplayed(WebElement element) {
		boolean isDisplayed = false;
		try {
			isDisplayed = element.isDisplayed();
			pass("element displayed? " + String.valueOf(isDisplayed));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isDisplayed;
	}

	/**
	 * override the isDisplayed method, the element to be find by By</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement displayed
	 * @throws RuntimeException
	 */
	protected boolean isDisplayed(By by) {
		boolean isDisplayed = false;
		try {
			isDisplayed = driver.findElement(by).isDisplayed();
			pass("element [ " + by.toString() + " ] displayed? " + String.valueOf(isDisplayed));
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return isDisplayed;
	}

	/**
	 * get its css property value</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 * @throws RuntimeException
	 */
	protected String getCssValue(WebElement element, String propertyName) {
		String cssValue = null;
		try {
			cssValue = element.getCssValue(propertyName);
			pass("element's css [" + propertyName + "] value is:" + cssValue);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return cssValue;
	}

	/**
	 * find the element by By and get its css property value</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 * @throws RuntimeException
	 */
	protected String getCssValue(By by, String propertyName) {
		String cssValue = null;
		try {
			cssValue = driver.findElement(by).getCssValue(propertyName);
			pass("element [ " + by.toString() + " ]'s css[" 
				+ propertyName + "] value is: " + cssValue);
		} catch (Exception e) {
			failValidation();
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return cssValue;
	}
}