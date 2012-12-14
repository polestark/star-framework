package com.star.frame.basecase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.star.support.externs.Win32GuiByVbs;
import com.star.testdata.string.StringBufferUtils;
import com.star.toolapi.watij.IEWebTestByWatiJ;

public class WatiJBaseCase extends IEWebTestByWatiJ {
	
	private final Win32GuiByVbs vbs = new Win32GuiByVbs();
    private final String className = this.getClass().getName();
    private final StringBufferUtils string = new StringBufferUtils();
    private long starts = 0;

	/**
	 * start new watij ie test.
	 * 
	 * @throws	RuntimeException
	 **/
	@BeforeTest(alwaysRun = true, timeOut=30000)
	public void testSetup(){
    	String begins = string.formatedTime("yyyy-MM-dd HH-mm-ss-SSS");
        starts = System.currentTimeMillis();
        System.out.println("==============" + begins + "：案例【" + className + "】开始==============");
        vbs.killWin32Process("werfault");
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
        vbs.killWin32Process("iexplore");
        vbs.killWin32Process("werfault");
        String ends = string.formatedTime("yyyy-MM-dd HH-mm-ss-SSS");
        long stops = System.currentTimeMillis();
        System.out.println("==============" + ends + "：案例【" + className + "】结束==============");
        System.out.println("==============本次运行消耗时间 " + (stops - starts)/1000 + " 秒！==============");
	}
}