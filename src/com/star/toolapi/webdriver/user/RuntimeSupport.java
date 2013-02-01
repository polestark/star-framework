package com.star.toolapi.webdriver.user;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.star.logging.frame.LoggingManager;
import com.star.support.externs.Win32GuiByAu3;

public class RuntimeSupport{
	
	private WebDriver driver;
	private final LoggingManager LOG = new LoggingManager(RuntimeSupport.class.getName());
	
	public RuntimeSupport(){
	}

	public RuntimeSupport(WebDriver driver){
		this.driver = driver;
	}

	/**
	 * take a screen shot and save the file by path and name</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param object the WebDriver instance.
	 * @param fileName the file path&name of the screenshot to be saved.
	 */
	public void screenShot(WebDriver object, String fileName) {
		try {
			screenShot(fileName, object);
		} catch (Throwable t) {
			LOG.error("RemoteWebDriver截图异常，下面使用Autoit重新截屏！");
			new Win32GuiByAu3().screenCapture(fileName);
		}
	}

	/**
	 * take a screen shot and save the file by path and name</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param fileName the file path&name of the screenshot to be saved
	 * @throws IllegalArgumentException
	 */
	public void screenShot(String fileName) {
		if (null == driver){
			throw new IllegalArgumentException("the construction must have WebDriver object parameter!");
		}
		try {
			screenShot(fileName, driver);
		} catch (Throwable t) {
			LOG.error("RemoteWebDriver截图异常，下面使用Autoit重新截屏！");
			new Win32GuiByAu3().screenCapture(fileName);
		}
	}

	/**
	 * take a screen shot and save the file.</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param fileName the file path&name of the screenshot to be saved
	 * @param myDriver the WebDriver instance.
	 * @throws Exception
	 */
	private void screenShot(String fileName, WebDriver myDriver) throws Exception{
		RemoteWebDriver swd = (RemoteWebDriver) new Augmenter().augment(myDriver);
		File file = ((TakesScreenshot) swd).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(file, new File(fileName));
	}
}