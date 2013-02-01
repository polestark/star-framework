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

	public RuntimeSupport(WebDriver driver){
		this.driver = driver;
	}

	/**
	 * take a screen shot and save the file by path and name</BR>
	 * 网页截图操作，按照指定的文件名称保存快照文件。
	 * 
	 * @param fileName the file path&name of the screenshot to be saved
	 * @throws RuntimeException
	 */
	public void screenShot(String fileName) {
		try {
			RemoteWebDriver swd = (RemoteWebDriver) new Augmenter().augment(driver);
			File file = ((TakesScreenshot) swd).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(file, new File(fileName));
		} catch (Throwable t) {
			LOG.error("使用RemoteWebDriver的TakesScreenshot发生异常，下面重新截屏！");
			new Win32GuiByAu3().screenCapture(fileName);
		}
	}
}