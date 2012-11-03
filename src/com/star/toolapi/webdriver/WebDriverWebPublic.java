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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.apache.commons.io.FileUtils;
import com.star.logging.frame.LoggingManager;
import com.star.support.externs.Win32GuiByAu3;
import com.star.toolapi.webdriver.group.WebDriverWebTable;
import static org.testng.AssertJUnit.assertTrue;

public class WebDriverWebPublic extends WebDriverController {

	private static final LoggingManager LOG = new LoggingManager(WebDriverWebPublic.class.getName());
	protected static final String FORMATTER = "_yyyyMMddHHmmssSSS";
	protected static final Win32GuiByAu3 AU3 = new Win32GuiByAu3(); 
	private static long maxWaitfor = 10000;
	private static long sleepUnit = 500;
	private static By tabFinder = null;
	private static WebDriverWebTable webTable = null;

	/**
	 * set sleep interval for loop wait.
	 * 
	 * @param 	interval milliseconds for each sleep
	 */
	protected void setSleepInterval(long interval) {
		WebDriverWebPublic.sleepUnit = interval;
	}

	/**
	 * config timeout setting for each step, default is 10 seconds.</BR>
	 * 配置单个步骤运行的最大超时时间，默认是10秒钟。
	 * 
	 * @param 	timeout max wait time setting in seconds
	 */
	protected void setMaxWaitTime(long timeout) {
		WebDriverWebPublic.maxWaitfor = timeout;
	}

	/**
	 * wait util the element visible in max wait time setting.</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations.</BR>
	 * 在指定时间内循环等待，直到对象可见，超时之后直接抛出对象不可见异常信息。
	 * 
	 * @param element the WebElement to be judged
	 * @param timeout timeout setting in millisecond unit
	 * @throws ElementNotVisibleException
	 */
	private void waitUtilElementVisible(WebElement element, long timeout) {
		long start = System.currentTimeMillis();
		boolean isDisplayed = false;
		while (!isDisplayed && ((System.currentTimeMillis() - start) < timeout)) {
			isDisplayed = (element == null)? false : element.isDisplayed();
			pause(sleepUnit);
		}
		if (!isDisplayed){
			throw new ElementNotVisibleException(
				"the element is not visible in " + timeout + "milliseconds!");
		}
	}

	/**
	 * wait util the element visible in max wait time setting.</BR>
	 * if not visible at last, throw ElementNotVisibleException to the operations.</BR>
	 * 在指定时间内循环等待，直到对象可见，使用用户指定的默认超时设置。
	 * 
	 * @param element the WebElement to be judged
	 * @throws ElementNotVisibleException
	 */
	private void waitUtilElementVisible(WebElement element) {
		waitUtilElementVisible(element, maxWaitfor);
	}

	/**
	 * public method for handle assertions and screenshot.</BR>
	 * 框架共用的各个操作结果检查和断言，出错则抛出异常并且截图。
	 * 
	 * @param isSucceed if your operation success
	 * @throws RuntimeException
	 */
	private void operationCheck(boolean isSucceed) {
		String method = Thread.currentThread().getStackTrace()[2].getMethodName();
		String file = LOG_REL + this.getClass().getName() + STRUTIL.formatedTime(FORMATTER) + ".png";
		try {
			assertTrue(isSucceed);
		} catch (AssertionError ae) {
			takeScreenShot(file);
			fail("method [" + method + "] failed, screenshot is: [" + file + "]");
			throw new RuntimeException("Test Run Failed:" + ae.getMessage());
		}
	}

