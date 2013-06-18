package com.star.core.selenium;

/**
 * 封装整体思路：
 * 1、封装常用方法，每个方法对SeleniumException进行捕获，其余的直接抛出RuntimeException；
 * 2、对于封装过的方法，存在SeleniumException的操作为失败，否则默认为成功，失败的操作在
 * 	  operationCheck中进行截图、报告错误、抛出RuntimeException操作，强制出错则停止运行。
 *  
 * @author 测试仔刘毅
 **/

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import static org.testng.AssertJUnit.assertTrue;

import com.star.core.selenium.SeleniumController;
import com.star.logging.frame.LoggingManager;
import com.star.support.externs.Win32GuiByAu3;
import com.star.support.externs.Win32GuiByVbs;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumWebPublic extends SeleniumController {

	private static final LoggingManager LOG = new LoggingManager(SeleniumWebPublic.class.getName());
	protected static final Win32GuiByAu3 AU3 = new Win32GuiByAu3();

	private long commitTimeout = 60000;
	private long stepTimeout = 20000;

	/**
	 * timeout setting for whole procedure. enviTimeout timeout setting for commit options
	 * 
	 */
	protected void setCommitTimeout(long cmtTimeout) {
		this.commitTimeout = cmtTimeout;
	}

	/**
	 * get the whole procedure timeout.
	 * 
	 * @return timeout setting for commit options
	 */
	protected long getCommitTimeout() {
		return this.commitTimeout;
	}

	/**
	 * set step timeout.
	 * 
	 * @param stepTimeout timeout setting for single steps
	 */
	protected void setStepTimeout(long stepTimeout) {
		this.stepTimeout = stepTimeout;
	}

	/**
	 * get step time out.
	 * 
	 * @return timeout setting for single steps
	 */
	protected long getStepTimeout() {
		return this.stepTimeout;
	}

	/**
	 * public method for handle assertions and screenshot.
	 * 
	 * @param isSucceed if your operation success
	 * @throws RuntimeException
	 */
	protected void operationCheck(boolean isSucceed) {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		String timeString = SBF.formatedTime("-yyyyMMdd-HHmmssSSS");
		try {
			assertTrue(isSucceed);
		} catch (AssertionError ae) {
			LOG.error(ae, "method [" + methodName + "] run failed!");
			selenium.captureScreenshot(LOG_DIR + methodName + timeString + ".png");
			throw new RuntimeException("Assert Failed:" + ae.getMessage());
		}
	}

	/**
	 * a new method for waitForPageToLoad.
	 * 
	 * @throws RuntimeException
	 */
	protected void syncBrowser() {
		boolean isSucceed = false;
		try {
			selenium.waitForPageToLoad(String.valueOf(getCommitTimeout()));
			isSucceed = true;
		} catch (SeleniumException se) {
			LOG.error(se);
		}
		operationCheck(isSucceed);
	}

	/**
	 * syncronize browser used Ajax, judge using jQuery.active.
	 * 
	 * @param timeout timeout setting
	 * @throws RuntimeException
	 */
	protected void syncAjaxByJQuery(String timeout) {
		boolean isSucceed = false;
		try {
			selenium.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery.active == 0",
					timeout);
			isSucceed = true;
		} catch (SeleniumException se) {
			LOG.error(se);
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * syncronize browser used Ajax, judge using jQuery.active.
	 * 
	 * @throws RuntimeException
	 */
	protected void syncAjaxByJQuery() {
		syncAjaxByJQuery(String.valueOf(getStepTimeout()));
	}

	/**
	 * syncronize browser used Ajax, judge using Ajax.activeRequestCount.
	 * 
	 * @param timeout timeout setting
	 * @throws RuntimeException
	 */
	protected void syncAjaxByPrototype(String timeout) {
		boolean isSucceed = false;
		try {
			selenium.waitForCondition(
					"selenium.browserbot.getCurrentWindow().Ajax.activeRequestCount == 0", timeout);
			isSucceed = true;
		} catch (SeleniumException se) {
			LOG.error(se);
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * syncronize browser used Ajax, judge using Ajax.activeRequestCount.
	 * 
	 * @throws RuntimeException
	 */
	protected void syncAjaxByPrototype() {
		syncAjaxByPrototype(String.valueOf(getStepTimeout()));
	}

	/**
	 * syncronize browser used Ajax, judge using dojo.io.XMLHTTPTransport.inFlight.length.
	 * 
	 * @param timeout timeout setting
	 * @throws RuntimeException
	 */
	protected void syncAjaxByDojo(String timeout) {
		boolean isSucceed = false;
		try {
			selenium.waitForCondition(
					"selenium.browserbot.getCurrentWindow().dojo.io.XMLHTTPTransport.inFlight.length == 0",
					timeout);
			isSucceed = true;
		} catch (SeleniumException se) {
			LOG.error(se);
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		operationCheck(isSucceed);
	}

	/**
	 * syncronize browser used Ajax, judge using dojo.io.XMLHTTPTransport.inFlight.length.
	 * 
	 * @throws RuntimeException
	 */
	protected void syncAjaxByDojo() {
		syncAjaxByDojo(String.valueOf(getStepTimeout()));
	}

	/**
	 * a new method for waitForFrameToLoad.
	 * 
	 * @param locator the locator of the frame
	 * @throws RuntimeException
	 */
	protected void syncFrame(String locator) {
		boolean isSucceed = false;
		try {
			selenium.waitForFrameToLoad(locator, String.valueOf(getCommitTimeout()));
			isSucceed = true;
		} catch (SeleniumException se) {
			LOG.error(se);
		}
		operationCheck(isSucceed);
	}

	/**
	 * rewrite selectwindow method, wait for window syncronize, using timeout setting.
	 * 
	 * @param locator the name/title of the window
	 * @throws RuntimeException
	 */
	public void selectWindow(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		String[] allWindows = null;
		while (System.currentTimeMillis() - timeBegins < getStepTimeout() && !isSucceed) {
			try {
				allWindows = selenium.getAllWindowTitles();
				for (int i = 0; i < allWindows.length; i++) {
					if (allWindows[i].contains(locator)) {
						selenium.selectWindow(locator);
						isSucceed = true;
						break;
					}
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * rewrite selectframe method, add timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void selectFrame(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			pause(1000);
			try {
				selenium.selectFrame(locator);
				isSucceed = true;
				break;
			} catch (SeleniumException se) {
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * get row count of a webtable
	 * 
	 * @param tabLocator the webtable locator
	 * @return the row count of the table
	 * @throws RuntimeException
	 */
	protected int tabRowCount(String tabLocator) {
		String locator = tabLocator.replace("@", "").replace("'", "");
		String rowCount = "0";
		try {
			rowCount = selenium.getTabRowCount(locator);
		} catch (SeleniumException se) {
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		return Integer.parseInt(rowCount);
	}

	/**
	 * get column count of a specified webtable row.
	 * 
	 * @param tabLocator : the webtable locator
	 * @param rowNum : row index of your table to count
	 * @return the column count of the row in table
	 * @throws RuntimeException
	 */
	protected int tabColCount(String tabLocator, int rowNum) {
		String locator = tabLocator.replace("@", "").replace("'", "");
		String colCount = "0";
		try {
			colCount = selenium.getTabColCount(locator, String.valueOf(rowNum));
		} catch (SeleniumException se) {
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		return Integer.parseInt(colCount);
	}

	/**
	 * get row count of a webtable(using xpath loop count)
	 * 
	 * @param tabLocator the webtable locator
	 * @return the row count of the table
	 * @throws RuntimeException
	 */
	protected int countTabRows(String tabLocator) {
		String locator = null;
		int count = 0;
		for (int i = 1; i < 999; i++) {
			locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(i) + "]";
			if (!selenium.isElementPresent(locator)) {
				count = i - 1;
				break;
			}
		}
		return count;
	}

	/**
	 * get column count of a specified webtable row(using xpath loop count).
	 * 
	 * @param tabLocator the webtable locator
	 * @param rowNum row index of your table to count
	 * @return the column count of the row in table
	 * @throws RuntimeException
	 */
	protected int countTabCols(String tabLocator, int rowNum) {
		String locator = null;
		int count = 0;
		for (int i = 1; i < 999; i++) {
			locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(rowNum) + "]/td["
					+ String.valueOf(i) + "]";
			if (!selenium.isElementPresent(locator)) {
				count = i - 1;
				break;
			}
		}
		return count;
	}

	/**
	 * judge if the webtable cell has any child element.
	 * 
	 * @param tabLocator the webtable locator
	 * @param row row number of your cell
	 * @param col column number of your cell
	 * @return boolean
	 * @throws RuntimeException
	 */
	protected boolean isCellHasChild(String tabLocator, int row, int col) {
		boolean hasChild = false;
		String locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td["
				+ String.valueOf(col) + "]/*";
		if (selenium.isElementPresent(locator)) {
			hasChild = true;
		}
		return hasChild;
	}

	/**
	 * get text of a specified webtable cell.
	 * 
	 * @param tabLocator the webtable locator
	 * @param row row number of your cell
	 * @param col column number of your cell
	 * @return the text value of the cell
	 * @throws RuntimeException
	 */
	protected String getTabCellText(String tabLocator, int row, int col) {
		String text = null;
		String locator = tabLocator + "/tbody/tr[" + String.valueOf(row) + "]/td["
				+ String.valueOf(col) + "]";
		if (!tabLocator.startsWith("//")) {
			locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td["
					+ String.valueOf(col) + "]";
		}
		try {
			if (!selenium.isElementPresent(locator)) {
				text = selenium.getText(locator + "//*");
			} else {
				text = selenium.getText(locator);
			}
		} catch (SeleniumException se) {
			pause(1000);
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
		return text;
	}

	/**
	 * modify value of a specified webtable cell(for editbox).
	 * 
	 * @param tabLocator the webtable locator
	 * @param row row number of your cell
	 * @param col column number of your cell
	 * @param index the index of element in table cell
	 * @param setText the text you want to set into the edit
	 * @throws RuntimeException
	 */
	protected void editTableCell(String tabLocator, int row, int col, int index, String setText) {
		long timeBegins = System.currentTimeMillis();
		boolean isSucceed = false;
		String locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td["
				+ String.valueOf(col) + "]/input[" + String.valueOf(index) + "]";
		String javaScript = "this.browserbot.findElement(\"" + locator + "\").innerText = '"
				+ setText + "';";
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.getEval(javaScript);
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * modify value of a specified webtable cell(for picklist) by label.
	 * 
	 * @param tabLocator the webtable locator
	 * @param row row number of your cell
	 * @param col column number of your cell
	 * @param index the index of element in table cell
	 * @param setText the choice you want to choose from list, default use label
	 * @throws RuntimeException
	 */
	protected void selectTableCell(String tabLocator, int row, int col, int index, String setText) {
		String locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td["
				+ String.valueOf(col) + "]/select[" + String.valueOf(index) + "]";
		if (setText.contains("index=") || setText.contains("label=")) {
			newSelect(locator, setText);
		} else {
			newSelect(locator, "label=" + setText);
		}
	}

	/**
	 * click the element inside of a specified webtable cell.
	 * 
	 * @param tabLocator the webtable locator
	 * @param row row number of your cell
	 * @param col column number of your cell
	 * @param index the index of element in table cell
	 * @throws RuntimeException
	 */
	protected void clickTableCell(String tabLocator, int row, int col, int index) {
		String locator = "//table[" + tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td["
				+ String.valueOf(col) + "]//*[@onclick][" + String.valueOf(index) + "] | //table["
				+ tabLocator + "]/tbody/tr[" + String.valueOf(row) + "]/td[" + String.valueOf(col)
				+ "]//*[@type][" + String.valueOf(index) + "]";
		newClick(locator);
	}

	/**
	 * rewrite click method, add timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void newClick(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.click(locator);
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * rewrite click method, check pafa non-repeat click control.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void menuClickAndCheck(String locator, int checkSenconds) {
		boolean isSucceed = false;
		String errLocator = "//div[@class='error']/pre[contains(text(),'您的请求还未返回')]";
		long timeBegins = System.currentTimeMillis();
		while ((System.currentTimeMillis() - timeBegins < getStepTimeout()) && !isSucceed) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.click(locator);
					for (int i = 0; i < checkSenconds; i++) {
						pause(1000);
						isSucceed = !selenium.isElementPresent(errLocator);
						if (!isSucceed) {
							break;
						}
					}
				}
				if (new Win32GuiByVbs().ieVersion().startsWith("6.0")) {
					AU3.closeWindow("提示信息");
				} else {
					AU3.closeWindow("来自网页的消息");
				}
			} catch (SeleniumException se) {
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * rewrite type method, add timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @param text the text you want to set into the edit
	 * @throws RuntimeException
	 */
	protected void newType(String locator, String text) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.type(locator, text);
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * rewrite select method, add timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @param text the choice you want to select from the list
	 * @throws RuntimeException
	 */
	protected void newSelect(String locator, String text) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.select(locator, text);
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * fourcefully change the innertext,specially for not editable.
	 * 
	 * @param locator the element locator on the page
	 * @param setText the text you want to set into the element
	 * @throws RuntimeException
	 */
	protected void setCalendar(String locator, String setText) {
		boolean isSucceed = false;
		locator = locator.replace("@", "").replace("'", "");
		String javascript = "this.browserbot.findElement('" + locator + "').innerText = '"
				+ setText + "';";
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					selenium.getEval(javascript);
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for attribute not equals specified value during timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @param value the value of your element attribute
	 * @throws RuntimeException
	 */
	protected void waitForAttrNotEquals(String locator, String value) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (!selenium.getAttribute(locator).equals(value)) {
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for specified text within timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @param text the element text you want to wait for
	 * @throws RuntimeException
	 */
	protected void waitForText(String locator, String text) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator + "[contains(text(),'" + text + "')]")) {
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for specified text within timeout setting, fail if not found.
	 * 
	 * @param text the element text you want to wait for
	 * @throws RuntimeException
	 */
	protected void waitForTextPresent(String text) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isTextPresent(text)) {
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for element disappears with timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void waitForEleNotPresent(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (!selenium.isElementPresent(locator)) {
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for the specified element appears with timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void waitForElement(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			try {
				if (selenium.isElementPresent(locator)) {
					isSucceed = true;
					break;
				}
			} catch (SeleniumException se) {
				pause(1000);
			} catch (Exception re) {
				throw new RuntimeException(re.getMessage());
			}
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for element visiable with timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void waitForEleVisible(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			if (selenium.isVisible(locator)) {
				isSucceed = true;
				break;
			}
			pause(1000);
		}
		operationCheck(isSucceed);
	}

	/**
	 * wait for element not visiable with timeout setting.
	 * 
	 * @param locator the element locator on the page
	 * @throws RuntimeException
	 */
	protected void waitForEleNotVisible(String locator) {
		boolean isSucceed = false;
		long timeBegins = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeBegins < getStepTimeout()) {
			if (!selenium.isVisible(locator)) {
				isSucceed = true;
				break;
			}
			pause(1000);
		}
		operationCheck(isSucceed);
	}

	/**
	 * open some special windows, which can't be opened by click method.
	 * 
	 * @param locator the element locator, click witch can popup a new window
	 * @param sysAddress the system root address like http://egis-pos-stg.paic.com.cn
	 * @throws RuntimeException
	 */
	protected void openAndSelectWindow(String locator, String sysAddress) {
		String addressTo = null;
		int winCount = 0;
		try {
			winCount = selenium.getAllWindowTitles().length;
			if (locator.toLowerCase().contains("onclick")) {
				addressTo = selenium.getAttribute(locator).split("'")[1];
			} else if (locator.toLowerCase().contains("href")) {
				addressTo = selenium.getAttribute(locator);
			} else {
				LOG.error("only onclick and href supported, you can add a new method!");
			}
			if (addressTo != null) {
				if (addressTo.substring(0, 2).contains("/") && sysAddress != null) {
					addressTo = sysAddress + addressTo;
				} else if (addressTo.substring(0, 2).contains("/") && sysAddress == null) {
					LOG.error("please sign out the system url, such as http://egis-pos-stg.paic.com.cn");
					return;
				}
				selenium.openWindow(addressTo, "AddresslessWindow");
				selenium.waitForPopUp("AddresslessWindow", String.valueOf(getStepTimeout()));
				selenium.selectWindow("AddresslessWindow");
			} else {
				LOG.info("can not open new window, because the url you got is null!");
				selenium.click(locator.replace("\\@onclick", "").replace("\\@href", ""));
				String[] winName = selenium.getAllWindowTitles();
				if (winName.length == winCount) {
					LOG.error("selenium can not recognize the pop window!");
				} else {
					selenium.selectWindow(winName[winName.length - 1]);
				}
			}
		} catch (SeleniumException se) {
			LOG.error(se);
		} catch (Exception re) {
			throw new RuntimeException(re.getMessage());
		}
	}

	/**
	 * override the newOpenWindow method using default system root url null.
	 * 
	 * @param locator the element locator, click witch can popup a new window
	 * @throws RuntimeException
	 */
	protected void openAndSelectWindow(String locator) {
		openAndSelectWindow(locator, null);
	}

	/**
	 * get and write browser message to file using AU3 compiled exe.
	 * 
	 * @param title pop window title
	 * @param idOrName the id or name of the text's father element
	 * @param eleType the type of the element which to get text
	 * @param timeout time out setting to find the pop window
	 * @throws RuntimeException
	 */
	protected void assertErrors(String title, String idOrName, String eleType, int timeout) {
		String fileName = ROOT_DIR + "/error_info_" + SBF.getMilSecNow() + ".txt";
		AU3.assertErrors(title, idOrName, eleType, fileName, timeout);
		StringBuffer buffer = new StringBuffer();
		File file = null;
		BufferedReader br = null;

		try {
			file = new File(fileName);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String text = null;
			while ((text = br.readLine()) != null) {
				buffer.append(text);
			}
			String message = buffer.toString();
			if (message.contains("0") && message.replace("0", null) == null) {
				return;
			}
			LOG.error("*******获取到页面错误信息为：" + message);
			br.close();
		} catch (FileNotFoundException t) {
			LOG.info("can not get error record file, the case must be running correctly!");
		} catch (IOException re) {
			throw new RuntimeException(re.getMessage());
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}
}
