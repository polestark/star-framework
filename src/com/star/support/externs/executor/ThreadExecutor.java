package com.star.support.externs.executor;

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
			listener.threadClear();
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
			listener.threadClear();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
}