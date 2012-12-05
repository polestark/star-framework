package com.star.support.externs.executor;

/**
 * JAVA执行EXE/BAT/SHELL/VBS等外部文件的时候需要通过线程监听执行进程的情况。</BR>
 * 执行进程需要waitFor的情况下，必须要先读出缓存的内容，否则容易hang死。
 * 
 * @author 测试仔刘毅
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.star.logging.frame.LoggingManager;

public class ProcessListener extends Thread {
	
	private final LoggingManager LOG = new LoggingManager(ProcessListener.class.getName());
	private Process proc;
	private boolean processOver;

	/**
	 * construct with parameter sets. 
	 * @param process the process instance.
	 */
	public ProcessListener(Process process) {
		this.proc = process;
		processOver = false;
	}

	/**
	 * do read inputstream before process.waitfor(). keep threads never hangs up.
	 */
	public void run() {
		try {
			if (proc == null) {
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while (proc != null && !processOver) {
				reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * do set bool for thread control
	 */
	public void setOver(boolean over) {
		this.processOver = over;
	}
	
	/**
	 * do process distroy after use.
	 */
	public void processClear(){
		this.proc.destroy();
	}
}