package com.star.demo.testcase;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;
import static watij.finders.SymbolFactory.*;
import com.star.frame.basecase.WatiJBaseCase;

public class TestWatiJDemo extends WatiJBaseCase{
	
	@Test
	public void testBaidu() throws Exception{
		ie.start("http://www.baidu.com");
		ie.maximize();
        ie.focus();
        if (ie.alertDialog().exists()){
        	ie.alertDialog().ok();
        	ie.alertDialog().quit();
        	ie.fileDownloadDialog().save("C:\\a.txt");
        	int ieCount = ie.childBrowserCount();
        	ie.childBrowser(ieCount - 1).confirmDialog();
        	ie.frame(name, "test").frame(id, "watij").textField(name, "customerNo").set("测试");
        }
        ie.textField(id, "kw").set("刘毅");
        ie.button(id, "su").click();
        assertTrue(ie.containsText("找到相关结果约"));
	}
}