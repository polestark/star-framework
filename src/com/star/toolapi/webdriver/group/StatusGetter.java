package com.star.toolapi.webdriver.group;

import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class StatusGetter{

	/**
	 * override the getTitle method, adding user defined log.
	 * 
	 * @param driver the WebDriver instance
	 * @return the title on your current session
	 */
	protected String getTitle(WebDriver driver) {
		return driver.getTitle();
	}

	/**
	 * override the getCurrentUrl method, adding user defined log.
	 * 
	 * @param driver the WebDriver instance
	 * @return the url on your current session
	 */
	protected String getCurrentUrl(WebDriver driver) {
		return driver.getCurrentUrl();
	}

	/**
	 * override the getWindowHandles method, adding user defined log.
	 * 
	 * @param driver the WebDriver instance
	 * @return the window handlers set
	 */
	protected Set<String> getWindowHandles(WebDriver driver) {
		return driver.getWindowHandles();
	}

	/**
	 * override the getWindowHandle method, adding user defined log.
	 * 
	 * @param driver the WebDriver instance
	 * @return the window handler string
	 */
	protected String getWindowHandle(WebDriver driver) {
		return driver.getWindowHandle();
	}

	/**
	 * override the getPageSource method, adding user defined log.
	 * 
	 * @param driver the WebDriver instance
	 * @return the page source string
	 */
	protected String getPageSource(WebDriver driver) {
		return driver.getPageSource();
	}

	/**
	 * override the getSessionId method, adding user defined log.
	 * 
	 * @param driver the RemoteWebDriver instance
	 * @return current session id string
	 */
	protected String getSessionId(RemoteWebDriver driver) {
		return driver.getSessionId().toString();
	}

	/**
	 * override the getTagName method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 * @return the tagname string
	 */
	protected String getTagName(WebElement element) {
		return element.getTagName();
	}

	/**
	 * override the getTagName method, find the element by By and get its tagname.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @return the tagname string
	 */
	protected String getTagName(WebDriver driver, By by) {
		return driver.findElement(by).getTagName();
	}

	/**
	 * override the getAttribute method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 */
	protected String getAttribute(WebElement element, String attributeName) {
		return element.getAttribute(attributeName);
	}

	/**
	 * override the getAttribute method, find the element by By and get its attribute value.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @param attributeName the name of the attribute you want to get
	 * @return the attribute value string
	 */
	protected String getAttribute(WebDriver driver, By by, String attributeName) {
		return driver.findElement(by).getAttribute(attributeName);
	}

	/**
	 * override the isSelected method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement selected
	 */
	protected boolean isSelected(WebElement element) {
		return element.isSelected();
	}

	/**
	 * override the isSelected method, the element to be find by By.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement selected
	 */
	protected boolean isSelected(WebDriver driver, By by) {
		return driver.findElement(by).isSelected();
	}

	/**
	 * override the isEnabled method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement enabled
	 */
	protected boolean isEnabled(WebElement element) {
		return element.isEnabled();
	}

	/**
	 * override the isEnabled method, the element to be find by By.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement enabled
	 */
	protected boolean isEnabled(WebDriver driver, By by) {
		return driver.findElement(by).isEnabled();
	}

	/**
	 * override the getText method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 */
	protected String getText(WebElement element) {
		return element.getText();
	}

	/**
	 * override the getText method, find the element by By and get its own text.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @return the text string
	 */
	protected String getText(WebDriver driver, By by) {
		return driver.findElement(by).getText();
	}

	/**
	 * override the isDisplayed method, adding user defined log.
	 * 
	 * @param element the webelement you want to operate
	 * @return the bool value of whether is the WebElement displayed
	 */
	protected boolean isDisplayed(WebElement element) {
		return element.isDisplayed();
	}

	/**
	 * override the isDisplayed method, the element to be find by By.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @return the bool value of whether is the WebElement displayed
	 */
	protected boolean isDisplayed(WebDriver driver, By by) {
		return driver.findElement(by).isDisplayed();
	}

	/**
	 * get its css property value.
	 * 
	 * @param element the webelement you want to operate
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 */
	protected String getCssValue(WebElement element, String propertyName) {
		return element.getCssValue(propertyName);
	}

	/**
	 * find the element by By and get its css property value.
	 * 
	 * @param driver the WebDriver instance
	 * @param by the locator you want to find the element
	 * @param propertyName the name of the property you want to get
	 * @return the css property value string
	 */
	protected String getCssValue(WebDriver driver, By by, String propertyName) {
		return driver.findElement(by).getCssValue(propertyName);
	}	
}