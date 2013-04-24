package com.star.tools.ssh;

import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import com.star.logging.frame.LoggingManager;

public class ServiceManager{
	private Properties property = new Properties();
	private final LoggingManager LOG = new LoggingManager(ServiceManager.class.getName()); 
	private SSHClient ssh;
	
	private String hostName;
	private int port;
	private String userName;
	private String passWord;
	
	public ServiceManager(){
		try {
			property.load(this.getClass().getResourceAsStream("/com/star/tools/ssh/service_monitor.properties"));
			hostName = (String) property.get("MONITOR_HOST");
			port = Integer.parseInt((String)property.get("MONITOR_PORT"));
			userName = (String) property.get("MONITOR_USER");
			passWord = (String) property.get("MONITOR_PWD");
			
			ssh = new SSHClient(hostName, port, userName, passWord);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void getServiceStatus(){
		getServiceStatus("egis_pos_stg");
	}
	
	public void getServiceStatus(String sysName){
		String monitor_res = null;
		String commands =  " && stg && sremotectl -s " + sysName + " -a monitor";
		try {
			ssh.createConnection();
			ssh.createSession();
			ssh.executeBash("su - " + userName);
			ssh.executeBash(passWord);
			monitor_res = ssh.executeBash(commands);
			System.out.println(monitor_res);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			ssh.closeSession();
			ssh.closeConnection();
		}
	}
}