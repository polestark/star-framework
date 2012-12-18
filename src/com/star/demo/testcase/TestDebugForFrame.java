package com.star.demo.testcase;

import org.junit.Test;
import com.star.support.externs.Win32GuiByVbs;

public class TestDebugForFrame{
	
	Win32GuiByVbs exe = new Win32GuiByVbs();
	
	@Test
	public void frameWork() {
		exe.killWin32Process("iexplore");
	}
}
