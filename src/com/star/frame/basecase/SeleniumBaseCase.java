package com.star.frame.basecase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import com.star.toolapi.selenium.SeleniumWebPublic;

public class SeleniumBaseCase extends SeleniumWebPublic {

	/**
	 * test initialize: start selenium-server, create log bufferwriter
	 * 
	 * @throws	RuntimeException
	 **/
	@BeforeTest(alwaysRun = true)
	public void testSetup(){
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
	}
}