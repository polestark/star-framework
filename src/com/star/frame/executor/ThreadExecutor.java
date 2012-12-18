package com.star.frame.executor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class ThreadExecutor{
	private long threadTimeOut = 60000;
	private long readTimeOut = 20000;

	/**
	 * set process execute timeout 
	 * @param timeOut timeout of milliseconds.
	 */
	public void setThreadTimeOut(long timeOut){
		this.threadTimeOut = timeOut;
	}

	/**
	 * set process buffer read timeout 
	 * @param timeOut timeout of milliseconds.
	 */
	public void setReadTimeOut(long timeOut){
		this.readTimeOut = timeOut;
	}
	
	/**
	 * execute exe/bat/shell string or file by java.
	 * 
	 * @param	command command to be executed
	 * @throws	RuntimeException
	 * @throws	TimeoutException
	 */
	public int executeCommands(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			ProcessListener listener = new ProcessListener(process);
			listener.start();
			
	        try {
	        	listener.join(threadTimeOut);
	            if (listener.exit != null){
	                return listener.exit;
	            } else{
	                throw new TimeoutException();
	            }
	        } catch (InterruptedException ex) {
	        	listener.interrupt();
	            Thread.currentThread().interrupt();
				throw new RuntimeException(ex);
	        } finally {
	            process.destroy();
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * execute vbs by java.
	 * 
	 * @param	command vbs file with params Object to be executed
	 * @throws	RuntimeException
	 * @throws	TimeoutException
	 */
	public int executeCommands(String[] command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			ProcessListener listener = new ProcessListener(process);
			listener.start();
			
	        try {
	        	listener.join(threadTimeOut);
	            if (listener.exit != null){
	                return listener.exit;
	            } else{
	                throw new TimeoutException();
	            }
	        } catch (InterruptedException ex) {
	        	listener.interrupt();
	            Thread.currentThread().interrupt();
				throw new RuntimeException(ex);
	        } finally {
	            process.destroy();
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private class ProcessListener extends Thread {
		private Process process = null;
		public Integer exit = null;

		/**
		 * construct with parameter sets. 
		 * @param process the process instance.
		 */
		public ProcessListener(Process process) {
			this.process = process;
		}

		/**
		 * read inputstream before process.waitfor(). keep threads never hangs up.
		 */
		@Override
		public void run() {
			try {
				StreamReader reader = new StreamReader(process.getInputStream());
				reader.start();
				reader.join(readTimeOut);
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private class StreamReader extends Thread {
		private InputStream input;

		/**
		 * construct with parameter sets. 
		 * @param input the InputStream instance.
		 */
		public StreamReader(InputStream input) {
			this.input = input;
		}

		/**
		 * read inputstream and do buffer output write.
		 */
		@Override
		public void run() {
			try {
				InputStreamReader isReader = new InputStreamReader(input, "GBK");
				BufferedReader bfRader = new BufferedReader(isReader);
				while(bfRader.readLine() != null){
				}
				bfRader.close();
				isReader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}