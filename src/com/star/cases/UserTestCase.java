package com.star.cases;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.frame.basecase.WebDriverBaseCase;

public class UserTestCase extends WebDriverBaseCase {

	@Test
	public void testCSV() {

		String searchText = "砖家叫兽";

		startWebDriver();

		get("http://www.baidu.com/");
		sendKeys(By.id("kw"), searchText);
		click(By.id("su"));

		this.waitForElementVisible(By.xpath("//a[contains(text(), '百度知道')]/font[text() = '" + searchText + "']"), 5);

		closeWebDriver();
	}
}