package com.star.tools;

import java.util.HashMap;
import java.util.Map;

public class StackTraceUtils {

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
	public int getTraceMethodLevel(StackTraceElement[] trace) {
		for (int i = trace.length - 1; i > 0; i--) {
			if (trace[i].getMethodName().equals("invoke0")) {
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
	public Map<String, String> logRecord(StackTraceElement[] traces, String status, String message) {
		Map<String, String> logMap = new HashMap<String, String>();

		int level = getTraceClassLevel(traces);
		StackTraceElement classTrace = traces[level];
		String mtdName = classTrace.getMethodName();
		String clsInfo = getClassTrace(classTrace);
		String traceInfo = message + " - " + mtdName + " - " + clsInfo;

		logMap.put("status", status);
		logMap.put("message", message);
		logMap.put("method", mtdName);
		logMap.put("class_name", clsInfo);
		logMap.put("trace_info", traceInfo);

		return logMap;
	}

	/**
	 * Description: get the class and execute line number.
	 * 
	 * @param trace the StackTraceElement.
	 * @return class name and running line number.
	 */
	public String getClassTrace(StackTraceElement trace) {
		return trace.getClassName() + " # " + trace.getLineNumber();
	}
}