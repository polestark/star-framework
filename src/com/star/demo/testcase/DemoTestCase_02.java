package com.star.demo.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import com.star.demo.framework.DemoBaseCase;

public class DemoTestCase_02 extends DemoBaseCase {

	@Test
	public void getQuestionnaireNoProcessInfo() {
		this.click(By.linkText("系统运营维护岗"));
		this.click(By.linkText("超期未反馈问题件导出"));
		this.selectFrame("content");

		this.selectByValue(By.name("corporation"), "ALL");
		this.selectByValue(By.name("branchCode"), "G02");
		this.sendKeys(By.name("startApplyDate"), "20120101");
		this.sendKeys(By.name("endApplyDate"), "20121201");
		this.selectByValue(By.name("symbol"), "<");
		this.sendKeys(By.name("overdueDate"), "1000");

		this.click(By.xpath("//img[contains(@onclick,'submit_pagequery_per()')]"));
		this.waitForElementVisible(By.id("id_getQuestionnaireNoProcessInfo_query"), 5);
		this.click(By.xpath("//img[contains(@onclick,'exportExcel()')]"));

		String fileName = CONFIG.get("respath") + "超期未反馈问题件导出" + STRUTIL.formatedTime(FORMATTER)
				+ ".xls";

		AU3.fileDownload("文件下载", "另存为", fileName, 20);
	}

	@Test(dependsOnMethods = { "getQuestionnaireNoProcessInfo" }, alwaysRun = true)
	public void settlePrintQuery() {

		this.browserRefresh();

		selectWindow("平安养老保险股份有限公司保险业务管理系统");
		this.click(By.linkText("收付汇总岗"));
		this.click(By.linkText("收付汇总打印"));
		this.selectFrame(By.id("content"));

		failAndExit("我觉得此处可失败！");

		// sendKeys(By.name("settlementNettingSeq"), "1110000227150");
		// click(By.name("button"));

		// waitForElementVisible(By.id("querySettlementNettingForLongTable_table"),
		// 5);
		// click(tableChildElement(By.id("querySettlementNettingForLongTable_table"),
		// 3, 3, "link", 1));
	}
}