package com.star.support.externs;

import com.star.support.externs.Win32GuiByAu3;
import com.star.logging.frame.LoggingManager;

public class Win32GuiByAu3 {

	private static final String ASSIST = System.getProperty("user.dir") + "/assist/";
	private static final LoggingManager LOG = new LoggingManager(Win32GuiByAu3.class.getName());

	/**
	 * upload file in win32 gui using autoit compiled exe.
	 * 
	 * @param	title dialog title
	 * @param	filePath file from where to upload
	 * @param	system OS:win7/winxp
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void fileUpload(String title, String filePath, String system){
		String file = "";
		if (title.equals("选择要加载的文件")) {
			if (system.toUpperCase().equals("WIN7")) {
				file = "upload_win7.exe";
			}
			file = "Upload.exe";
		} else if (title.equals("选择文件")) {
			file = "upload_win7.exe";
		}
		String execute_file = ASSIST + file;
		String cmd = "\"" + execute_file + "\"" + " " + "\"" + title + "\"" + " " + "\"" + filePath + "\"";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.getErrorStream();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * download file in win32 gui using autoit compiled exe.
	 * 
	 * @param	downloadTitle init dialog title
	 * @param	saveTitle save file dialog title
	 * @param	saveFileName file name to save
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void fileDownload(String downloadTitle, String saveTitle, String saveFileName){
		String execute_file = ASSIST + "Download.exe";
		String cmd = "\"" + execute_file + "\"" + " " + "\"" + downloadTitle + "\"" + " " + "\"" + saveTitle
				+ "\"" + " " + "\"" + saveFileName + "\"";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.getErrorStream();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * click alert dialog in win32 gui using autoit compiled exe.
	 * 
	 * @param	dialogTitle dialog title
	 * @param	buttonName the button name on the dialog
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void clickAlert(String dialogTitle, String buttonName){
		String execute_file = ASSIST + "HandleAlert.exe";
		String command = "\"" + execute_file + "\"" + " " + "\"" + dialogTitle + "\"" + " " + "\""
				+ buttonName + "\"";
		try {
			Process proc = Runtime.getRuntime().exec(command);
			proc.getErrorStream();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * type text in alert dialog in win32 gui using autoit compiled exe.
	 * 
	 * @param	title init dialog title
	 * @param	locator the locator on the dialog
	 * @param	content text to put into the edit
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void typeAlert(String title, String locator, String content){
		String execute_file = ASSIST + "TyepAlert.exe";
		String cmd = "\"" + execute_file + "\"" + " " + "\"" + title + "\"" + " " + "\"" + locator + "\""
				+ " " + "\"" + content + "\"";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.getErrorStream();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * close window by name in win32 gui using autoit compiled exe.
	 * 
	 * @param	title dialog title
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void closeWindow(String title){
		String execute_file = ASSIST + "CloseWindow.exe";
		String cmd = "\"" + execute_file + "\"" + " " + "\"" + title + "\"";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * get and write browser message to file using autoit compiled exe.
	 * 
	 * @param	title pop window title
	 * @param	upIdName the id or name of the text's father element
	 * @param	eleType the type of the element which to get text
	 * @param	fileName file path and name to store the error text
	 * @param	timeout time out setting to find the pop window
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void assertErrors(String title, String upIdName, String eleType, String fileName, long timeout){
		String execName = ASSIST + "WriteErrorMessage.exe";
		String cmd = "\"" + execName + "\" \"" + title + "\" \"" + upIdName + "\" \"" + eleType + "\" \""
				+ fileName + "\" \"" + String.valueOf(timeout) + "\"";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}
}