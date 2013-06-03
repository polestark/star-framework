package com.star.tools.ssh2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.star.logging.frame.LoggingManager;

public class SSHClient{
	private String _hostName;
	private String _userName;
	private String _passWord;
	private int _port;

	private Session _session;
	private Connection _connection;
	private final LoggingManager LOG = new LoggingManager(SSHClient.class.getName());
	
	public SSHClient(String hostName, int port, String userName, String passWord){
		this._hostName = hostName;
		this._port = port;
		this._userName = userName;
		this._passWord = passWord;
	}
	
	public void createConnection()  throws IOException{
		String error = "can not logon to " + _hostName + ":" + _port 
				+ " using:" + _userName + "/ " + _passWord + "!";
		_connection = new Connection(_hostName, _port);
		_connection.connect();
				
		if (!_connection.authenticateWithPassword(_userName, _passWord)){
			throw new RuntimeException(error);
		}
	}
	
	public void createSession() throws IOException{
		_session = _connection.openSession();
		_session.requestPTY("vt100", 80, 24, 640, 480, null); 
	}
	
	public String executeBash(String command, String[] params) {
		String result = null;
		try {
			_session.execCommand(command);
			
			byte[] buffer = new byte[10240];
			int length = 0;
			InputStream output = _session.getStdout();
			OutputStream input = _session.getStdin();

            Thread.currentThread().join(500);
			length = output.read(buffer);
            if (length > 0) {  
                String out = new String(buffer, 0, length);  
                System.out.println(out);
            }

            Thread.currentThread().join(500);
			for (int i = 0; i < params.length; i ++){
				String param = params[i];
				input.write((param + "\n").getBytes());
				
				if (param.contains("monitor")){
		            Thread.currentThread().join(60000);
				}else{
		            Thread.currentThread().join(500);
				}
				
				length = output.read(buffer);
	            if (length > 0) {  
	            	result = new String(buffer, 0, length, "gbk");
	            }
			}			
			return result;
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public void closeSession(){
		if (null != _session){
			_session.close();
		}
	}
	
	public void closeConnection(){
		if (null != _connection){
			_connection.close();
		}		
	}
}