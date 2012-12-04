package com.star.demo.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.demo.framework.DemoBaseCase;

public class TestDebugForCase extends DemoBaseCase {
	
	@Test(timeOut=30000)
	public void testApi(){
		click(By.linkText("网点保全处理岗"));
		click(By.linkText("处理岗工作台"));
		selectFrame("content");
		click(By.xpath("//img[contains(@onclick,'searchQueueTask')]"));
		tableRowCount(By.id("workTable"));
		tableColCount(By.id("workTable"), 2);
		for (int i = 1; i <= 3; i ++){
			System.out.println(tableCellText(By.id("workTable"), 2, i));
		}
	}
}