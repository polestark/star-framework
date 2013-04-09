package com.star.logging.webdriver;

import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.star.support.config.ParseProperties;

public class LoggingModeHelper {
	protected static final ParseProperties CONFIG = new ParseProperties("config/config.properties");
	protected static Handler handler;
	private final String devidor = "~";
	private final String className;
	private final String LOG_ABS;
	private final String charSet;
	private String logMode = CONFIG.get("LOG_MODE").toUpperCase();
	private static HTMLLogWritter htmlWritter;
	private static XMLLogWritter xmlWritter;
	private static Logger logger;
	
	/**
	 * @param clsName the class name to be logged.
	 * @param path the path where the log file to be located. 
	 * @param charSet the file charSet of log files.
	 */
	public LoggingModeHelper(String clsName, String path, String charSet) {
		this.className = clsName;
		this.LOG_ABS = path;
		this.charSet = charSet;
		if (null == logMode){
			logMode = "HTML";
		}
	}

	/**
	 * Description: initialize the Logger instance.
	 *
	 * @param startTime the time of test begins, by unit of millisecond.
	 */
	public void LogInit(long startTime) {
		try {
			if (logMode.contains("XML")) {
				logger = getLogger(className);
				xmlWritter = new XMLLogWritter(logger, devidor);
			} else {
				htmlWritter = new HTMLLogWritter(LOG_ABS + className + ".html");
				htmlWritter.setEncoding(charSet);
				htmlWritter.init(className, startTime);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Description: appned log info line be line.
	 *
	 * @param logMap the log info hashmap.
	 */
	public void LogWrite(Map<String, String> logMap) {
		if (logMode.contains("XML")) {
			xmlWritter.write(logMap);// XML写入
		} else {
			htmlWritter.write(logMap);// HTML写入
		}
	}

	/**
	 * Description: write tail of log file and close file output stream. 
	 *
	 * @param endTime the time of test ends, by unit of millisecond.
	 */
	public void LogDestory(long endTime) {
		if (logMode.contains("XML")) {
			if (handler != null) {
				handler.close();
			}
		} else {
			htmlWritter.changeTime(endTime);
			htmlWritter.destory();
		}
	}

	/**
	 * Description: create xml file log handle.
	 *
	 * @param clsName the class name to be logged.
	 * @return the Logger instance.
	 * @throws Exception
	 */
	private Logger getLogger(String clsName) throws Exception {
		Logger logger = Logger.getLogger(this.getClass().getName());
		XMLLogFormatter formatter = new XMLLogFormatter(devidor);
		handler = new FileHandler(LOG_ABS + clsName + ".xml", false);
		handler.setLevel(Level.FINE);
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		return logger;
	}
}
