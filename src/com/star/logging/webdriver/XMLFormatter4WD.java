package com.star.logging.webdriver;

/**
 * 说明：
 * 1、复制java.util.logging.XMLFormatter;
 * 2、改写，根据用户定义的key列表定义需要向日志中添加的内容。
 * 
 * @author 测试仔刘毅
**/

import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class XMLFormatter4WD extends Formatter {
	private void a2(StringBuffer sb, int x) {
		if (x < 10) {
			sb.append('0');
		}
		sb.append(x);
	}

	/**
	 * append string buffer use encode ISO8601.
	 * 
	 * @param sb
	 *            StringBuffers
	 * @param millis
	 *            the long type milliseconds
	 **/
	@SuppressWarnings("deprecation")
	private void appendISO8601(StringBuffer sb, long millis) {
		Date date = new Date(millis);
		sb.append(date.getYear() + 1900);
		sb.append('-');
		a2(sb, date.getMonth() + 1);
		sb.append('-');
		a2(sb, date.getDate());
		sb.append('T');
		a2(sb, date.getHours());
		sb.append(':');
		a2(sb, date.getMinutes());
		sb.append(':');
		a2(sb, date.getSeconds());
	}

	/**
	 * escape string text.
	 * 
	 * @param sb
	 *            StringBuffers
	 * @param text
	 *            text to be escaped
	 **/
	private void escape(StringBuffer sb, String text) {
		if (text == null) {
			text = "<null>";
		}
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == '<') {
				sb.append("<");
			} else if (ch == '>') {
				sb.append(">");
			} else if (ch == '&') {
				sb.append("&");
			} else {
				sb.append(ch);
			}
		}
	}

	/**
	 * user defined format to append xml log files
	 * 
	 * @param record
	 *            currnet thread info
	 **/
	public String format(LogRecord record) {
		String sMark = System.getProperty("SMARK");
		String[] msgContent = record.getMessage().split(sMark);
		StringBuffer sb = new StringBuffer(500);
		sb.append("<record>\n");

		sb.append("  <date>");
		appendISO8601(sb, record.getMillis());
		sb.append("</date>\n");

		sb.append("  <millis>");
		sb.append(record.getMillis());
		sb.append("</millis>\n");

		sb.append("  <method>");
		escape(sb, msgContent[1]);
		sb.append("</method>\n");

		sb.append("  <status>");
		escape(sb, msgContent[2]);
		sb.append("</status>\n");

		sb.append("  <message>");
		escape(sb, msgContent[3]);
		sb.append("</message>");
		sb.append("\n");

		sb.append("  <class>");
		escape(sb, msgContent[0]);
		sb.append("</class>\n");

		ResourceBundle bundle = record.getResourceBundle();
		try {
			if (bundle != null && bundle.getString(record.getMessage()) != null) {
				sb.append("  <key>");
				escape(sb, record.getMessage());
				sb.append("</key>\n");
				sb.append("  <catalog>");
				escape(sb, record.getResourceBundleName());
				sb.append("</catalog>\n");
			}
		} catch (Exception ex) {
		}

		Object parameters[] = record.getParameters();
		if (parameters != null && parameters.length != 0 && record.getMessage().indexOf("{") == -1) {
			for (int i = 0; i < parameters.length; i++) {
				sb.append("  <param>");
				try {
					escape(sb, parameters[i].toString());
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
			escape(sb, th.toString());
			sb.append("</message>\n");
			StackTraceElement trace[] = th.getStackTrace();
			for (int i = 0; i < trace.length; i++) {
				StackTraceElement frame = trace[i];
				sb.append("    <frame>\n");
				sb.append("      <class>");
				escape(sb, frame.getClassName());
				sb.append("</class>\n");
				sb.append("      <method>");
				escape(sb, frame.getMethodName());
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
	 * @param h
	 *            the logger file handler
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
	 * @param h
	 *            the logger file handler
	 **/
	public String getTail(Handler h) {
		return "</log>\n";
	}
}