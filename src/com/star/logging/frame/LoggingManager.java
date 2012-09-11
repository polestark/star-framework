package com.star.logging.frame;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggingManager {

	private String className;
	private Properties property = new Properties();
	private Logger logger = null;

	/**
	 * construct with set class name parameter.
	 * 
	 * @param	clsName	the name of your runtime class to be logged
	 * @throws	RuntimeException
	*/
	public LoggingManager(String clsName) {
		this.className = clsName;
	}

	/**
	 * record error info.
	 * 
	 * @param	t Throwable:Exceptions and Errors
	 * @param	userText user defined message to record
	 * @throws	RuntimeException
	*/
	public void error(Throwable t, String userText) {
		try {
			property.load(this.getClass().getResourceAsStream("/log4j.properties"));
			PropertyConfigurator.configure(property);
			logger = Logger.getLogger("message");
			logger.info("#################################################################");
			logger.error(className + ":" + userText, t);
			logger.info("#################################################################\n");
		} catch (Exception ie) {
			throw new RuntimeException("can not load log4j.properties:" + ie.getMessage());
		}
	}

	/**
	 * orverride the error method with default user text null.
	 * 
	 * @param	t Throwable:Exceptions and Errors
	*/
	public void error(Throwable t) {
		error(t, null);
	}

	/**
	 * orverride the error method.
	 * 
	 * @param	userText user defined message to record
	 * @throws	RuntimeException
	*/
	public void error(String text) {
		try {
			property.load(this.getClass().getResourceAsStream("/log4j.properties"));
			PropertyConfigurator.configure(property);
			logger = Logger.getLogger("message");
			logger.info("#################################################################");
			logger.info(className + ": ");
			logger.error(text);
			logger.info("#################################################################\n");
		} catch (Exception ie) {
			throw new RuntimeException("can not load log4j.properties:" + ie.getMessage());
		}
	}

	/**
	 * record user defined info message.
	 * 
	 * @param	text user defined message to record
	 * @throws	RuntimeException
	*/
	public void info(String text) {
		try {
			property.load(this.getClass().getResourceAsStream("/log4j.properties"));
			PropertyConfigurator.configure(property);
			logger = Logger.getLogger("message");
			logger.info(className + ": " + text + "\n");
		} catch (Exception ie) {
			throw new RuntimeException("can not load log4j.properties:" + ie.getMessage());
		}
	}
}