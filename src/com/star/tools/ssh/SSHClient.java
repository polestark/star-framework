package com.star.tools.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
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
	}
	
	public void setbashTimeout(int timeout){
		//this._timeout = timeout;
	}
	
	public String executeBash(String command) {
		final StringBuilder sbuilder = new StringBuilder(1024);
		try {
			_session.requestPTY("vt100", 80, 24, 640, 480, null); 
			Thread.currentThread().join(5000);
			
			_session.execCommand(command);

			@SuppressWarnings("resource")
			InputStream stdOut = new StreamGobbler(_session.getStdout());
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] bytes = new byte[1024];
			
			int count;
			while ((count = stdOut.read(bytes)) != -1) {
				buffer.write(bytes, 0, count);
				if (count == 1024) {
					sbuilder.append(new String(bytes));
				} else {
					byte[] chars = new byte[count];
					for (int i = 0; i < count; i++) {
						chars[i] = bytes[i];
					}
					sbuilder.append(new String(chars));
				}
			}
			
			return sbuilder.toString();
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