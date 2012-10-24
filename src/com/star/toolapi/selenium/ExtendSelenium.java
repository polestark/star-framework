package com.star.toolapi.selenium;

/**
 * 说明：
 * 1、继承com.star.toolapi.selenium工具原生API接口；
 * 2、修改selenium-server-standalone.jar/core/scripts/user-extension.js，扩展自定义操作;
 * 
 * @author 测试仔刘毅
 */

import com.thoughtworks.selenium.Selenium;

public interface ExtendSelenium extends Selenium {

	/**
	 * get the current webtable's row count, on condition initialize completed.
	 * put this code into selenium-server-standalone.jar/core/scripts/user-extension.js:
	 * 
	 *		Selenium.prototype.getTabRowCount = function(locator) {
				var table = this.browserbot.findElement(locator);
				return table.rows.length.toString();
			};
	 * 
	 * @param 	locator the locator of the webtable
	 * @return 	row count of the current webtable
	 * @throws	SeleniumException
	 */
	public abstract String getTabRowCount(String locator);

	/**
	 * get the current webtable's column count, on condition initialize completed.
	 * put this code into selenium-server-standalone.jar/core/scripts/user-extension.js:
	 * 
	 * 		Selenium.prototype.getTabColCount = function(locator, rownum) {
				var table = this.browserbot.findElement(locator);
				return table.rows[rownum - 1].cells.length.toString();
			};
	 * 
	 * @param 	locator the locator of the webtable
	 * @param	rowIndex the row index of the table to count
	 * @return 	column count of the current webtable
	 * @throws	SeleniumException
	 */
	public abstract String getTabColCount(String locator, String rowIndex);
}