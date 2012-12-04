package com.star.support.externs.executor;

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
	 **/
	public ProcessListener(Process process) {
		this.proc = process;
		processOver = false;
	}

	/**
	 * do read inputstream before process.waitfor(). keep threads never hangs up.
	 **/
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
	 **/
	public void setOver(boolean over) {
		this.processOver = over;
	}
	
	/**
	 * do process distroy after use.
	 **/
	public void threadClear(){
		this.proc.destroy();
	}
}