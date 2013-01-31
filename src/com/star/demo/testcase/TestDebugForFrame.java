package com.star.demo.testcase;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService.Builder;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.star.toolapi.webdriver.user.WebDriverListener;

public class TestDebugForFrame {

	WebDriverEventListener listener = new WebDriverListener();
	private static InternetExplorerDriverService service = null;
	private static WebDriver driver = null;

	@BeforeTest
	public void steUp(){
		System.setProperty("webdriver.ie.driver", "./lib/IEDriverServer.exe");
		Builder builder = new InternetExplorerDriverService.Builder();
		service = builder.usingAnyFreePort().withLogLevel(InternetExplorerDriverLogLevel.INFO).build();
		try {
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		driver = new EventFiringWebDriver(new RemoteWebDriver(service.getUrl(),
				DesiredCapabilities.internetExplorer())).register(listener);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}
	
	@Test
	public void frameWork(){
		driver.get("http://www.baidu.com/");
		driver.findElement(By.id("kw")).sendKeys("shout animals");
		driver.findElement(By.id("su")).click();
		driver.findElement(By.linkText("shout animals")).click();
	}
	
	@AfterTest
	public void tearDown(){
		driver.close();
		service.stop();
	}
}
