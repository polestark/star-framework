package com.star.toolapi.selenium;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.star.toolapi.selenium.ExtendSelenium;

public class ExtendDefaultSelenium extends DefaultSelenium implements ExtendSelenium {

	public ExtendDefaultSelenium(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	/**
	 * get the current webtable's row count, on condition initialize completed.
	 * put this code into selenium-server-standalone.jar/core/scripts/user-extension.js:
	 * 
	 *		Selenium.prototype.getTabRowCount = function(locator) {
				var table = this.browserbot.findElement(locator);
				return table.rows.length.toString();
			};
			
	 * @param 	locator the locator of the webtable
	 * @return 	row count of the current webtable
	 * @author 	PAICDOM/LIUYI027
	 * @throws	SeleniumException
	 **/
	public String getTabRowCount(String locator) {
		return this.commandProcessor.getString("getTabRowCount", new String[] { locator });
	}

	/**
	 * get the current webtable's column count, on condition initialize completed.
	 * put this code into selenium-server-standalone.jar/core/scripts/user-extension.js:
	 * 
	 * 		Selenium.prototype.getTabColCount = function(locator, rownum) {
				var table = this.browserbot.findElement(locator);
				return table.rows[rownum - 1].cells.length.toString();
			};
			
	 * @param 	locator	the locator of the webtable
	 * @param	rowIndex the row index of the table to count
	 * @return 	column count of the current webtable
	 * @author 	PAICDOM/LIUYI027
	 * @throws	SeleniumException
	 **/
	public String getTabColCount(String locator, String rowIndex) {
		return this.commandProcessor.getString("getTabColCount", new String[] { locator, rowIndex });
	}
}