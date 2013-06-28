package com.star.udemo.testcase;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import com.star.frame.basecase.WebDriverBaseCase;
import com.star.testdata.fileio.ExcelParseUtils;

public class DPM_RM_Query extends WebDriverBaseCase {
	private String fileName = null;
	private String rmNo = null;
	private String sysName = null;
	private ExcelParseUtils xls = null;
	private static String verNo = null;

	private void createFiles(String filenName) {
		File file = new File(filenName);
		if (file.exists()) {
			file.delete();
		}
		File parent = file.getParentFile();
		if (null != parent && !parent.exists()) {
			parent.mkdirs();
		}
	}

	@Test(alwaysRun = true)
	public void enterRMQueryMenu() {
		this.startWebDriver();
		this.get("http://pws.paic.com.cn/m/");
		this.get("http://sqms-dpm.paic.com.cn/dpm/");
		this.click(By.xpath("//div[text()='版本管理']"));
		this.click(By.xpath("//span[text()='版本管理']"));
	}

	@Test(dependsOnMethods={"enterRMQueryMenu"})
	@Parameters({"versionNo"})
	public void queryRMBaseInfo(String versionNo) {
		verNo = versionNo;
		this.selectFrame("rmManage");
		this.sendKeys(By.id("rmNo"), verNo);
		this.click(By.id("searchBtnId"));
		ASSERT.assertTrue(elementExists(By.id("rmListTable")));
	}

	@Test(dependsOnMethods={"queryRMBaseInfo"})
	public void queryRMPlanInfo() {
		sysName = this.tableCellText(By.id("rmListTable"), 2, 3);
		rmNo = this.tableCellText(By.id("rmListTable"), 2, 2);
		String filePath = "D:\\01_常规版本\\" + sysName + "\\" + verNo;
		fileName = verNo + "版本信息.xls";
		createFiles(filePath + "\\" + fileName);
		xls = new ExcelParseUtils(filePath, fileName);

		this.click(By.xpath("//a[contains(@onclick,'" + verNo + "')]"));

		this.selectDefaultWindowFrame();
		this.selectFrame(rmNo);

		this.click(By.linkText("任务执行计划"));
		if (this.getAttribute(By.id("nest_plan_box"), "style").contains("none")) {
			this.click(By.id("nest_plan"));
		}
		xls.setExcelValue("版本计划", 1, 1, "测试版本首次移交");
		xls.setExcelValue("版本计划", 2, 1, "测试版本首次部署");
		xls.setExcelValue("版本计划", 3, 1, "ST/UAT完成时间");
		xls.setExcelValue("版本计划", 4, 1, "正式发布版本移交");
		xls.setExcelValue("版本计划", 5, 1, "正式发布计划开始");
		xls.setExcelValue("版本计划", 6, 1, "正式发布计划完成");

		xls.setExcelValue("版本计划", 1, 2, this.getAttribute(By.id("dateStgFstTransferPlan"), "value"));
		xls.setExcelValue("版本计划", 2, 2, this.getAttribute(By.id("dateStgFstDeployPlan"), "value"));
		xls.setExcelValue("版本计划", 3, 2, this.getAttribute(By.id("dateStgStPlan"), "value"));
		xls.setExcelValue("版本计划", 4, 2, this.getAttribute(By.id("datePrdTransferPlan"), "value"));
		xls.setExcelValue("版本计划", 5, 2, this.getAttribute(By.id("dateReleaseBeginPlan1"), "value"));
		xls.setExcelValue("版本计划", 6, 2, this.getAttribute(By.id("dateReleaseEndPlan1"), "value"));
	}

	@Test(dependsOnMethods={"queryRMPlanInfo"})
	public void queryRMContentInfo() {
		this.click(By.linkText("版本内容"));
		String tabXpath = "//form[@id='removeSrForm']/div/table";
		ASSERT.assertTrue(elementExists(By.xpath(tabXpath)));

		List<String> datas = new ArrayList<String>();
		List<String> needs = new ArrayList<String>();
		datas.add("需求编号");
		datas.add("原始需求");
		datas.add("所属版本");
		datas.add("需求状态");
		datas.add("需求内容");
		datas.add("编码");
		datas.add("需求类型");
		datas.add("需求提交人");
		datas.add("需求提出人");
		datas.add("提交部门");

		needs.add("需求内容");
		needs.add("原始需求");
		needs.add("需求类型");
		needs.add("需求提交人");
		needs.add("需求提出人");
		needs.add("提交部门");

		int rowCount = this.tableRowCount(By.xpath(tabXpath));
		for (int i = 2; i <= rowCount; i++) {
			this.selectDefaultWindowFrame();
			this.click(By.linkText(verNo));
			this.selectFrame(rmNo);

			String srNo = this.tableCellText(By.xpath(tabXpath), i, 2);
			String needNo = this.tableCellText(By.xpath(tabXpath), i, 6);
			String rStatus = this.tableCellText(By.xpath(tabXpath), i, 4);
			String rContent = this.tableCellText(By.xpath(tabXpath), i, 3);
			String rdOwner = this.tableCellText(By.xpath(tabXpath), i, 10);
			String rType = this.tableCellText(By.xpath(tabXpath), i, 5);
			datas.add(srNo);
			datas.add(needNo);
			needs.add(needNo);
			datas.add(rmNo);
			datas.add(rStatus);
			datas.add(rContent);
			needs.add(rContent);
			datas.add(rdOwner);
			datas.add(rType);
			needs.add(rType);
			this.click(By.linkText(needNo));

			this.selectDefaultWindowFrame();
			this.selectFrame(needNo);

			String presenter = this.getAttribute(By.id("presenter"), "value");
			String submitPerson = this.getAttribute(By.id("submitPerson"), "value");
			String inputDept = this.getAttribute(By.id("inputdept"), "value");
			datas.add(presenter);
			needs.add(presenter);
			datas.add(submitPerson);
			needs.add(submitPerson);
			datas.add(inputDept);
			needs.add(inputDept);
		}
		xls.putListToExcelWithNoIgnore("开发需求", datas, rowCount);
		xls.putListToExcelWithNoIgnore("用户需求", needs, rowCount);
	}
}