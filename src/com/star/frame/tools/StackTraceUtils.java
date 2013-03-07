package com.star.frame.tools;

import java.util.logging.Logger;

public class StackTraceUtils{
	private String devidor = "~";
	private Logger logger;
	
	public StackTraceUtils(Logger logger, String devidor){
		this.logger = logger;
		this.devidor = devidor;
	}

	/**
	 * get the trace level index of the running test class. 
	 * 
	 * @param trace the StackTraceElement array.
	 * @return the index of the trace deepth of class.
	 */
	public int getTraceClassLevel(StackTraceElement[] trace) {
		for (int i = trace.length - 1; i > 0; i--) {
			if (trace[i].getClassName().equals("sun.reflect.NativeMethodAccessorImpl")
					&& trace[i].getMethodName().equals("invoke0")) {
				return i - 1;
			}
		}
		return 0;
	}

	/**
	 * get the trace level index of the running test method. 
	 * 
	 * @param trace the StackTraceElement array.
	 * @return the index of the trace deepth of method.
	 */
	public int getTraceMethodLevel(StackTraceElement[] trace){
		for(int i = trace.length - 1; i > 0 ; i --){
			if (trace[i].getMethodName().equals("invoke0")){
				return i - 2;
			}
		}
		return 0;
	}
	
	/**
	 * Description: record log for listeners.
	 *
	 * @param traces the StackTraceElement.
	 * @param message the user defined message info.
	 */
	public void traceRecord(StackTraceElement[] traces, String status, String message){
		String methodName = null;
		for (int i = 0 ; i < traces.length; i ++){
			if (traces[i].getMethodName().equals("getStackTrace")){
				methodName = traces[i + 2].getMethodName();
				break;
			}
		}
		StackTraceElement classTrace = traces[getTraceClassLevel(traces)];
		logger.info(getClassTrace(classTrace) + devidor + methodName + devidor + status + devidor + message);
	}
	
	/**
	 * Description: get the class and execute line number.
	 *
	 * @param trace the StackTraceElement.
	 * @return class name and running line number.
	 */
	public String getClassTrace(StackTraceElement trace){
		return trace.getClassName() + " # " + trace.getLineNumber();
	}
}