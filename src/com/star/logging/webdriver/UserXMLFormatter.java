package com.star.logging.webdriver;

import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import com.star.testdata.string.StringBufferUtils;

public class UserXMLFormatter extends Formatter{
	
	private String seperateMark = "~";
	
	public UserXMLFormatter(String seperateMark){
		this.seperateMark = seperateMark;
	}
	
	/**
	 * user defined format to append xml log files
	 * 
	 * @param record currnet thread info
	 **/
	public String format(LogRecord record) {
		String[] msgContent = record.getMessage().split(seperateMark);
		StringBuffer sb = new StringBuffer(500);
		String message = msgContent[3];

		// XML特殊字符的处理
		message = message.replace("&", "&amp;");
		message = message.replace("<", "&lt;");
		message = message.replace(">", "&gt;");
		message = message.replace("'", "&apos;");
		message = message.replace("\"", "&quot;");

		sb.append("<record>\n");
		sb.append("  <date>");// log current time
		sb.append(new StringBufferUtils().formatedTime("HH:mm:ss.SSS"));
		sb.append("</date>\n");
		sb.append("  <millis>");// log current milliseconds
		sb.append(record.getMillis());
		sb.append("</millis>\n");
		sb.append("  <method>");// log current method
		sb.append(msgContent[1]);
		sb.append("</method>\n");
		sb.append("  <status>");// log current run status
		sb.append(msgContent[2]);
		sb.append("</status>\n");
		sb.append("  <message>");// log current message details
		sb.append(message);
		sb.append("</message>");
		sb.append("\n");
		sb.append("  <class>");// log current running classname
		sb.append(msgContent[0]);
		sb.append("</class>\n");

		ResourceBundle bundle = record.getResourceBundle();
		try {
			if (bundle != null && bundle.getString(record.getMessage()) != null) {
				sb.append("  <key>");
				sb.append(record.getMessage());
				sb.append("</key>\n");
				sb.append("  <catalog>");
				sb.append(record.getResourceBundleName());
				sb.append("</catalog>\n");
			}
		} catch (Exception ex) {
		}

		Object parameters[] = record.getParameters();
		if (parameters != null && parameters.length != 0 && record.getMessage().indexOf("{") == -1) {
			for (int i = 0; i < parameters.length; i++) {
				sb.append("  <param>");
				try {
					sb.append(parameters[i].toString());
				} catch (Exception ex) {
					sb.append("???");
				}
				sb.append("</param>\n");
			}
		}

		if (record.getThrown() != null) {
			Throwable th = record.getThrown();
			sb.append("  <exception>\n");
			sb.append("    <message>");
			sb.append(th.toString());
			sb.append("</message>\n");
			StackTraceElement trace[] = th.getStackTrace();
			for (int i = 0; i < trace.length; i++) {
				StackTraceElement frame = trace[i];
				sb.append("    <frame>\n");
				sb.append("      <class>");
				sb.append(frame.getClassName());
				sb.append("</class>\n");
				sb.append("      <method>");
				sb.append(frame.getMethodName());
				sb.append("</method>\n");
				if (frame.getLineNumber() >= 0) {
					sb.append("      <line>");
					sb.append(frame.getLineNumber());
					sb.append("</line>\n");
				}
				sb.append("    </frame>\n");
			}
			sb.append("  </exception>\n");
		}
		sb.append("</record>\n");
		return sb.toString();
	}

	/**
	 * create xml file head
	 * 
	 * @param h the logger file handler
	 **/
	public String getHead(Handler h) {
		StringBuffer sb = new StringBuffer();
		String encoding;
		sb.append("<?xml version=\"1.0\"");
		if (h != null) {
			encoding = h.getEncoding();
		} else {
			encoding = null;
		}
		if (encoding == null) {
			encoding = java.nio.charset.Charset.defaultCharset().name();
		}
		try {
			Charset cs = Charset.forName(encoding);
			encoding = cs.name();
		} catch (Exception ex) {
		}
		sb.append(" encoding=\"");
		sb.append(encoding);
		sb.append("\"");
		sb.append(" standalone=\"no\"?>\n");
		sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">\n");
		sb.append("<log>\n");
		return sb.toString();
	}

	/**
	 * create xml file tail
	 * 
	 * @param h the logger file handler
	 **/
	public String getTail(Handler h) {
		return "</log>\n";
	}
}