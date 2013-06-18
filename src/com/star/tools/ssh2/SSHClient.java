package com.star.tools.ssh2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.Connection;

public class SSHClient {
	private String _hostName;
	private String _userName;
	private String _passWord;
	private int _port;
	private long _waitFor = 500;

	private Session _session;
	private Connection _connection;

	/**
	 * @param hostName host name to be connected to.
	 * @param port the port number of the host.
	 * @param userName user name to login to the host.
	 * @param passWord password to login to the host.
	 */
	public SSHClient(String hostName, int port, String userName, String passWord) {
		this._hostName = hostName;
		this._port = port;
		this._userName = userName;
		this._passWord = passWord;
	}

	/**
	 * Description: set operation sleep interval for getStdout.
	 * 
	 * @param sleep milliseconds to sleep.
	 */
	public void setBashWait(long sleep) {
		this._waitFor = sleep;
	}

	/**
	 * Description: guess the bash usage by bash keyword and set bash sleep.
	 * 
	 * @param command the bash commands.
	 */
	public void setBashUsage(String command) {
		if (null == command || command.isEmpty()) {
			throw new IllegalArgumentException("command must not be empty!");
		} else if (command.contains("monitor")) {
			setBashWait(60000);
		} else if (command.contains("start") || command.contains("stop")) {
			setBashWait(600000);
		} else {
			setBashWait(500);
		}
	}

	/**
	 * Description: create connection to host.
	 * 
	 * @throws IOException
	 */
	public void createConnection() throws IOException {
		String error = "can not logon to " + _hostName + ":" + _port + " using:" + _userName + "/ "
				+ _passWord + "!";
		_connection = new Connection(_hostName, _port);
		_connection.connect();

		if (!_connection.authenticateWithPassword(_userName, _passWord)) {
			throw new RuntimeException(error);
		}
	}

	/**
	 * Description: create new session and open a remote crt after conneted to host.
	 * 
	 * @throws IOException
	 */
	public void createSession() throws IOException {
		_session = _connection.openSession();
		_session.requestPTY("vt100", 80, 24, 640, 480, null);
	}

	/**
	 * Description: execute commands on remote host.
	 * 
	 * @param command first command or bash to be exected.
	 * @param params other commands or bashes after first bash executed.
	 * @return bash execute result on ssh screen.
	 */
	public String executeBash(String command, String[] params) throws Exception {
		String result = null;
		_session.execCommand(command);

		byte[] buffer = new byte[10240];
		int length = 0;
		InputStream output = _session.getStdout();
		OutputStream input = _session.getStdin();

		for (int i = 0; i < params.length; i++) {
			String bashComm = params[i];
			input.write((bashComm + "\n").getBytes());

			setBashUsage(bashComm);
			Thread.currentThread().join(_waitFor);

			length = output.read(buffer);
			if (length > 0) {
				result = new String(buffer, 0, length, "gbk");
			}
		}
		return result;
	}

	/**
	 * Description: close session already opened.
	 */
	public void closeSession() {
		if (null != _session) {
			_session.close();
		}
	}

	/**
	 * Description: logout and close connection from host.
	 */
	public void closeConnection() {
		if (null != _connection) {
			_connection.close();
		}
	}
}