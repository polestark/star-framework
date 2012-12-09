package com.star.frame.executor;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeoutException;

public class ThreadExecutor{
	private long threadTimeOut = 30000;
	private long readTimeOut = 10000;

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
		private StringWriter writer;

		/**
		 * construct with parameter sets. 
		 * @param input the InputStream instance.
		 */
		public StreamReader(InputStream input) {
			this.input = input;
			writer = new StringWriter();
		}

		/**
		 * read inputstream and do buffer output write.
		 */
		@Override
		public void run() {
			try {
				ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
				int buffer = -1;
				while ((buffer = input.read()) != -1) {
					baoStream.write(buffer);
				}
				writer.write(new String(baoStream.toString("GBK").getBytes("UTF-8")));
				writer.close();
				baoStream.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}