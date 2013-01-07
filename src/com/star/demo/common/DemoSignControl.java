package com.star.demo.common;

import static org.testng.AssertJUnit.assertTrue;
import org.openqa.selenium.By;
import com.star.toolapi.webdriver.WebDriverWebPublic;

public class DemoSignControl extends WebDriverWebPublic{

	private static String setUpAddr = property.get("setUpAddr");
	private static String openUrl = property.get("openUrl");

	/*egis-pos 用户登录*/
	public void userLogin(String userName, String passWord){
        
        startWebDriver(); 
        driver.get(setUpAddr + openUrl);
		windowMaximize();
		
		sendKeys(By.xpath("//input[@id='j_username']"), userName);
		sendKeys(By.xpath("//input[@id='j_password']"), passWord);
		click(By.xpath("//input[contains(@onclick,'loginformSubmit')]"));
		
		if (driver.getPageSource().contains("此网站的安全证书有问题")){
			click(By.xpath("//a[@id='overridelink']"));			
		}
		assertTrue(elementExists(By.linkText("综合查询"), 10));
	}

	/*egis-pos 用户退出登录*/
	public void userLogout() {
		selectDefaultWindowFrame();
		selectFrame(By.xpath("//iframe[@name='top' and contains(@src,'top')]"));
		focusOnActiveElement();
		click(By.xpath("//a[contains(@onclick,'closeMain')]/font"));
		chooseOKOnAlert();
	}
}