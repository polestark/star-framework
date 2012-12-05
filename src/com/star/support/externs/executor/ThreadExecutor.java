package com.star.support.externs.executor;

/**
 * execute exe/bat/shell/vbs string or file by java.</BR>
 * JAVA执行EXE、BAT、SHELL、VBS等外部文件.
 * 
 * @author 测试仔刘毅
 */

import com.star.logging.frame.LoggingManager;

public class ThreadExecutor{

	private static final LoggingManager LOG = new LoggingManager(ThreadExecutor.class.getName());
	
	/**
	 * execute exe/bat/shell string or file by java.
	 * 
	 * @param	command command to be executed
	 * @throws	RuntimeException
	 **/
	public void executeCommands(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			ProcessListener listener = new ProcessListener(process);
			listener.start();
			process.waitFor();
			listener.setOver(true);
			listener.processClear();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * execute vbs by java.
	 * 
	 * @param	command vbs content to be executed
	 * @throws	RuntimeException
	 **/
	public void executeCommands(String[] command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			ProcessListener listener = new ProcessListener(process);
			listener.start();
			process.waitFor();
			listener.setOver(true);
			listener.processClear();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
}