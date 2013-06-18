package com.star.tools.ssh2;

import java.io.IOException;
import java.util.Properties;
import com.star.logging.frame.LoggingManager;

/**
 * 内容描述: 检查应用的服务状态，如果服务异常，则直接重启。</BR> 本方法仅在平安科技所辖系统有效，依赖基础架构已封装linux shell。
 * 
 * @author 测试仔刘毅
 */
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

	/**
	 * Description: check the application run status, if unhealthy then restart.
	 * 
	 * @param args the application instance name.
	 */
	public static void main(String[] args) {
		try {
			property.load(ServiceManager.class
					.getResourceAsStream("/com/star/tools/ssh2/service_monitor.properties"));
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

		if (!isApplicationHealthy(args[0])) {
			restartService(args[0]);
		}
	}

	/**
	 * Description: check if the application instances are running.
	 * 
	 * @param instanceName application instance name, such as egis_nbs_stg1_rebuild.
	 * @return if healthy return true, else false.
	 */
	public static boolean isApplicationHealthy(String instanceName) {
		String commands = "sremotectl -s " + instanceName + " -a monitor";
		String status = null;
		try {
			ssh.createConnection();
			ssh.createSession();
			status = ssh.executeBash("su - " + mUserName,
					new String[] { mPassWord, "stg", commands });

			if (status.contains("No such file or directory")) {
				throw new RuntimeException("该系统服务未正常搭建，请检查！");
			} else if (status.contains("not running")) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			ssh.closeSession();
			ssh.closeConnection();
		}
	}

	/**
	 * Description: restart the application service.
	 * 
	 * @param instanceName application instance name, such as egis_nbs_stg1_rebuild.
	 */
	public static void restartService(String instanceName) {
		String commands = "sremotectl -s " + instanceName + " -a restart paralell";
		try {
			ssh.createConnection();
			ssh.createSession();
			ssh.executeBash("su - " + mUserName, new String[] { mPassWord, commands });
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			ssh.closeSession();
			ssh.closeConnection();
		}
	}
}