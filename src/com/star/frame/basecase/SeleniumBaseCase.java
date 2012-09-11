package com.star.frame.basecase;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.star.toolapi.selenium.SeleniumWebPublic;

public class SeleniumBaseCase extends SeleniumWebPublic {

	/**
	 * test initialize: start selenium-server, create log bufferwriter
	 * 
	 * @author 	PAICDOM\LIUYI027
	 * @throws	RuntimeException
	 */
	@BeforeClass(alwaysRun = true)
	public void testSetup(){
		testConstruction(this.getClass().getName());
	}

	/**
	 * test clear: stop selenium,close log bufferwriter, stop selenium-server.
	 * 
	 * @author 	PAICDOM\LIUYI027
	 * @throws	RuntimeException
	 */
	@AfterClass(alwaysRun = true)
	public void tearDown(){
		testTermination();
	}
}