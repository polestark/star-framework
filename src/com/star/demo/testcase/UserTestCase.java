package com.star.demo.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.frame.basecase.WebDriverBaseCase;

public class UserTestCase extends WebDriverBaseCase {

	@Test
	public void testCSV() {
		String searchText = "砖家叫兽";

		startWebDriver();
		windowMaximize();

		get("http://www.baidu.com/");
		setElementLocateTimeout(3);
		sendKeys(By.id("kw"), searchText);
		click(By.id("su"));
		click(By.linkText(searchText + "_百度百科"));

		closeWebDriver();
	}
}