package com.star.frame.basecase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import com.star.support.externs.Win32GuiByVbs;
import com.star.toolapi.webdriver.WebDriverWebPublic;

public class WebDriverBaseCase extends WebDriverWebPublic {
	
	private final Win32GuiByVbs vbs = new Win32GuiByVbs();
		
	/**
	 * test initialize: start selenium-server, create log bufferwriter
	 * 
	 * @throws	RuntimeException
	 **/
	@BeforeTest(alwaysRun = true)
	public void testSetup(){
		vbs.killWin32Process("iexplore");
		vbs.killWin32Process("IEDriverServer");
		
		testCunstruction(this.getClass().getName());
	}

	/**
	 * test clear: stop selenium,close log bufferwriter, stop selenium-server.
	 * 
	 * @throws	RuntimeException
	 **/
	@AfterTest(alwaysRun = true)
	public void tearDown(){
		testTermination();

		vbs.killWin32Process("iexplore");
		vbs.killWin32Process("IEDriverServer");
	}
}