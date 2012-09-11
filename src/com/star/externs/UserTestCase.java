package com.star.externs;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.frame.basecase.WebDriverBaseCase;

public class UserTestCase extends WebDriverBaseCase{
	

	@Test
	public void testBaidu() throws Exception {
				
		String kinString = "刘毅";
		
		startWebDriver("ie");
		
		get("http://www.baidu.com/");
		
		sendKeys(By.id("kw"), kinString);
		
		click(By.id("su"));
		
		warn("我艹！");
		fail("测试完毕！");

		closeWebDriver();
		
	}
}
