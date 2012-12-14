package com.star.frame.basecase;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import com.star.support.externs.Win32GuiByVbs;
import com.star.testdata.string.StringBufferUtils;
import com.star.toolapi.webdriver.WebDriverWebPublic;

public class WebDriverBaseCase extends WebDriverWebPublic {

    private final Win32GuiByVbs vbs = new Win32GuiByVbs();
    private final String className = this.getClass().getName();
    private final StringBufferUtils string = new StringBufferUtils();
    private long starts = 0;
       
    /**
    * test initialize: start selenium-server, create log bufferwriter
    *
     * @throws RuntimeException
    **/
    @BeforeTest(alwaysRun = true, timeOut=60000)
    public void testSetup(){
    	String begins = string.formatedTime("yyyy-MM-dd HH-mm-ss-SSS");
        starts = System.currentTimeMillis();
        System.out.println("==============" + begins + "：案例【" + className + "】开始==============");
        vbs.killWin32Process("werfault");
        vbs.killWin32Process("IEDriverServer");
        testCunstruction(className);
        tableRefresh();
    }

    /**
    * test clear: stop selenium,close log bufferwriter, stop selenium-server.
    *
     * @throws RuntimeException
    **/
    @AfterTest(alwaysRun = true, timeOut=60000)
    public void tearDown(){
        testTermination();
        vbs.killWin32Process("iexplore");
        vbs.killWin32Process("werfault");
        vbs.killWin32Process("IEDriverServer");
        String ends = string.formatedTime("yyyy-MM-dd HH-mm-ss-SSS");
        long stops = System.currentTimeMillis();
        System.out.println("==============" + ends + "：案例【" + className + "】结束==============");
        System.out.println("==============本次运行消耗时间 " + (stops - starts)/1000 + " 秒！==============");
    }
}