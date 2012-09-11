package com.star.externs;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertTrue;
import static watij.finders.SymbolFactory.*;
import com.star.frame.basecase.WatiJBaseCase;

public class TestWatiJDemo extends WatiJBaseCase{
	
	@Test
	public void testBaidu() throws Exception{
        ie.focus();
		ie.start("http://www.baidu.com");
		ie.maximize();
        ie.textField(id, "kw").set("刘毅");
        ie.button(id, "su").click();
        assertTrue(ie.containsText("找到相关结果约"));
	}
}