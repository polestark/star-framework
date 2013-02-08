package com.star.logging.webdriver;

/**
 * 说明：
 * 1、加载java.util.logging生成的XML格式的日志文件；
 * 2、将xml文件内容根据用户选择配置的key列表读取存入List；
 * 3、解析List，根据用户的期望做一系列转换之后变为html格式的文本；
 * 4、根据html格式的文本生成html文件，最后可选择是否继续保留xml源文件；
 * 5、输出html文件的编码格式可由用户自定义指定；
 * 6、用户配置的key列表必须和html表第一行（表头）关键字个数配置一致，否则格式混乱。
 * 
 * @author 测试仔刘毅
**/

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HTMLLogTransfer {

	private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final LoggingManager LOG = new LoggingManager(HTMLLogTransfer.class.getName());
	private static final ParseProperties property = new ParseProperties("config/config.properties");
	private static final String LOG_MARK = new File(property.get("log")).getName();
	private static final String CAM_LOG = "screenshot is:";
	private static final String CAM_LINK_TEXT = "【点击查看场景截图】";

	private static String xmlName = null;
	private static String nodesName = null;
	private static String htmlName = null;
	private static String[] nodeName = null;
	private static Document document;
	private static String htmls = null;

	private static final String HTML_HEADER = "<html><head><style type=\"text/css\">body, " 
		+ "table{font-family: Verdana, Arial, sans-serif;font-size: 12;}</style><title>Test Results</title></head>";
	private static final String HTML_FOOTER = "</table></body></html>\n";
	private static final String HTML_START_WARN = "<tr style='background:#FFC000'><td align=center><p><span>\n";
	private static final String HTML_START_FAIL = "<tr style='background:#FF5050'><td align=center><p><span>\n";
	private static final String HTML_START_PASS = "<tr><td align=center><p><span>\n";
	private static final String HTML_MID = "</span></p></td><td style=\"word-break:break-all;\"><p><span>\n";
	private static final String HTML_END = "</span></p></td style=\"word-break:break-all;\"></tr>\n";
	private static final String KEEP_XML_LOGFILE = property.get("KEEP_XML_LOGFILE");;

	/**
	 * construct with parameter intialize and check.
	 * 
	 * @param fileName the whole path and name of the xml log file
	 * @param nodesList the key names you want to record into html logs
	 * @throws IllegalArgumentException
	 **/
	public HTMLLogTransfer(String fileName, String nodesList) {
		if (nodesList == null) {
			throw new IllegalArgumentException("parameter can not be null!");
		} else {
			HTMLLogTransfer.nodesName = nodesList;
			if (nodesName.contains(";")) {
				HTMLLogTransfer.nodeName = nodesName.split(";");
			} else if (nodesName.contains(",")) {
				HTMLLogTransfer.nodeName = nodesName.split(",");
			} else {
				LOG.error("parameter must be separate by ',' or ';'!");
				throw new IllegalArgumentException("parameter must be separate by ',' or ';'!");
			}
		}
		HTMLLogTransfer.xmlName = fileName;
		HTMLLogTransfer.htmlName = new File(fileName).getAbsolutePath().replace(".xml", ".html");
	}

	/**
	 * get html file content, and write to the new html file. if needed, delete
	 * xml log file finally.
	 * 
	 * @param startTime the milliseconds when you start your test
	 * @param endTime the milliseconds when you stop your test
	 **/
	public void xmlTansToHtml(long startTime, long endTime) {
		String htmls = htmlBodyHead(startTime, endTime);

		writeHtml(startTime, endTime, htmls);

		if (!Boolean.parseBoolean(KEEP_XML_LOGFILE)) {
			new File(xmlName).delete();
		}
	}

	/**
	 * read the xml log file into list, choosen by your nodes select.
	 * 
	 * @param startTime the milliseconds when you start your test
	 * @param endTime the milliseconds when you stop your test
	 * @return xml file content in list, choosen by your nodes select
	 * @throws RuntimeException
	 **/
	public List<String> xmlParse(long startTime, long endTime) {

		document = loadDocument();
		List<String> arrayList = new ArrayList<String>();
		long lastMillis = startTime;
		long tempMillis = startTime;

		Node firstFNode = document.getElementsByTagName(nodeName[0]).item(0).getParentNode();
		NodeList fatherList = document.getElementsByTagName(firstFNode.getNodeName());

		for (int i = 0; i < fatherList.getLength(); i++) {
			Node fatherNodes = fatherList.item(i);
			NodeList childList = fatherNodes.getChildNodes();
			int nNum = childList.getLength();
			for (int j = 0; j < nNum; j++) {
				Node childNode = childList.item(j);
				String cnodeName = childNode.getNodeName();
				String cnodeValue = childNode.getTextContent();
				for (int k = 0; k < nodeName.length; k++) {
					if (cnodeName != null && !cnodeName.contains("#text") && cnodeName.equals(nodeName[k])) {
						if (cnodeName.contains("date")) {
							cnodeValue = cnodeValue.replace("T", " ");
						} else if (cnodeName.contains("millis")) {
							lastMillis = Long.parseLong(cnodeValue);
							cnodeValue = String.valueOf(Long.parseLong(cnodeValue) - tempMillis) + " ms";
							tempMillis = lastMillis;
						}
						cnodeValue = cnodeValue.replace("&lt;", "&");
						cnodeValue = cnodeValue.replace("&amp;", "<");
						cnodeValue = cnodeValue.replace("&gt;", ">");
						cnodeValue = cnodeValue.replace("&apos;", "'");
						cnodeValue = cnodeValue.replace("&quot;", "\"");
						arrayList.add(cnodeValue);
					}
				}
			}
		}
		return arrayList;
	}

	/**
	 * load your xml log file, parse and return document object.
	 * 
	 * @return the document of your xml file, loaded as domfactory
	 * @throws RuntimeException
	 **/
	private Document loadDocument() {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setValidating(false);
			domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			return builder.parse(xmlName);
		} catch (Exception e) {
			LOG.error(e, "XML parse failed:");
			throw new RuntimeException("XML parse failed:" + e.getMessage());
		}
	}

	/**
	 * get html file content title and table.
	 * 
	 * @param startTime the milliseconds when you start your test
	 * @param endTime the milliseconds when you stop your test
	 * @return the first part html body
	 * @throws RuntimeException
	 **/
	private String htmlBodyHead(long startTime, long endTime) {

		String className = new File(xmlName).getName().replace(".xml", "");
		String startedTime = DFORMAT.format(startTime);
		String finishedTime = DFORMAT.format(endTime);
		long duration = endTime - startTime;
		String totalExpensed = String.valueOf(duration) + "ms ≈ " + (duration) / 1000 + "s ≈ "
				+ (int) (duration / 60000) + "m " + (duration / 1000) % 60 + "s";
		String htmlBody = "<body> \n"
				+ "		<span>Logging for Star Tests, Formatted to HTML, Contributed By PAICDOM\\LIUYI027.</span> \n"
				+ "		<h1><span>Test Results</span></h1> \n"
				+ "		<table border=1 cellspacing=0 cellpadding=1 width=100% style='background:#D4ECD7'> \n"
				+ "		 <tr><td width=12%><p><b><span>class-name</span></b></p></td><td><p><span>{0}</span></p></td></tr> \n"
				+ "		 <tr><td width=12%><p><b><span>start-time</span></b></p></td><td><p><span>{1}</span></p></td></tr> \n"
				+ "		 <tr><td width=12%><p><b><span>end-time</span></b></p></td><td><p><span>{2}</span></p></td></tr> \n"
				+ "		 <tr><td width=12%><p><b><span>duration</span></b></p></td><td><p><span>{3}</span></p></td></tr>  \n"
				+ "		</table> \n	<p><span></span></p> \n"
				+ "		<table border=1 cellspacing=0 cellpadding=2 width=100% style='background:#D4ECD7'> \n	<tr> \n"
				+ "				<td align=center width=12%><p><b><span>time</span></b></p></td> \n"
				+ "				<td align=center width=6%><p><b><span>duration</span></b></p></td> \n"
				+ "				<td align=center width=15%><p><b><span>method</span></b></p></td> \n"
				+ "				<td align=center width=5%><p><b><span>status</span></b></p></td> \n"
				+ "				<td align=center width=38%><p><b><span>message</span></b></p></td> \n "
				+ "				<td align=center width=24%><p><b><span>class</span></b></p></td> \n </tr> \n";
		return MessageFormat.format(htmlBody, new Object[] { className, startedTime, finishedTime, totalExpensed });
	}

	/**
	 * write loop to append formatted html to new log file.
	 * 
	 * @param startTime the milliseconds when you start your test
	 * @param endTime the milliseconds when you stop your test
	 * @throws RuntimeException
	 **/
	private void writeHtml(long startTime, long endTime, String htmlBody) {
		List<?> content = xmlParse(startTime, endTime);

		String params = null;
		int arraySize = nodeName.length;
		int loopNum = content.size() / arraySize;

		try {
			OutputStreamWriter outWriter = getOutputWriter(new File(htmlName), "gbk");
			outWriter.write(HTML_HEADER);
			outWriter.append(htmlBody);

			for (int i = 0; i < loopNum; i++) {
				htmls = HTML_START_PASS;
				for (int k = 0; k < arraySize; k++) {
					params = content.get(arraySize * i + k).toString();
					if (params.contains("fail")) {
						htmls = HTML_START_FAIL;
					} else if (params.contains("warn")) {
						htmls = HTML_START_WARN;
					}
				}
				for (int j = 0; j < arraySize; j++) {
					params = content.get(arraySize * i + j).toString();
					int hasFile = 0;
					if (params.indexOf("\\" + LOG_MARK + "\\") > 0){
						hasFile = params.indexOf("\\" + LOG_MARK + "\\");
					}else if(params.indexOf("/" + LOG_MARK + "/") > 0){
						hasFile = params.indexOf("/" + LOG_MARK + "/");						
					}

					String camera = (hasFile > 0) ? "<a href=\""
							+ params.substring(hasFile + LOG_MARK.length() + 2).replace("]", "")
							+ "\">" + CAM_LINK_TEXT + "</a>\n" : null;
					String finalHt = (camera == null) ? params : params.substring(0,
							params.indexOf(CAM_LOG) + CAM_LOG.length()) + " " + camera;
					htmls = htmls + finalHt + ((j == (arraySize - 1)) ? HTML_END : HTML_MID);
				}
				outWriter.append(htmls);
			}
			outWriter.append(HTML_FOOTER);
			outWriter.close();
		} catch (Exception e) {
			LOG.error(e, "create HTML log file failed:");
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * create and return OutputStreamWriter.
	 * 
	 * @param outputFile File to write
	 * @param encode the encode of the output file
	 * @return OutputStreamWriter
	 * @throws RuntimeException
	 **/
	private OutputStreamWriter getOutputWriter(File outputFile, String encode) {
		try {
			return new OutputStreamWriter(new FileOutputStream(outputFile), encode);
		} catch (Exception e) {
			LOG.error(e, "create HTML log file writer failed:");
			throw new RuntimeException("create HTML log file writer failed:" + e.getMessage());
		}
	}
}