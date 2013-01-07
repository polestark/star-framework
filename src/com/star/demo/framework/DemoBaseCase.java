package com.star.demo.framework;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterClass;
import com.star.frame.basecase.WebDriverBaseCase;
import com.star.demo.common.DemoSignControl;

public class DemoBaseCase extends WebDriverBaseCase {
	
	DemoSignControl login = new DemoSignControl();

	@BeforeClass(alwaysRun = true, timeOut=50000)
	@Parameters({ "userName", "passWord" })
	public void userLogin(String userName, String passWord){
		login.userLogin(userName, passWord);
	}
	
	@AfterClass(alwaysRun = true, timeOut=10000)
	public void userLogout() {
		login.userLogout();
	}
}