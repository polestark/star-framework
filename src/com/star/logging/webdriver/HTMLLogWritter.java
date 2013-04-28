package com.star.logging.webdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import com.star.logging.frame.LoggingManager;
import com.star.tools.ReadConfiguration;

public class HTMLLogWritter {
	private final LoggingManager LOG = new LoggingManager(HTMLLogWritter.class.getName());
	private final ReadConfiguration config = new ReadConfiguration(
			"/com/star/logging/webdriver/style.properties");
	private final ReadConfiguration config_tool = new ReadConfiguration(
			"/com/star/core/webdriver/webdirver_config.properties");

	private static File file;
	private OutputStreamWriter outwriter;
	private final SimpleDateFormat DFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final String CAPTURE_FORMAT = config_tool.get("CAPTURE_FORMAT");
	private final String CAPTURE_MESSAGE = config_tool.get("CAPTURE_MESSAGE");
	private final String HTML_HEADER = config.get("HTML_HEADER");
	private final String HTML_FOOTER = config.get("HTML_FOOTER");
	private final String HTML_START_WARN = config.get("HTML_START_WARN");
	private final String HTML_START_FAIL = config.get("HTML_START_FAIL");
	private final String HTML_START_PASS = config.get("HTML_START_PASS");
	private final String HTML_MID = config.get("HTML_MID");
	private final String HTML_END = config.get("HTML_END");
	private final String MESSAGE_HEAD = config.get("MESSAGE_HEAD");   
	private final String ERROR_MARK = "\">【点击查看场景截图】</a>";
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
		file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			LOG.error(e);
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
		String html = config.get("HTML_BODY");
		String htmlBody = MessageFormat.format(html, new Object[] { className, startedTime, "finishedTime",
				"totalExpensed" });
		fileWrite(HTML_HEADER + htmlBody, false);
	}

	/**
	 * Description: write html log info line by line.
	 *
	 * @param logMap the log info logMap.
	 */
	public void write(Map<String, String> logMap) {
		try {
			String time = DFORMAT.format(System.currentTimeMillis());
			String method = logMap.get("method");
			String status = logMap.get("status");
			String message = logMap.get("message");
			String className = logMap.get("classname");
			String html;
			String htmlStatus = HTML_START_PASS;
			if (status.equals("warn")) {
				htmlStatus = HTML_START_WARN;
			} else if (status.equals("failed")) {
				htmlStatus = HTML_START_FAIL;
				if (logMap.get("message").contains("." + CAPTURE_FORMAT)) {
					String []files = logMap.get("message").split(CAPTURE_MESSAGE, 2);
					String fileName = files[1].trim().replace("[", "").replace("]", "");
					fileName = new File(fileName).getName();
					message = files[0] + CAPTURE_MESSAGE + MESSAGE_HEAD + fileName + ERROR_MARK;
				}
			}
			html = htmlStatus + time + HTML_MID + method + HTML_MID + status + HTML_MID + message + HTML_MID
					+ className + HTML_END;
			fileWrite(html, true);
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
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
			LOG.error(e);
			throw new RuntimeException(e);
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
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
}
