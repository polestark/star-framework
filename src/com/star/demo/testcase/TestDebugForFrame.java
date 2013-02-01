package com.star.demo.testcase;

import org.junit.Test;

import com.star.support.externs.Win32GuiByAu3;

public class TestDebugForFrame {

	@Test
	public void frameWork(){
		String test = "D:\\AAA\\asd\\ABC.png";
		new Win32GuiByAu3().screenCapture(test);
	}
}
