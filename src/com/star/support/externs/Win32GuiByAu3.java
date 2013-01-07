package com.star.support.externs;

import com.star.frame.executor.ThreadExecutor;

/**
 * 说明：
 * 1、依赖AUTOIT实现的GUI操作，必须编译为可知性程序；
 * 2、含上载、下载、关闭Dialog、点击Dialog、在Dialog上输入文本等操作。
 * 
 * @author 测试仔刘毅
 **/

public class Win32GuiByAu3 {

	private static final String ASSIST = System.getProperty("user.dir") + "/assist/";
	private final ThreadExecutor execute = new ThreadExecutor();

	/**
	 * upload file in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: fileUpload("选择文件", "D:\\a.txt", 5);
	 * 
	 * @param	title dialog title
	 * @param	fileName file from where to upload
	 * @param	timeout timeout setting for wait alert appears in second unit
	 * @throws	RuntimeException
	 **/
	public void fileUpload(String title, String fileName, int timeout){
		String fileExec = ASSIST + "Upload.exe";
		String cmd = "\"" + fileExec + "\" \"" + title + "\" \"" + fileName + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
		closeWindow(title, 1);
	}
	
	/**
	 * upload file in win32 gui using autoit compiled exe</BR>
	 * wait for upload dialog appers in 20 seconds</BR>
	 * you can use it like this: fileUpload("选择文件", "D:\\a.txt");
	 * 
	 * @param	title dialog title
	 * @param	fileName file from where to upload
	 * @throws	RuntimeException
	 **/
	public void fileUpload(String title, String fileName){
		fileUpload(title, fileName, 20);
	}

	/**
	 * download file in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: fileDownload("文件下载", "另存为", "D:\\download.csv", 30);
	 * 
	 * @param	fstTitle the first download dialog title
	 * @param	sndTitle the second dialog after click "Save"
	 * @param	saveAs file name to be saved as
	 * @param	timeout download window wait timeout setting
	 * @throws	RuntimeException
	 **/
	public void fileDownload(String fstTitle, String sndTitle, String saveAs, int timeout){
		String fileExec = ASSIST + "Download.exe";
		String cmd = "\"" + fileExec + "\" \"" + fstTitle + "\" \"" 
					+ sndTitle + "\" \"" + saveAs + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
		closeWindow(fstTitle, 1);
		closeWindow(sndTitle, 1);
	}

	/**
	 * download file in win32 gui using autoit compiled exe</BR>
	 * wait for download dialog appers in 20 seconds</BR>
	 * you can use it like this: fileDownload("文件下载", "另存为", "D:\\download.csv");
	 * 
	 * @param	fstTitle the first download dialog title
	 * @param	sndTitle the second dialog after click "Save"
	 * @param	saveAs file name to be saved as
	 * @throws	RuntimeException
	 **/
	public void fileDownload(String fstTitle, String sndTitle, String saveAs){
		fileDownload(fstTitle, sndTitle, saveAs, 20);
	}

	/**
	 * click alert dialog in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: clickAlert("提示信息", "确定(Y)", 10);
	 * 
	 * @param	dialogTitle dialog title
	 * @param	buttonName the button name/text on the dialog to click
	 * @param	timeout timeout setting for wait alert appears in second unit
	 * @throws	RuntimeException
	 **/
	public void clickAlert(String dialogTitle, String buttonName, int timeout){
		String fileExec = ASSIST + "ClickAlert.exe";
		String cmd = "\"" + fileExec + "\" \"" + dialogTitle + "\" \""
				+ buttonName + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
		closeWindow(dialogTitle, 1);
	}

	/**
	 * click alert dialog in win32 gui using autoit compiled exe</BR>
	 * wait for alert appers in 5 seconds</BR>
	 * you can use it like this: clickAlert("提示信息", "确定(Y)");
	 * 
	 * @param	dialogTitle dialog title
	 * @param	buttonName the button name/text on the dialog to click
	 * @throws	RuntimeException
	 **/
	public void clickAlert(String dialogTitle, String buttonName){
		clickAlert(dialogTitle, buttonName, 5);
	}

	/**
	 * type text in alert dialog in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: typeAlert("窗口标题", "Edit1", "输入内容", 5);
	 * 
	 * @param	title init dialog title
	 * @param	locator the locator on the dialog
	 * @param	text text to put into the edit
	 * @param	timeout timeout setting for wait alert appears in second unit
	 * @throws	RuntimeException
	 **/
	public void typeAlert(String title, String locator, String text, int timeout){
		String fileExec = ASSIST + "TypeAlert.exe";
		String cmd = "\"" + fileExec + "\" \"" + title + "\" \"" 
					+ locator + "\" \"" + text + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
		closeWindow(title, 1);
	}

	/**
	 * type text in alert dialog in win32 gui using autoit compiled exe</BR>
	 * wait for alert appers in 5 seconds</BR>
	 * you can use it like this: typeAlert("窗口标题", "Edit1", "输入内容");
	 * 
	 * @param	title init dialog title
	 * @param	locator the locator on the dialog
	 * @param	text text to put into the edit
	 * @throws	RuntimeException
	 **/
	public void typeAlert(String title, String locator, String text){
		typeAlert(title, locator, text, 5);
	}

	/**
	 * close window by name in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: closeWindow("窗口标题", 5);
	 * 
	 * @param	title dialog title
	 * @param	timeout timeout setting for wait alert appears in second unit
	 * @throws	RuntimeException
	 **/
	public void closeWindow(String title, int timeout){
		String fileExec = ASSIST + "CloseWindow.exe";
		String cmd = "\"" + fileExec + "\" \"" + title + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
	}

	/**
	 * close window by name in win32 gui using autoit compiled exe</BR>
	 * wait for alert appers in 5 seconds</BR>
	 * you can use it like this: closeWindow("窗口标题");
	 * 
	 * @param	title dialog title
	 * @throws	RuntimeException
	 **/
	public void closeWindow(String title){
		closeWindow(title, 5);
	}

	/**
	 * activate window by name in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: activateWindow("窗口标题", 5);
	 * 
	 * @param	title dialog title
	 * @param	timeout timeout setting for wait alert appears in second unit
	 * @throws	RuntimeException
	 **/
	public void activateWindow(String title, int timeout){
		String fileExec = ASSIST + "ActivateWindow.exe";
		String cmd = "\"" + fileExec + "\" \"" + title + "\" \"" + timeout + "\"";
		execute.executeCommands(cmd);
	}

	/**
	 * activate window by name in win32 gui using autoit compiled exe</BR>
	 * you can use it like this: activateWindow("窗口标题");
	 * 
	 * @param	title dialog title
	 * @throws	RuntimeException
	 **/
	public void activateWindow(String title){
		activateWindow(title, 5);
	}

	/**
	 * get and write browser message to file using autoit compiled exe</BR>
	 * you can use it like this: assertErrors(title, "errorMessage", "li", "D:\\a.txt", 10);
	 * 
	 * @param	title pop window title
	 * @param	upIdName the id or name of the text's father element
	 * @param	eleType the type of the element which to get text
	 * @param	fileName file path and name to store the error text
	 * @param	timeout time out setting to find the pop window
	 * @throws	RuntimeException
	 **/
	public void assertErrors(String title, String upIdName, String eleType, String fileName, long timeout){
		String execName = ASSIST + "WriteErrorMessage.exe";
		String cmd = "\"" + execName + "\" \"" + title + "\" \"" + upIdName + "\" \"" 
				+ eleType + "\" \""	+ fileName + "\" \"" + String.valueOf(timeout) + "\"";
		execute.executeCommands(cmd);
		closeWindow(title, 1);
	}
}