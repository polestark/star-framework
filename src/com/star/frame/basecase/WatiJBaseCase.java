package com.star.frame.basecase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.star.support.externs.Win32GuiByVbs;
import com.star.toolapi.watij.IEWebTestByWatiJ;

public class WatiJBaseCase extends IEWebTestByWatiJ {
	
	private final Win32GuiByVbs vbs = new Win32GuiByVbs();
	private final String className = this.getClass().getName();

	/**
	 * start new watij ie test.
	 * 
	 * @throws	RuntimeException
	 **/
	@BeforeTest(alwaysRun = true, timeOut=30000)
	public void testSetup(){
		System.out.println("==============当前测试案例【" + className + "】运行开始==============");
		vbs.killWin32Process("iexplore");
		vbs.killWin32Process("IEDriverServer");
		testCunstruction();
	}

	/**
	 * close watij ie test.
	 * 
	 * @throws	RuntimeException
	 **/
	@AfterTest(alwaysRun = true, timeOut=30000)
	public void tearDown(){
		testTermination();
		vbs.killWin32Process("IEDriverServer");
		vbs.killWin32Process("iexplore");
		System.out.println("==============当前测试案例【" + className + "】运行结束==============");
	}
}