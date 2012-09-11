package com.star.frame.basecase;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.star.toolapi.watij.IEWebTestByWatiJ;

public class WatiJBaseCase extends IEWebTestByWatiJ {

	/**
	 * start new watij ie test.
	 * 
	 * @author 	PAICDOM\LIUYI027
	 * @throws		RuntimeException
	 */
	@BeforeClass(alwaysRun = true)
	public void testSetup(){
		testConstruct();
	}

	/**
	 * close watij ie test.
	 * 
	 * @author 	PAICDOM\LIUYI027
	 * @throws		RuntimeException
	 */
	@AfterClass(alwaysRun = true)
	public void tearDown(){
		testTerminate();
	}
}