package com.star.cases;

import org.testng.annotations.Test;
import com.star.frame.basecase.WebDriverBaseCase;

public class UserTestCase extends WebDriverBaseCase{

	@Test
	public void testCSV() throws Exception {
		System.out.println(System.getProperty("ENV_CHOICE"));
		pass("测试成功");
	}
	
	@Test(dependsOnMethods="testCSV")
	public void testCSV2() throws Exception {
		failAndExit("测试失败");
	}
}