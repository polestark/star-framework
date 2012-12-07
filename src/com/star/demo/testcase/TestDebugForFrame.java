package com.star.demo.testcase;

import org.junit.Test;
import com.star.frame.executor.ThreadExecutor;

public class TestDebugForFrame{
	
	ThreadExecutor exe = new ThreadExecutor();
	
	@Test
	public void frameWork() {
		exe.executeCommands("C:\\Program Files\\Java\\jdk1.6.0_20\\bin\\javac.exe");
	}
}
