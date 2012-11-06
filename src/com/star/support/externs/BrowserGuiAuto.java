package com.star.support.externs;

import com.star.logging.frame.LoggingManager;

 public class BrowserGuiAuto{

	private static final String ASSIST = System.getProperty("user.dir") + "/assist/";
	private static final LoggingManager LOG = new LoggingManager(Win32GuiByAu3.class.getName());

	/**
	 * click on browser object, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickById("窗口标题", 1, "idvalues", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	ieIndex the window index shared the same title, begins at 1
	 * @param	id id value of ie object
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickById(String ieTitle, int ieIndex, String id, int timeout) {
		String fileExec = ASSIST + "ieClickById.exe";
		String cmd = "\"" + fileExec + "\" \"" + ieTitle + "\" \"" 
					+ ieIndex + "\" \"" + id + "\" \"" + timeout + "\"";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute au3 exe files failed:" + e.getMessage());
		}
	}

	/**
	 * click on browser object, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickById("窗口标题", "idvalues", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	id id value of ie object
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickById(String ieTitle, String id, int timeout) {
		ieAU3ClickById(ieTitle, 1, id, timeout);
	}

	/**
	 * click on browser object, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickById("窗口标题", "idvalues");
	 * 
	 * @param	ieTitle ie window title
	 * @param	id id value of ie object
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickById(String ieTitle, String id) {
		ieAU3ClickById(ieTitle, id, 5);
	}

	/**
	 * click on browser object, found by name and index, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByName("窗口标题", 1, "namevalues", 0, 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	ieIndex the window index shared the same title, begins at 1
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByName(String ieTitle, int ieIndex, String name, int index, int timeout){
		String fileExec = ASSIST + "ieClickByName.exe";
		String cmd = "\"" + fileExec + "\" \"" + ieTitle + "\" \"" + ieIndex 
					+ "\" \"" + name + "\" \"" + index + "\" \"" + timeout + "\"";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute au3 exe files failed:" + e.getMessage());
		}
	}

	/**
	 * click on browser object, found by name and index, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByName("窗口标题", "namevalues", 0, 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByName(String ieTitle, String name, int index, int timeout){
		ieAU3ClickByName(ieTitle, 1, name, index, timeout);
	}

	/**
	 * click on browser object, found by name and index, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByName("窗口标题", "namevalues", 0);
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByName(String ieTitle, String name, int index){
		ieAU3ClickByName(ieTitle,name, index, 5);
	}

	/**
	 * click on browser object, found by name and index, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByName("窗口标题", "namevalues");
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByName(String ieTitle, String name){
		ieAU3ClickByName(ieTitle,name, 0);
	}

	/**
	 * click on browser object, found by linktext, default index 0, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByLinkText("窗口标题", 1, "link texts", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	ieIndex the window index shared the same title, begins at 1
	 * @param	linkText link text of ie links
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByLinkText(String ieTitle, int ieIndex, String linkText, int timeout) {
		String fileExec = ASSIST + "ieClickByLinkText.exe";
		String cmd = "\"" + fileExec + "\" \"" + ieTitle + "\" \"" 
					+ ieIndex + "\" \"" + linkText + "\" \"" + timeout + "\"";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute au3 exe files failed:" + e.getMessage());
		}
	}

	/**
	 * click on browser object, found by linktext, default index 0, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByLinkText("窗口标题", "link texts", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	linkText link text of ie links
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByLinkText(String ieTitle, String linkText, int timeout) {
		ieAU3ClickByLinkText(ieTitle, 1, linkText, timeout);
	}

	/**
	 * click on browser object, found by linktext, default index 0, using au3 libs</BR>
	 * you can use it like this: ieAU3ClickByLinkText("窗口标题", "link texts");
	 * 
	 * @param	ieTitle ie window title
	 * @param	linkText link text of ie links
	 * @throws	RuntimeException
	 **/
	public void ieAU3ClickByLinkText(String ieTitle, String linkText) {
		ieAU3ClickByLinkText(ieTitle, linkText, 5);
	}

	/**
	 * input text to editros, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysById("窗口标题", 1, "idvalues", "text content", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	ieIndex the window index shared the same title, begins at 1
	 * @param	id id value of ie object
	 * @param	text text content to be input to editors
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysById(String ieTitle, int ieIndex, String id, String text, int timeout) {
		String fileExec = ASSIST + "ieSendKeysById.exe";
		String cmd = "\"" + fileExec + "\" \"" + ieTitle + "\" \"" + ieIndex 
					+ "\" \"" + id + "\" \"" + text + "\" \"" + timeout + "\"";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute au3 exe files failed:" + e.getMessage());
		}
	}

	/**
	 * input text to editros, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysById("窗口标题", 1, "idvalues", "text content", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	id id value of ie object
	 * @param	text text content to be input to editors
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysById(String ieTitle, String id, String text, int timeout) {
		ieAU3SendKeysById(ieTitle, 1, id, text, timeout);
	}

	/**
	 * input text to editros, found by id, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysById("窗口标题", 1, "idvalues", "text content");
	 * 
	 * @param	ieTitle ie window title
	 * @param	id id value of ie object
	 * @param	text text content to be input to editors
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysById(String ieTitle, String id, String text) {
		ieAU3SendKeysById(ieTitle,id, text, 5);
	}

	/**
	 * input text to editros, found by name and its index, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysByName("窗口标题", 1, "name values", 0, "text content", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	ieIndex the window index shared the same title, begins at 1
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @param	text text content to be input to editors
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysByName(String ieTitle, int ieIndex, String name, int index, String text, int timeout) {
		String fileExec = ASSIST + "ieSendKeysByName.exe";
		String cmd = "\"" + fileExec + "\" \"" + ieTitle + "\" \"" + ieIndex + "\" \"" 
					+ name + "\" \"" + index + "\" \"" + text + "\" \"" + timeout + "\"";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute au3 exe files failed:" + e.getMessage());
		}
	}

	/**
	 * input text to editros, found by name and its index, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysByName("窗口标题", "name values", 0, "text content", 5);
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @param	text text content to be input to editors
	 * @param	timeout time to find ie window
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysByName(String ieTitle, String name, int index, String text, int timeout) {
		ieAU3SendKeysByName(ieTitle, 1, name, index, text, timeout);
	}

	/**
	 * input text to editros, found by name and its index, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysByName("窗口标题", "name values", 0, "text content");
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @param	index index of the object shared the same name, begins at 0
	 * @param	text text content to be input to editors
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysByName(String ieTitle, String name, int index, String text) {
		ieAU3SendKeysByName(ieTitle, name, index, text, 5);
	}

	/**
	 * input text to editros, found by name and its index, using au3 libs</BR>
	 * you can use it like this: ieAU3SendKeysByName("窗口标题", "name values", "text content");
	 * 
	 * @param	ieTitle ie window title
	 * @param	name name value of ie object
	 * @param	text text content to be input to editors
	 * @throws	RuntimeException
	 **/
	public void ieAU3SendKeysByName(String ieTitle, String name, String text) {
		ieAU3SendKeysByName(ieTitle, name, 0, text);
	}
 }