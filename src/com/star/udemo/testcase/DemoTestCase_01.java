package com.star.udemo.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.frame.basecase.WebDriverBaseCase;

public class DemoTestCase_01 extends WebDriverBaseCase {

	@Test
	public void baiduSearchExam() {
		String searchText = "砖家叫兽";

		startWebDriver();
		windowMaximize();

		get("http://www.baidu.com/");
		sendKeys(By.id("kw"), searchText);
		click(By.id("su"));
		click(By.linkText(searchText + "_百度百科"));

		closeWebDriver();
	}
}