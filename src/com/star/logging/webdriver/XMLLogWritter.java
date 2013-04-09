package com.star.logging.webdriver;

import java.util.Map;
import java.util.logging.Logger;

public class XMLLogWritter {
	private String devidor;
	private Logger logger;
	
	/**
	 * @param logger the Logger instance.
	 * @param devidor the string split mark.
	 */
	public XMLLogWritter(Logger logger, String devidor){
		this.logger = logger;
		this.devidor = devidor;
	}
	
	/**
	 * Description: write xml log file use append mode.
	 *
	 * @param map the log info map.
	 */
	public void write(Map<String, String> map){
		String method = map.get("method");
		String status = map.get("status");
		String message = map.get("message");
		String classname = map.get("classname");
		logger.info(classname + devidor + method + devidor + status + devidor + message);
	}
}
