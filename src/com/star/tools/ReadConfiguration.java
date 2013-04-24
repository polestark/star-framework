package com.star.tools;

/**
 * @author 测试仔刘毅
 */

import java.util.Properties;
import java.io.IOException;
import com.star.logging.frame.LoggingManager;

public class ReadConfiguration {

	private final Properties property = new Properties();
	private final LoggingManager LOG = new LoggingManager(ReadConfiguration.class.getName());

	/**
	 * construct with parameter intialize
	 * 
	 * @param fileName whole path and name of config file
	 * @throws RuntimeException, IllegalArgumentException
	 */
	public ReadConfiguration(String fileName) {		
		if (fileName == null){
			throw new IllegalArgumentException("the parameter can not be null!");
		}
		try {
			property.load(this.getClass().getResourceAsStream(fileName));
		} catch (IOException ioe) {
			LOG.error(ioe);
			throw new RuntimeException(ioe);
		}		
	}

	/**
	 * get specified key in config files
	 * @param	key the key name to get value
	 */
	public String get(String key) {
		String keyValue = null;
		if (property.containsKey(key)) {
			keyValue = (String) property.get(key);
		}
		return keyValue;
	}
}