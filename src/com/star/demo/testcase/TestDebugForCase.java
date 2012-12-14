package com.star.demo.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.demo.framework.DemoBaseCase;

public class TestDebugForCase extends DemoBaseCase {
	
	@Test
	public void testApi(){
		this.click(By.linkText("系统运营维护岗"));
		this.click(By.linkText("超期未反馈问题件导出"));
		this.selectFrame("content");

		this.selectByValue(By.name("corporation"), "ALL");
		this.selectByValue(By.name("branchCode"), "G02");
		this.sendKeys(By.name("startApplyDate"), "20120101");
		this.sendKeys(By.name("endApplyDate"), "20121201");
		this.selectByValue(By.name("symbol"), "<");
		this.sendKeys(By.name("overdueDate"), "100");
		
		this.click(By.xpath("//img[contains(@onclick,'submit_pagequery_per()')]"));		
		this.waitForElementVisible(By.id("id_getQuestionnaireNoProcessInfo_query"), 5);
		this.click(By.xpath("//img[contains(@onclick,'exportExcel()')]"));
		
		String fileName = CONFIG.get("respath") + "超期未反馈问题件导出" + STRUTIL.formatedTime(FORMATTER) + ".xls";
		
		AU3.fileDownload("文件下载", "另存为", fileName, 20);
	}
}