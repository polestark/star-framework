package com.star.tools.ssh2;

import java.io.IOException;
import java.util.Properties;
import com.star.logging.frame.LoggingManager;

public class ServiceManager {
	private static Properties property = new Properties();
	private static final LoggingManager LOG = new LoggingManager(ServiceManager.class.getName());
	private static SSHClient ssh;

	private static int port;
	private static String hostName;
	private static String userName;
	private static String passWord;
	private static String mUserName;
	private static String mPassWord;

	public static void main(String[] args) {
		try {
			property.load(ServiceManager.class.getResourceAsStream("/com/star/tools/ssh2/service_monitor.properties"));
			hostName = (String) property.get("MONITOR_HOST");
			port = Integer.parseInt((String) property.get("MONITOR_PORT"));
			userName = (String) property.get("MONITOR_USER");
			passWord = (String) property.get("MONITOR_PWD");
			mUserName = (String) property.get("MANAGER_USER");
			mPassWord = (String) property.get("MANAGER_PWD");

			ssh = new SSHClient(hostName, port, userName, passWord);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		
		if (!isApplicationHealthy(args[0])){
			restartService(args[0]);
		}
	}

	public static boolean isApplicationHealthy(String instanceName) {
		String commands = "sremotectl -s " + instanceName + " -a monitor";
		String status = null;
		try {
			ssh.createConnection();
			ssh.createSession();
			status = ssh.executeBash("su - " + mUserName, new String[] { mPassWord, "stg", commands });
			
			if (status.contains("No such file or directory")) {
				throw new RuntimeException("该系统服务未正常搭建，请检查！");
			} else if (status.contains("not running")) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			ssh.closeSession();
			ssh.closeConnection();
		}
	}

	public static void restartService(String instanceName) {
		String commands = "sremotectl -s " + instanceName + " -a restart paralell";
		try {
			ssh.createConnection();
			ssh.createSession();
			ssh.executeBash("su - " + mUserName, new String[] { mPassWord, commands });
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			ssh.closeSession();
			ssh.closeConnection();
		}
	}
}