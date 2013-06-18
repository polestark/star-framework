package com.star.logging.webdriver;

import java.util.Map;
import java.util.logging.Logger;

public class HTMLLogWriter {
	private final String className;
	private final String LOG_ABS;
	private final String charSet;
	private static HTMLFormatter htmlWritter;
	private static Logger logger;

	/**
	 * @param clsName the class name to be logged.
	 * @param path the path where the log file to be located.
	 * @param charSet the file charSet of log files.
	 */
	public HTMLLogWriter(String clsName, String path, String charSet) {
		this.className = clsName;
		this.LOG_ABS = path;
		this.charSet = charSet;
	}

	/**
	 * Description: initialize the Logger instance.
	 * 
	 * @param startTime the time of test begins, by unit of millisecond.
	 */
	public void logInit(long startTime) {
		logger = Logger.getLogger(className);
		htmlWritter = new HTMLFormatter(LOG_ABS + className + ".html");
		htmlWritter.setEncoding(charSet);
		htmlWritter.init(className, startTime);
	}

	/**
	 * Description: appned log info line be line.
	 * 
	 * @param logMap the log info hashmap.
	 */
	public void logWrite(Map<String, String> logMap) {
		logger.info(logMap.get("trace_info"));
		htmlWritter.write(logMap);// HTML写入
	}

	/**
	 * Description: write tail of log file and close file output stream.
	 * 
	 * @param endTime the time of test ends, by unit of millisecond.
	 */
	public void logDestory(long endTime) {
		htmlWritter.changeTime(endTime);
		htmlWritter.destory();
	}
}