	/**
	 * take a screen shot and save the file by path and name.</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param fileName the file path&name of the screenshot to be saved
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
	 * override the screenShot method, using default path and name.</BR>
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
	 * judge if the alert is existing.</BR>
	 * 判断弹出的对话框（Dialog）是否存在。
	 * 
	 * @throws RuntimeException
	 */
	protected boolean alertExists() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException ne) {
			warn("no alert is present now");
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e.getMessage());
		}
		return false;
	}

	/**
	 * judge if the alert is present in specified seconds.</BR>
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
				throw new RuntimeException(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * judge if the element is existing.</BR>
	 * 判断指定的对象是否存在。
	 * 
	 * @param by the element locator By
	 */
	protected boolean elementExists(By by) {
		return (driver.findElements(by).size() > 0) ? true : false;
	}

	/**
	 * judge if the element is present in specified seconds.</BR>
	 * 在指定的时间内判断指定的对象是否存在。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 */
	protected boolean elementExists(final By by, int seconds) {
		long start = System.currentTimeMillis();
		boolean exists = false;
		while (!exists && ((System.currentTimeMillis() - start) < seconds * 1000)) {
			exists = elementExists(by);
		}
		return exists;
	}

	/**
	 * judge if the browser is existing, using part of the page title.</BR>
	 * 按照网页标题判断页面是否存在，标题可使用部分内容匹配。
	 * 
	 * @param browserTitle part of the title to see if browser exists
	 * @throws RuntimeException
	 */
	protected boolean browserExists(String browserTitle) {
		String defaultHandler = driver.getWindowHandle();
		try {
			Set<String> windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String currentTitle = driver.getTitle();
				if (currentTitle.contains(browserTitle)){
					return true;
				}
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			driver.switchTo().window(defaultHandler);
		}
		return false;
	}

	/**
	 * judge if the browser is present by title reg pattern in specified seconds.</BR>
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
	 * maximize browser window: support ie, ff3.6 and lower.</BR>
	 * 网页窗口最大化操作。
	 * 
	 * @throws RuntimeException
	 */
	protected void maximizeWindow() {
		boolean isSucceed = false;
		String js = "if(document.all) { " + "self.moveTo(0, 0); "
				+ "self.resizeTo(screen.availWidth, screen.availHeight); self.focus();" + "}";
		try {
			driver.executeScript(js);
			isSucceed = true;
			pass("window maximized");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select default window and default frame.</BR>
	 * 在当前页面中自动选择默认的页面框架（frame）。
	 * 
	 * @throws RuntimeException
	 */
	protected void selectDefaultWindowFrame() {
		boolean isSucceed = false;
		try {
			driver.switchTo().defaultContent();
			isSucceed = true;
			pass("switch to default frame on window");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * switch to active element.</BR>
	 * 在当前操作的页面和对象时自动选择已被激活的对象。
	 * 
	 * @throws RuntimeException
	 */
	protected void focusOnActiveElement() {
		boolean isSucceed = false;
		try {
			driver.switchTo().activeElement();
			isSucceed = true;
			pass("switch to active element");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * switch to new window supporting, by deleting first hanlder.</BR>
	 * 选择最新弹出的窗口，需要预存第一个窗口的handler。
	 * 
	 * @param firstHandler the first window handle
	 * @throws RuntimeException
	 */
	protected void selectNewWindow(String firstHandler) {
		boolean isSucceed = false;
		Set<String> handlers = null;
		Iterator<String> it = null;
		try {
			handlers = driver.getWindowHandles();
			handlers.remove(firstHandler);
			it = handlers.iterator();
			if (it.hasNext()) {
				driver.switchTo().window(it.next());
				isSucceed = true;
				pass("switch to new window");
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * switch to window by title.</BR>
	 * 按照网页标题选择窗口，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be switched to
	 * @throws RuntimeException
	 */
	protected void selectWindow(String windowTitle) {
		boolean isSucceed = false;
		Set<String> windowHandles = null;
		try {
			windowHandles = driver.getWindowHandles();
			for (String handler : windowHandles) {
				driver.switchTo().window(handler);
				String title = driver.getTitle();
				if (windowTitle.equals(title)) {
					isSucceed = true;
					pass("switch to window [ " + windowTitle + " ]");
					break;
				}
			}
			if (!isSucceed){
				fail("there is no window named [ " + windowTitle + " ]");
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * close window by window title and its index if has the same title, by string full pattern.</BR>
	 * 按照网页标题选择并且关闭窗口，重名窗口按照指定的重名的序号关闭，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be closed.
	 * @param index the index of the window which shared the same title, begins with 1.
	 * @throws RuntimeException
	 */
	@SuppressWarnings("null")
	protected void closeWindow(String windowTitle, int index) {
		boolean isSucceed = true;
		Object[] winArray = null;
		List<String> winList = null;
		try {
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
		} catch (WebDriverException e) {
			isSucceed = false;
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * close the last window by the same window title, by string full pattern.</BR>
	 * 按照网页标题选择窗口，适用于无重名的窗口，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window to be closed.
	 * @throws RuntimeException
	 */
	protected void closeWindow(String windowTitle) {
		boolean isSucceed = true;
		Object[] winArray = null;
		try {
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
		} catch (WebDriverException e) {
			isSucceed = false;
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * close windows except specified window title, by string full pattern.</BR>
	 * 关闭除了指定标题页面之外的所有窗口，适用于例外窗口无重名的情况，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window not to be closed
	 * @throws RuntimeException
	 */
	protected void closeWindowExcept(String windowTitle) {
		boolean isSucceed = true;
		Set<String> windowHandles = null;
		try {
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
		} catch (WebDriverException e) {
			isSucceed = false;
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * close windows except specified window title, by string full pattern.</BR>
	 * 关闭除了指定标题页面之外的所有窗口，例外窗口如果重名，按照指定的重名顺序关闭，标题内容需要全部匹配。
	 * 
	 * @param windowTitle the title of the window not to be closed
	 * @param index the index of the window to keep shared the same title with others, begins with 1.
	 * @throws RuntimeException
	 */
	protected void closeWindowExcept(String windowTitle, int index) {
		boolean isSucceed = true;
		Set<String> windowHandles = null;
		Object[] winArray = null;
		try {
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
			for (int i = 0; i < winArray.length; i++) {
				driver.switchTo().window(winArray[i].toString());
				if (i + 1 != index) {
					driver.switchTo().defaultContent();
					driver.close();
				}
			}
			pass("keep only window [ " + windowTitle + " ] by title index [ " + index + " ]");
		} catch (WebDriverException e) {
			isSucceed = false;
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for new window which has no title in few seconds.</BR>
	 * 判断在指定的时间内是否有新的窗口弹出，无论其是否有标题。
	 * 
	 * @param browserCount windows count before new window appears.
	 * @param seconds time unit in seconds.
	 */
	protected boolean isNewWindowExits(int browserCount, int seconds) {
		boolean isExist = false;
		long begins = System.currentTimeMillis();
		while ((System.currentTimeMillis() - begins < seconds * 1000) && !isExist) {
			isExist = (driver.getWindowHandles().size() > browserCount) ? true : false;
		}
		return isExist;
	}

	/**
	 * wait for new window which has no title in few seconds.</BR>
	 * 判断在指定的时间内是否有新的窗口弹出，无论其是否有标题。
	 * 
	 * @param oldHandlers windows handler Set before new window appears.
	 * @param seconds time unit in seconds.
	 */
	protected boolean isNewWindowExits(Set<String> oldHandlers, int seconds) {
		boolean isExist = false;
		long begins = System.currentTimeMillis();
		while ((System.currentTimeMillis() - begins < seconds * 1000) && !isExist) {
			isExist = (driver.getWindowHandles().size() > oldHandlers.size()) ? true : false;
		}
		return isExist;
	}

	/**
	 * select a frame by index.</BR>
	 * 按照序号选择框架（frame）。
	 * 
	 * @param index the index of the frame to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(int index) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(index);
			isSucceed = true;
			pass("select frame by index [ " + index + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select a frame by name or id.</BR>
	 * 按照名称或者ID选择框架（frame）。
	 * 
	 * @param nameOrId the name or id of the frame to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(String nameOrId) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(nameOrId);
			isSucceed = true;
			pass("select frame by name or id [ " + nameOrId + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select a frame by frameElement.</BR>
	 * 按照框架对象本身选择框架（frame）。
	 * 
	 * @param frameElement the frame element to select
	 * @throws RuntimeException
	 */
	protected void selectFrame(WebElement frameElement) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(frameElement);
			isSucceed = true;
			pass("select frame by frameElement");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select a frame by frame element locator: By.</BR>
	 * 按照指定的元素定位方式选择框架（frame）。
	 * 
	 * @param by the frame element locator
	 * @throws RuntimeException
	 */
	protected void selectFrame(By by) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(driver.findElement(by));
			isSucceed = true;
			pass("select frame by frame locator [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * edit a content editable iframe.</BR>
	 * 编辑指定框架（frame）内的最直接展示文本内容。
	 * 
	 * @param by the frame element locaotr
	 * @param text the text string to be input
	 * @throws RuntimeException
	 */
	protected void editFrameText(By by, String text) {
		boolean isSucceed = false;
		try {
			driver.switchTo().frame(driver.findElement(by));
			driver.switchTo().activeElement().sendKeys(text);
			isSucceed = true;
			pass("input text [ " + text + " ] to frame [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the get method, adding user defined log.</BR>
	 * 地址跳转方法，与WebDriver原生get方法内容完全一致。
	 * 
	 * @param url the url you want to open
	 * @throws RuntimeException
	 */
	protected void get(String url) {
		boolean isSucceed = false;
		try {
			driver.get(url);
			isSucceed = true;
			pass("navigate to url [ " + url + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * navigate to some where by url.</BR>
	 * 地址跳转方法，与WebDriver原生navigate.to方法内容完全一致。
	 * 
	 * @param url the url you want to open
	 * @throws RuntimeException
	 */
	protected void navigateTo(String url){
		boolean isSucceed = false;
		try {
			driver.navigate().to(url);
			isSucceed = true;
			pass("navigate to url [ " + url + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);		
	}

	/**
	 * navigate back.</BR>
	 * 地址跳转方法，与WebDriver原生navigate.back方法内容完全一致。
	 * 
	 * @throws RuntimeException
	 */
	protected void navigateBack(){
		boolean isSucceed = false;
		try {
			driver.navigate().back();
			isSucceed = true;
			pass("navigate back");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);		
	}

	/**
	 * navigate forward.</BR>
	 * 地址跳转方法，与WebDriver原生navigate.forward方法内容完全一致。
	 * 
	 * @throws RuntimeException
	 */
	protected void navigateForward(){
		boolean isSucceed = false;
		try {
			driver.navigate().forward();
			isSucceed = true;
			pass("navigate forward");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);		
	}

	/**
	 * override the click method, adding user defined log.</BR>
	 * 在等到对象可见之后点击指定的对象。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void click(WebElement element) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			element.click();
			isSucceed = true;
			pass("click on WebElement");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the click method, click on the element to be find by By.</BR>
	 * 在等到对象可见之后点击指定的对象。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void click(By by) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(driver.findElement(by));
			driver.findElement(by).click();
			isSucceed = true;
			pass("click on element [ " + by.toString() + " ] ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * forcely click, by executing javascript.</BR>
	 * 在等到对象可见之后点击指定的对象，使用JavaScript执行的方式去操作，</BR>
	 * 这种方法使用过后一般需要调用一次selectDefaultWindowFrame以确保运行稳定。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void clickByJavaScript(WebElement element) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			driver.executeScript("return arguments[0].click();", element);
			isSucceed = true;
			pass("click on element");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * forcely click, by executing javascript.</BR>
	 * 在等到对象可见之后点击指定的对象，使用JavaScript执行的方式去操作，</BR>
	 * 这种方法使用过后一般需要调用一次selectDefaultWindowFrame以确保运行稳定。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void clickByJavaScript(By by) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(driver.findElement(by));
			driver.executeScript("return arguments[0].click();", driver.findElement(by));
			isSucceed = true;
			pass("click on element [ " + by.toString() + " ] ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * doubleclick on the element to be find by By.</BR>
	 * 在等到对象可见之后双击指定的对象.
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void doubleClick(WebElement element) {
		boolean isSucceed = false;		
		try {
			waitUtilElementVisible(element);
			actionDriver.doubleClick(element);
			actionDriver.perform();
			isSucceed = true;
			pass("doubleClick on element ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * doubleclick on the element.</BR>
	 * 在等到对象可见之后双击指定的对象.
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void doubleClick(By by) {
		boolean isSucceed = false;		
		try {
			waitUtilElementVisible(driver.findElement(by));
			actionDriver.doubleClick(findElement(by));
			actionDriver.perform();
			isSucceed = true;
			pass("doubleClick on element [ " + by.toString() + " ] ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * right click on the element to be find by By.</BR>
	 * 在等到对象可见之后鼠标右键点击指定的对象.
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void rightClick(WebElement element) {
		boolean isSucceed = false;		
		try {
			waitUtilElementVisible(element);
			actionDriver.contextClick(element);
			actionDriver.perform();
			isSucceed = true;
			pass("rightClick on element ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * right click on the element.</BR>
	 * 在等到对象可见之后鼠标右键点击指定的对象。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void rightClick(By by) {
		boolean isSucceed = false;		
		try {
			waitUtilElementVisible(driver.findElement(by));
			actionDriver.contextClick(findElement(by));
			actionDriver.perform();
			isSucceed = true;
			pass("rightClick on element [ " + by.toString() + " ] ");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the submit method, adding user defined log.</BR>
	 * 在等到指定对象可见之后在该对象上做确认/提交的操作。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void submit(WebElement element) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			element.submit();
			isSucceed = true;
			pass("submit on element");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the submit method, submit on the element to be find by By.</BR>
	 * 在等到指定对象可见之后在该对象上做确认/提交的操作。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void submit(By by) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(driver.findElement(by));
			driver.findElement(by).submit();
			isSucceed = true;
			pass("submit on element [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the clear method, adding user defined log.</BR>
	 * 在等到指定对象可见之后在该对象上做清理操作，一般用于输入框和选择框。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected void clear(WebElement element) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			element.clear();
			isSucceed = true;
			pass("element cleared");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the clear method, clear on the element to be find by By.</BR>
	 * 在等到指定对象可见之后在该对象上做清理操作，一般用于输入框和选择框。
	 * 
	 * @param by the locator you want to find the element
	 * @throws RuntimeException
	 */
	protected void clear(By by) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.clear();
			isSucceed = true;
			pass("element [ " + by.toString() + " ] cleared");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the sendKeys method, adding user defined log.</BR>
	 * 以追加文本的模式在指定可编辑对象中输入文本，操作之前自动等待到对象可见。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysAppend(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			element.sendKeys(text);
			isSucceed = true;
			pass("send text [ " + text + " ] to element");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the sendKeys method, sendKeys on the element to be find by By.</BR>
	 * 以追加文本的模式在指定可编辑对象中输入文本，操作之前自动等待到对象可见。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysAppend(By by, String text) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.sendKeys(text);
			isSucceed = true;
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the sendKeys method, adding user defined log.</BR>
	 * 清理指定对象中已经输入的内容重新输入，操作之前自动等待到对象可见。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeys(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			element.clear();
			element.sendKeys(text);
			isSucceed = true;
			pass("send text [ " + text + " ] to WebEdit");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the sendKeys method, sendKeys on the element to be find by By.</BR>
	 * 清理指定对象中已经输入的内容重新输入，操作之前自动等待到对象可见。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeys(By by, String text) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			element.clear();
			element.sendKeys(text);
			isSucceed = true;
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * readonly text box or richtext box input.</BR>
	 * 使用DOM（Documnet Object Modal）修改页面中对象的文本属性值，使用ID定位对象则返回唯一对象，其余返回数组。
	 * 
	 * @param by the attribute of the element, default support is TagName/Name/Id
	 * @param byValue the attribute value of the element
	 * @param text the text you want to input to element
	 * @param index the index of the elements shared the same attribute value
	 * @throws RuntimeException
	 * @throws IllegalArgumentException
	 */
	protected void sendKeysByDOM(String by, String byValue, String text, int index) {
		String js = null;
		boolean isSucceed = false;
		
		if (by.equalsIgnoreCase("tagname")) {
			js = "document.getElementsByTagName('" + byValue + "')[" + index + "].value='" + text + "'";
		} else if (by.equalsIgnoreCase("name")) {
			js = "document.getElementsByName('" + byValue + "')[" + index + "].value='" + text + "'";
		} else if (by.equalsIgnoreCase("id")) {
			js = "document.getElementById('" + byValue + "').value='" + text + "'";
		} else {
			throw new IllegalArgumentException("only can find element by TagName/Name/Id");
		}

		try {
			driver.executeScript(js);
			isSucceed = true;
			pass("input text [ " + text + " ] to element [ " + by + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * readonly text box or richtext box input, finding elements by element id.</BR>
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
	 * readonly text box or richtext box input, finding elements by element name.</BR>
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
	 * readonly text box or richtext box input, finding elements by element tag name.</BR>
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
	 * sendKeys by using keybord event on element.</BR>
	 * 使用键盘模拟的方法在指定的对象上输入指定的文本。
	 * 
	 * @param element the webelement you want to operate
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysByKeybord(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			actionDriver.sendKeys(element, text);
			actionDriver.perform();
			isSucceed = true;
			pass("send text [ " + text + " ] to WebEdit");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * sendKeys by using keybord event on element to be found by By.</BR>
	 * 使用键盘模拟的方法在指定的对象上输入指定的文本。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysByKeybord(By by, String text) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			actionDriver.sendKeys(element, text);
			actionDriver.perform();
			isSucceed = true;
			pass("input text [ " + text + " ] to element [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * edit rich text box created by kindeditor.</BR>
	 * 使用JS调用KindEditor对象本身的接口，在页面KindEditor对象中输入指定的文本。
	 * 
	 * @param editorId kindeditor id
	 * @param text the text you want to input to element
	 * @throws RuntimeException
	 */
	protected void sendKeysOnKindEditor(String editorId, String text) {
		boolean isSucceed = false;
		String javascript = "KE.html('" + editorId + "','<p>" + text + "</p>');";
		try {
			driver.executeScript(javascript);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by index.</BR>
	 * 按照指定序号选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param index the index of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByIndex(WebElement element, int index) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByIndex(index);
			isSucceed = true;
			pass("item selected by index [ " + index + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by index.</BR>
	 * 按照指定序号选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param index the index of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByIndex(By by, int index) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByIndex(index);
			isSucceed = true;
			pass("item selected by index [ " + index + " ] on [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by item value.</BR>
	 * 按照指定选项的实际值（不是可见文本值，而是对象的“value”属性的值）选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param itemValue the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByValue(WebElement element, String itemValue) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByValue(itemValue);
			isSucceed = true;
			pass("item selected by item value [ " + itemValue + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by item value.</BR>
	 * 按照指定选项的实际值（不是可见文本值，而是对象的“value”属性的值）选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param itemValue the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByValue(By by, String itemValue) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByValue(itemValue);
			isSucceed = true;
			pass("item selected by item value [ " + itemValue + " ] on [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by item value.</BR>
	 * 按照指定选项的可见文本值（用户直接可以看到的文本）选择下拉列表中的选项。
	 * 
	 * @param element the picklist element
	 * @param text the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByVisibleText(WebElement element, String text) {
		boolean isSucceed = false;
		try {
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByVisibleText(text);
			isSucceed = true;
			pass("item selected by visible text [ " + text + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * select an item from a picklist by item value.</BR>
	 * 按照指定选项的可见文本值（用户直接可以看到的文本）选择下拉列表中的选项。
	 * 
	 * @param by the locator you want to find the element
	 * @param text the item value of the item to be selected
	 * @throws RuntimeException
	 */
	protected void selectByVisibleText(By by, String text) {
		boolean isSucceed = false;
		try {
			WebElement element = driver.findElement(by);
			waitUtilElementVisible(element);
			Select select = new Select(element);
			select.selectByVisibleText(text);
			isSucceed = true;
			pass("item selected by visible text [ " + text + " ] on [ " + by.toString() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
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
		boolean isSucceed = false;
		try {
			WebElement checkElement = element.findElement(By.tagName("input"));
			waitUtilElementVisible(checkElement);
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				element.click();
			}
			isSucceed = true;
			pass("the checkbox is set to [ " + onOrOff.toUpperCase() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
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
		boolean isSucceed = false;
		try {
			WebElement checkBox = driver.findElement(by);
			waitUtilElementVisible(checkBox);
			WebElement checkElement = checkBox.findElement(By.tagName("input"));
			if ((onOrOff.toLowerCase().contains("on") && !checkElement.isSelected())
					|| (onOrOff.toLowerCase().contains("off") && checkElement.isSelected())) {
				checkBox.click();
			}
			isSucceed = true;
			pass("the checkbox [ " + by.toString() + " ] is set to [ " 
					+ onOrOff.toUpperCase() + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * find elements displayed on the page.</BR>
	 * 按照指定的定位方式寻找所有可见的对象。
	 * 
	 * @param by the way to locate webelements
	 * @return displayed webelement list
	 * @throws RuntimeException
	 */
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
			int eleNum = elementList.size();
			if (eleNum > 0){
				pass("got" + eleNum + "displayed elements [ " + by.toString() + " ]");
			}else{
				warn("there is not displayed element found by [" + by.toString() + " ]");
			}
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return elementList;
	}

	/**
	 * find elements displayed on the page.</BR>
	 * 按照指定的定位方式寻找第一可见的对象。
	 * 
	 * @param by the way to locate webelement
	 * @return the first displayed webelement
	 * @throws RuntimeException
	 */
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
		operationCheck(isSucceed);
		return retElement;
	}

	/**
	 * override the findElements method, adding user defined log.</BR>
	 * 按照指定的定位方式寻找象。
	 * 
	 * @param by the locator of the elements to be find
	 * @return the webelements you want to find
	 * @throws RuntimeException
	 */
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
		operationCheck(isSucceed);
		return elements;
	}

	/**
	 * override the findElement method, adding user defined log.</BR>
	 * 按照指定的定位方式寻找象。
	 * 
	 * @param by the locator of the element to be find
	 * @return the first element accord your locator
	 * @throws RuntimeException
	 */
	protected WebElement findElement(By by) {
		boolean isSucceed = false;
		WebElement element = null;
		try {
			List<WebElement> elements = driver.findElements(by);
			if (elements.size() > 0) {
				element = elements.get(0);
			}
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return element;
	}

	/**
	 * store the WebDriverWebTable object, it only changes on By changing</BR>
	 * 缓存WebTable对象，在WebTable对象不为空的情况下（为空则直接新建对象），</BR>
	 * 如果定位方式相同则直接返回原有对象，否则重新创建WebTable对象。
	 * 
	 * @param tabBy the element locator By
	 */
	private synchronized WebDriverWebTable tableCache(By tabBy) {
		if (tabFinder == null) {
			tabFinder = tabBy;
			return new WebDriverWebTable(driver, tabBy);
		} else {
			if (tabBy.toString().equals(tabFinder.toString())) {
				return webTable;
			} else {
				tabFinder = tabBy;
				return new WebDriverWebTable(driver, tabBy);
			}
		}
	}

	/**
	 * refresh the webtable on the same locator, only if it changes.</BR>
	 * 如果同一定位方式的WebTable内容发生变化需要重新定位，则需要刷新WebTable。
	 */
	protected synchronized void tableRefresh(){
		WebDriverWebPublic.tabFinder = null;
		WebDriverWebPublic.webTable = null;		
	}

	/**
	 * get row count of a webtable.</BR>
	 * 返回一个WebTable的行的总数。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @return the row count of the webTable
	 * @throws RuntimeException
	 */
	protected int tableRowCount(By tabBy) {
		boolean isSucceed = false;
		int rowCount = 0;
		try {
			webTable = tableCache(tabBy);
			rowCount = webTable.rowCount();
			isSucceed = true;
			pass("the webTable " + tabBy.toString() + "has row count: [ " + rowCount + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return rowCount;
	}

	/**
	 * get column count of a specified webtable row.</BR>
	 * 返回一个WebTable在制定行上的列的总数。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @param rowNum row index of your webTable to count
	 * @return the column count of the row in webTable
	 * @throws RuntimeException
	 */
	protected int tableColCount(By tabBy, int rowNum) {
		boolean isSucceed = false;
		int colCount = 0;
		try {
			webTable = tableCache(tabBy);
			colCount = webTable.colCount(rowNum);
			isSucceed = true;
			pass("count columns of the webTable " + tabBy.toString() 
				+ " on the row [ " + rowNum + " ], got: [ " + colCount + " ]");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return colCount;
	}

	/**
	 * get the element in the webTable cell by row and col index.</BR>
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
		boolean isSucceed = false;
		WebElement element = null;
		try {
			webTable = tableCache(tabBy);
			element = webTable.childItem(row, col, type, index);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return element;
	}

	/**
	 * get the cell text of the webTable on specified row and column.</BR>
	 * 返回WebTable的指定行和列的中的文本内容。
	 * 
	 * @param tabBy By, by which you can locate the webTable
	 * @param row row index of the webTable.
	 * @param col column index of the webTable.
	 * @return the cell text
	 * @throws RuntimeException
	 */
	protected String tableCellText(By tabBy, int row, int col) {
		boolean isSucceed = false;
		String text = null;
		try {
			webTable = tableCache(tabBy);
			text = webTable.cellText(row, col);
			pass("the text of cell[" + row + "," + col + "] is: [ " + text + " ]");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return text;
	}

	/**
	 * wait for the element visiable in timeout setting.</BR>
	 * 在指定时间内等待，直到对象可见。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForElementVisible(By by, int seconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, seconds);
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
		operationCheck(isSucceed);
		return isExists;
	}

	/**
	 * wait for the element clickable in timeout setting.</BR>
	 * 在指定时间内等待，直到对象能够被点击。
	 * 
	 * @param by the element locator By
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForElementClickable(By by, int seconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, seconds);
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
		operationCheck(isSucceed);
		return isExists;
	}

	/**
	 * wait for text appears on element in timeout setting.</BR>
	 * 在指定时间内等待，直到指定对象上出现指定文本。
	 * 
	 * @param by the element locator By
	 * @param text the text to be found of element
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForTextOnElement(By by, String text, int seconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait(driver, seconds);
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
		operationCheck(isSucceed);
		return isExists;
	}

	/**
	 * wait for text appears in element attributes in timeout setting.</BR>
	 * 在指定时间内等待，直到指定对象的某个属性值等于指定文本。
	 * 
	 * @param by the element locator By
	 * @param text the text to be found in element attributes
	 * @param seconds timeout in seconds
	 * @throws RuntimeException
	 */
	protected boolean waitForTextOfElementAttr(By by, String text, int seconds) {
		boolean isSucceed = false;
		boolean isExists = false;
		WebDriverWait wait = new WebDriverWait((WebDriver) driver, seconds);
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
		operationCheck(isSucceed);
		return isExists;
	}

	/**
	 * wait for alert disappears in the time united by seconds.</BR>
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
				LOG.error(e);
				throw new RuntimeException(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * make the alert dialog not to appears.</BR>
	 * 通过JS函数重载，在对话框（Alert）出现之前点击掉它，或者说等价于不让其出现。
	 * 
	 * @throws RuntimeException
	 */
	protected void ensrueBeforeAlert() {
		boolean isSucceed = false;
		try {
			driver.executeScript("window.alert = function() {}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * make the warn dialog not to appears when window.close().</BR>
	 * 通过JS函数重载，在浏览器窗口关闭之前除去它的告警提示。
	 * 
	 * @throws RuntimeException
	 */
	protected void ensureBeforeWinClose() {
		boolean isSucceed = false;
		String js = "window.close = function(){" 
				+ " window.opener=null; " 
				+ " window.open('','_self');"
				+ " window.close();}";
		try {
			driver.executeScript(js);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * make the confirm dialog not to appears choose default option OK.</BR>
	 * 通过JS函数重载，在确认框（Confirm）出现之前点击确认，或者说等价于不让其出现而直接确认。
	 * 
	 * @throws RuntimeException
	 */
	protected void ensureBeforeConfirm() {
		boolean isSucceed = false;
		try {
			driver.executeScript("window.confirm = function() {return true}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * make the confirm dialog not to appears choose default option Cancel.</BR>
	 * 通过JS函数重载，在确认框（Confirm）出现之前点击取消，或者说等价于不让其出现而直接取消。
	 * 
	 * @throws RuntimeException
	 */
	protected void dismissBeforeConfirm() {
		boolean isSucceed = false;
		try {
			driver.executeScript("window.confirm = function() {return false}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * make the prompt dialog not to appears choose default option OK.</BR>
	 * 通过JS函数重载，在提示框（Prompt）出现之前点击确认，或者说等价于不让其出现而直接确认。
	 * 
	 * @throws RuntimeException
	 */
	protected void ensureBeforePrompt() {
		boolean isSucceed = false;
		try {
			driver.executeScript("window.prompt = function() {return true}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * make the prompt dialog not to appears choose default option Cancel.</BR>
	 * 通过JS函数重载，在提示框（Prompt）出现之前点击取消，或者说等价于不让其出现而直接取消。
	 * 
	 * @throws RuntimeException
	 */
	protected void dismisBeforePrompt() {
		boolean isSucceed = false;
		try {
			driver.executeScript("window.prompt = function() {return false}");
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * choose OK/Cancel button's OK on alerts.</BR>
	 * 在弹出的对话框（Dialog）上点击确认/是等接受性按钮。
	 * 
	 * @throws RuntimeException
	 */
	protected void chooseOKOnAlert() {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().accept();
			isSucceed = true;
			pass("click OK button on alert");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * choose Cancel on alerts.</BR>
	 * 在弹出的对话框（Dialog）上点击取消/否等拒绝性按钮。
	 * 
	 * @throws RuntimeException
	 */
	protected void chooseCancelOnAlert() {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().dismiss();
			isSucceed = true;
			pass("click Cancel on alert dialog");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * get the text of the alerts.</BR>
	 * 返回对话框（Dialog）上的提示信息文本内容。
	 * 
	 * @return alert text string
	 * @throws RuntimeException
	 */
	protected String getTextOfAlert() {
		boolean isSucceed = false;
		String alerts = null;
		try {
			alerts = driver.switchTo().alert().getText();
			isSucceed = true;
			pass("the text of the alert is: " + alerts);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return alerts;
	}

	/**
	 * set text on alerts.</BR>
	 * 向对话框（InputBox）中输入文本。
	 * 
	 * @param text the text string you want to input on alerts
	 * @throws RuntimeException
	 */
	protected void setTextOnAlert(String text) {
		boolean isSucceed = false;
		try {
			driver.switchTo().alert().sendKeys(text);
			isSucceed = true;
			pass("set text [ " + text + " ] on alert");
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * use js to make the element to be un-hidden.</BR>
	 * 使用JS执行的方法强制让某些隐藏的控件显示出来。
	 * 
	 * @param element the element to be operate
	 * @throws RuntimeException
	 */
	protected void makeElementUnHidden(WebElement element) {
		boolean isSucceed = false;
		final String js = "arguments[0].style.visibility = 'visible'; "
						+ "arguments[0].style.height = '1px'; " 
						+ "arguments[0].style.width = '1px'; "
						+ "arguments[0].style.opacity = 1";
		try {
			driver.executeScript(js, element);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * use js to make the element to be un-hidden.</BR>
	 * 使用JS执行的方法强制让某些隐藏的控件显示出来。
	 * 
	 * @param by the By locator to find the element
	 * @throws RuntimeException
	 */
	protected void makeElementUnHidden(By by) {
		boolean isSucceed = false;
		final String js = "arguments[0].style.visibility = 'visible'; "
						+ "arguments[0].style.height = '1px'; " 
						+ "arguments[0].style.width = '1px'; "
						+ "arguments[0].style.opacity = 1";
		try {
			WebElement element = driver.findElement(by);
			driver.executeScript(js, element);
			isSucceed = true;
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * override the getTitle method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the title on your current session
	 * @throws RuntimeException
	 */
	protected String getWindowTitle() {
		boolean isSucceed = false;
		String title = null;
		try {
			title = driver.getTitle();
			isSucceed = true;
			pass("current window title is :" + title);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return title;
	}

	/**
	 * override the getCurrentUrl method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the url on your current session
	 * @throws RuntimeException
	 */
	protected String getCurrentUrl() {
		boolean isSucceed = false;
		String url = null;
		try {
			url = driver.getCurrentUrl();
			isSucceed = true;
			pass("current session url is :" + url);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return url;
	}

	/**
	 * override the getWindowHandles method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the window handlers set
	 * @throws RuntimeException
	 */
	protected Set<String> getWindowHandles() {
		boolean isSucceed = false;
		Set<String> handler = null;
		try {
			handler = driver.getWindowHandles();
			isSucceed = true;
			pass("window handlers are: " + handler.toString());
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return handler;
	}

	/**
	 * override the getWindowHandle method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the window handler string
	 * @throws RuntimeException
	 */
	protected String getWindowHandle() {
		boolean isSucceed = false;
		String handler = null;
		try {
			handler = driver.getWindowHandle();
			isSucceed = true;
			pass("current window handler is:" + handler);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return handler;
	}

	/**
	 * override the getPageSource method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return the page source string
	 * @throws RuntimeException
	 */
	protected String getPageSource() {
		boolean isSucceed = false;
		String source = null;
		try {
			source = driver.getPageSource();
			isSucceed = true;
			pass("page source begins with: " + source.substring(0, 50));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return source;
	}

	/**
	 * override the getSessionId method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @return current session id string
	 * @throws RuntimeException
	 */
	protected String getSessionId() {
		boolean isSucceed = false;
		String sessionId = null;
		try {
			sessionId = driver.getSessionId().toString();
			isSucceed = true;
			pass("current sessionid is:" + sessionId);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return sessionId;
	}

	/**
	 * override the getTagName method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the tagname string
	 * @throws RuntimeException
	 */
	protected String getTagName(WebElement element) {
		boolean isSucceed = false;
		String tagName = null;
		try {
			tagName = element.getTagName();
			isSucceed = true;
			pass("element's TagName is: " + tagName);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return tagName;
	}

	/**
	 * override the getTagName method, find the element by By and get its tag name.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the tagname string
	 * @throws RuntimeException
	 */
	protected String getTagName(By by) {
		boolean isSucceed = false;
		String tagName = null;
		try {
			tagName = driver.findElement(by).getTagName();
			isSucceed = true;
			pass("element [ " + by.toString() + " ]'s TagName is: " + tagName);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return tagName;
	}

	/**
	 * override the getAttribute method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 * @throws RuntimeException
	 */
	protected String getAttribute(WebElement element, String attributeName) {
		boolean isSucceed = false;
		String value = null;
		try {
			value = element.getAttribute(attributeName);
			isSucceed = true;
			pass("element's " + attributeName + "is: " + value);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return value;
	}

	/**
	 * override the getAttribute method, find the element by By and get its attribute value.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 * @throws RuntimeException
	 */
	protected String getAttribute(By by, String attributeName) {
		boolean isSucceed = false;
		String value = null;
		try {
			value = driver.findElement(by).getAttribute(attributeName);
			isSucceed = true;
			pass("element [ " + by.toString() + " ]'s " + attributeName + "is: " + value);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return value;
	}

	/**
	 * override the isSelected method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement selected
	 * @throws RuntimeException
	 */
	protected boolean isSelected(WebElement element) {
		boolean isSucceed = false;
		boolean isSelected = false;
		try {
			isSelected = element.isSelected();
			isSucceed = true;
			pass("element selected? " + String.valueOf(isSelected));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isSelected;
	}

	/**
	 * override the isSelected method, the element to be find by By.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement selected
	 * @throws RuntimeException
	 */
	protected boolean isSelected(By by) {
		boolean isSucceed = false;
		boolean isSelected = false;
		try {
			isSelected = driver.findElement(by).isSelected();
			isSucceed = true;
			pass("element [ " + by.toString() + " ] selected? "	+ String.valueOf(isSelected));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isSelected;
	}

	/**
	 * override the isEnabled method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement enabled
	 * @throws RuntimeException
	 */
	protected boolean isEnabled(WebElement element) {
		boolean isSucceed = false;
		boolean isEnabled = false;
		try {
			isEnabled = element.isEnabled();
			isSucceed = true;
			pass("element enabled? " + String.valueOf(isEnabled));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isEnabled;
	}

	/**
	 * override the isEnabled method, the element to be find by By.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement enabled
	 * @throws RuntimeException
	 */
	protected boolean isEnabled(By by) {
		boolean isSucceed = false;
		boolean isEnabled = false;
		try {
			isEnabled = driver.findElement(by).isEnabled();
			isSucceed = true;
			pass("element [ " + by.toString() + " ] enabled? " + String.valueOf(isEnabled));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isEnabled;
	}

	/**
	 * override the getText method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @throws RuntimeException
	 */
	protected String getText(WebElement element) {
		boolean isSucceed = false;
		String text = null;
		try {
			text = element.getText();
			isSucceed = true;
			pass("element text is:" + text);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return text;
	}

	/**
	 * override the getText method, find the element by By and get its own text.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the text string
	 * @throws RuntimeException
	 */
	protected String getText(By by) {
		boolean isSucceed = false;
		String text = null;
		try {
			text = driver.findElement(by).getText();
			isSucceed = true;
			pass("element [ " + by.toString() + " ]'s text is: " + text);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return text;
	}

	/**
	 * override the isDisplayed method, adding user defined log.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement displayed
	 * @throws RuntimeException
	 */
	protected boolean isDisplayed(WebElement element) {
		boolean isSucceed = false;
		boolean isDisplayed = false;
		try {
			isDisplayed = element.isDisplayed();
			isSucceed = true;
			pass("element displayed? " + String.valueOf(isDisplayed));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isDisplayed;
	}

	/**
	 * override the isDisplayed method, the element to be find by By.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement displayed
	 * @throws RuntimeException
	 */
	protected boolean isDisplayed(By by) {
		boolean isSucceed = false;
		boolean isDisplayed = false;
		try {
			isDisplayed = driver.findElement(by).isDisplayed();
			isSucceed = true;
			pass("element [ " + by.toString() + " ] displayed? " + String.valueOf(isDisplayed));
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return isDisplayed;
	}

	/**
	 * get its css property value.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param element the webelement you want to operate
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 * @throws RuntimeException
	 */
	protected String getCssValue(WebElement element, String propertyName) {
		boolean isSucceed = false;
		String cssValue = null;
		try {
			cssValue = element.getCssValue(propertyName);
			isSucceed = true;
			pass("element's css [" + propertyName + "] value is:" + cssValue);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return cssValue;
	}

	/**
	 * find the element by By and get its css property value.</BR>
	 * 与工具原生API作用完全一致，只是增加了操作结果检查和日志记录。
	 * 
	 * @param by the locator you want to find the element
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 * @throws RuntimeException
	 */
	protected String getCssValue(By by, String propertyName) {
		boolean isSucceed = false;
		String cssValue = null;
		try {
			cssValue = driver.findElement(by).getCssValue(propertyName);
			isSucceed = true;
			pass("element [ " + by.toString() + " ]'s css[" 
				+ propertyName + "] value is: " + cssValue);
		} catch (WebDriverException e) {
			LOG.error(e);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		operationCheck(isSucceed);
		return cssValue;
	}
}