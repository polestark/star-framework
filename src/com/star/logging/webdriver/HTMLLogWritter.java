package com.star.logging.webdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

public class HTMLLogWritter {
	
	private Properties property = new Properties();

	private static File file;
	private OutputStreamWriter outwriter;
	private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String HTML_HEADER;
	private String HTML_FOOTER;
	private String HTML_START_WARN;
	private String HTML_START_FAIL;
	private String HTML_START_PASS;
	private String HTML_MID;
	private String HTML_END;
	private String MESSAGE_HEAD;
	private String ERROR_MARK = "\">【点击查看场景截图】</a>";
	private long startTime;
	private String startedTime;
	private String finishedTime;
	private String charSet = "GBK";// 字符集默认为gbk

	/**
	 * Description: class construction with html head write.
	 * 
	 * @param fileName the log file name. 
	 */
	public HTMLLogWritter(String fileName) {
		try {
			property.load(this.getClass().getResourceAsStream("/com/star/logging/html/style.properties"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HTML_HEADER = (String) property.get("HTML_HEADER");           
		HTML_FOOTER = (String) property.get("HTML_FOOTER");           
		HTML_START_WARN = (String) property.get("HTML_START_WARN");   
		HTML_START_FAIL = (String) property.get("HTML_START_FAIL");   
		HTML_START_PASS = (String) property.get("HTML_START_PASS");   
		HTML_MID = (String) property.get("HTML_MID");                 
		HTML_END = (String) property.get("HTML_END");                 
		MESSAGE_HEAD = (String) property.get("MESSAGE_HEAD");                 
		file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Description: set encoding for log files.
	 *
	 * @param charSet the charset name.
	 */
	public void setEncoding(String charSet) {
		this.charSet = charSet;
	}

	/**
	 * Description: init the log files.
	 *
	 * @param className the class name to be logged.
	 * @param startTime the time when test starts.
	 */
	public void init(String className, long startTime) {
		this.startTime = startTime;
		startedTime = DFORMAT.format(startTime);
		String html = (String) property.get("HTML_BODY");
		String htmlBody = MessageFormat.format(html, new Object[] { className, startedTime, "finishedTime",
				"totalExpensed" });
		fileWrite(HTML_HEADER + htmlBody, false);
	}

	/**
	 * Description: write html log info line by line.
	 *
	 * @param map the log info map.
	 */
	public void write(Map<String, String> map) {
		try {
			String time = DFORMAT.format(System.currentTimeMillis());
			String method = map.get("method");
			String status = map.get("status");
			String message = map.get("message");
			String classname = map.get("classname");
			String html;
			String htmlstatus = HTML_START_PASS;
			if (status.equals("warn")) {
				htmlstatus = HTML_START_WARN;
			} else if (status.equals("failed")) {
				htmlstatus = HTML_START_FAIL;
				String messagehead = map.get("message").split(": ", 2)[0] + ": ";
				String messagebody = map.get("message").split("run failed, screenshot is:", 2)[1];
				int length = messagebody.length();
				message = messagehead + MESSAGE_HEAD + messagebody.substring(2, length - 1) + ERROR_MARK;
			}
			html = htmlstatus + time + HTML_MID + method + HTML_MID + status + HTML_MID + message + HTML_MID
					+ classname + HTML_END;
			fileWrite(html, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description: modify the end time at last.
	 *
	 * @param endTime the time when test stops.
	 */
	public void changeTime(long endTime) {
		try {
			String s = "";
			finishedTime = DFORMAT.format(endTime);
			long duration = endTime - startTime;
			String totalExpensed = String.valueOf(duration) + "ms ≈ " + (duration) / 1000 + "s ≈ "
					+ (int) (duration / 60000) + "m " + (duration / 1000) % 60 + "s";
			FileInputStream fis = new FileInputStream(file);
			byte[] b = new byte[1024];
			while (true) {
				int i = fis.read(b);
				if (i == -1) {
					break;
				}
				s = s + new String(b, 0, i, charSet);
			}
			fis.close();
			s = s.replace("finishedTime", finishedTime);
			s = s.replace("totalExpensed", totalExpensed);
			fileWrite(s, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description: write the tail of html log files.
	 *
	 */
	public void destory() {
		fileWrite(HTML_FOOTER, true);
	}

	/**
	 * Description: write html files.
	 *
	 * @param string the content to be put to log file.
	 * @param isAppend wether append mode used.
	 */
	private void fileWrite(String string, Boolean isAppend) {
		try {
			outwriter = new OutputStreamWriter(new FileOutputStream(file, isAppend), charSet);
			outwriter.write(string);
			outwriter.flush();
			outwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
