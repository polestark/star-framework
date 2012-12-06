package com.star.frame.executor;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeoutException;

public class ThreadExecutor{
	private long timeOut = 30000;

	/**
	 * set process execute timeout 
	 * @param time timeout of milliseconds.
	 */
	public void setTimeOut(long time){
		this.timeOut = time;
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
	        	listener.join(timeOut);
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
	        	listener.join(timeOut);
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
	
	private static class ProcessListener extends Thread {
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
				exit = process.waitFor();
				reader.join();
			} catch (InterruptedException ignore) {
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class StreamReader extends Thread {
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
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}