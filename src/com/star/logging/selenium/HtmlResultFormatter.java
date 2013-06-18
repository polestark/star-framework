package com.star.logging.selenium;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

public class HtmlResultFormatter implements ResultsFormatter {
	String resultFileEncoding = "ISO-8859-1";
	static final int HTML_MAX_COLUMNS = 7;
	static final int SCREENSHOT_PREVIEW_HEIGHT = 200;
	static final int SCREENSHOT_PREVIEW_WIDHT = 200;
	static final String URL_PATH_SEPARATOR = "/";
	static final String CSS_CLASS_FAILED = "status_failed";
	static final String CSS_CLASS_PASSED = "status_passed";
	static final String CSS_CLASS_UNKNOWN = "status_maybefailed";
	static final String CSS_CLASS_DONE = "status_done";
	static final String CSS_CLASS_TITLE = "title";
	static final String TOOL_TIPP_MESSAGE_TIME_DELTA = "time delta reporting is alpha and subject to change";
	static final SimpleDateFormat LOGGING_DATETIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat FILENAME_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

	String localFsPathSeparator = File.separator;
	String screenShotBaseUri = "";
	String automaticScreenshotPath = ".";

	Writer resultsWriter = null;
	static final String HTML_HEADER = "<html>\n<head><meta content=\"text/html; charset={0}\" http-equiv=\"content-type\"><meta content=\"cache-control\" http-equiv=\"no-cache\"><meta content=\"pragma\" http-equiv=\"no-cache\"><style type=\"text/css\">\nbody, table '{'\n    font-family: Verdana, Arial, sans-serif;\n    font-size: 12;\n'}'\n \nth, td '{'\n    padding-left: 0.3em;\n    padding-right: 0.3em;\n'}'\n\na '{'\n    text-decoration: none;\n'}'\n\n.title '{'\n    font-style: italic;\n'}'\n\n.selected '{'\n    background-color: #ffffcc;\n'}'\n\n.status_done '{'\n    background-color: #eeffee;\n'}'\n\n.status_passed '{'\n    background-color: #ccffcc;\n'}'\n\n.status_failed '{'\n    background-color: #ffcccc;\n'}'\n\n.status_maybefailed '{'\n    background-color: #ffffcc;\n'}'\n\n.breakpoint '{'\n    background-color: #cccccc;\n    border: 1px solid black;\n'}'\n</style>\n<title>Test results</title></head>\n<body>\n <span style=\"font-size:15px;font-family:arial,verdana,sans-serif;\">HTML-Formatted Test Logs, Based on LoggingSelenium-1.0.3, Modified By PAICDOM\\LIUYI027</span><h1>Test results </h1>";
	static final String HTML_TABLE_HEADER = "<tr><td><b>Selenium-Command</b></td><td><b>Parameter-1</b></td><td><b>Parameter-2</b></td><td><b>Res.RC</b></td><td><b>Res.Selenium</b></td><td><b>Time [ms]</b></td><td><b>Class-Linenumber</b></td></tr>\n";
	static final String HTML_METRICS = "<table border=1 cellspacing=0 cellpadding=2>\n<tr><td>test-started:</td><td>{0}</td></tr>\n<tr><td>test-finished:</td><td>{1}</td></tr>\n<tr><td>test-duration:</td><td>{2} ms</td></tr>\n<tr><td>commands-processed:</td><td>{3}</td></tr>\n<tr><td>verifications-processed:</td><td>{4}</td></tr>\n{5}\n</table>\n";
	static final String HTML_COMMENT = "<tr class=\"{0}\"><td colspan=\"{1}\">{2}</td></tr>\n";
	static final String HTML_FOOTER = "</tbody></table></body></html>";
	static final String HTML_SPECIAL = "<span style=\"font-size:9px;font-family:arial,verdana,sans-serif;\">{0}</span>";
	static final String HTML_SCREENSHOT_ROW = "<tr class=\"{0}\"><td colspan=\"{1}\" valign=\"center\" align=\"center\" halign=\"center\">{2}</td><td>{3}</td><td>{4}</td></tr>\n";
	static final String HTML_SCREENSHOT_IMG = "<a href=\"{0}\"><img src=\"{1}\" width=\"{2}\" height=\"{3}\" alt=\"Selenium Screenshot\" title=\"Selenium Screenshot\"/><br/>{4}</a>";
	static final String HTML_EMPTY_COLUMN = "<td>&nbsp;</td>";

	public HtmlResultFormatter(Writer myResultsWriter) {
		this.resultsWriter = myResultsWriter;
	}

	public HtmlResultFormatter(Writer myResultsWriter, String myResultFileEncoding) {
		this.resultsWriter = myResultsWriter;
		this.resultFileEncoding = myResultFileEncoding;
	}

	public void commentLogEvent(LoggingBean loggingBean) {
		String[] loggingBeanArgs = LoggingUtils.getCorrectedArgsArray(loggingBean, 2, "");
		String commentToBeLogged = loggingBeanArgs[0];
		String additionalInformation = loggingBeanArgs[1];
		logToWriter(MessageFormat.format("<tr class=\"{0}\"><td colspan=\"{1}\">{2}</td></tr>\n",
				new Object[] { "title", Integer.valueOf(7),
						commentToBeLogged + extraInformationLogEvent(additionalInformation) }));
	}

	String formatMetrics(TestMetricsBean metrics) {
		String failedCommandsRow = "";
		if (metrics.getFailedCommands() > 0L) {
			failedCommandsRow = "<tr class=\"status_failed\"><td>failed commands:</td><td>"
					+ metrics.getFailedCommands() + "</td></tr>\n";

			if (StringUtils.isNotBlank(metrics.getLastFailedCommandMessage())) {
				failedCommandsRow = failedCommandsRow + "<tr class=\"" + "status_failed"
						+ "\"><td>last failed message:</td><td>"
						+ metrics.getLastFailedCommandMessage() + "</td></tr>\n";
			} else {
				System.err.println("WARNING: NO LastFailedCommandMessage");
			}
		}
		return MessageFormat
				.format("<table border=1 cellspacing=0 cellpadding=2>\n<tr><td>test-started:</td><td>{0}</td></tr>\n<tr><td>test-finished:</td><td>{1}</td></tr>\n<tr><td>test-duration:</td><td>{2} ms</td></tr>\n<tr><td>commands-processed:</td><td>{3}</td></tr>\n<tr><td>verifications-processed:</td><td>{4}</td></tr>\n{5}\n</table>\n",
						new Object[] {
								LOGGING_DATETIME_FORMAT.format(Long.valueOf(metrics
										.getStartTimeStamp())),
								LOGGING_DATETIME_FORMAT.format(Long.valueOf(metrics
										.getEndTimeStamp())),
								Long.valueOf(metrics.getTestDuration()),
								Long.valueOf(metrics.getCommandsProcessed()),
								Long.valueOf(metrics.getVerificationsProcessed()),
								failedCommandsRow });
	}

	public void headerLogEvent(TestMetricsBean metrics) {
		logToWriter(formatHeader(metrics));
	}

	String formatHeader(TestMetricsBean metrics) {
		String header = MessageFormat
				.format("<html>\n<head><meta content=\"text/html; charset={0}\" http-equiv=\"content-type\"><meta content=\"cache-control\" http-equiv=\"no-cache\"><meta content=\"pragma\" http-equiv=\"no-cache\"><style type=\"text/css\">\nbody, table '{'\n    font-family: Verdana, Arial, sans-serif;\n    font-size: 12;\n'}' \n \nth, td '{'\n    padding-left: 0.3em;\n    padding-right: 0.3em;\n'}'\n\na '{'\n    text-decoration: none;\n'}'\n\n.title '{'\n    font-style: italic;\n'}'\n\n.selected '{'\n    background-color: #ffffcc;\n'}'\n\n.status_done '{'\n    background-color: #eeffee;\n'}'\n\n.status_passed '{'\n    background-color: #ccffcc;\n'}'\n\n.status_failed '{'\n    background-color: #ffcccc;\n'}'\n\n.status_maybefailed '{'\n    background-color: #ffffcc;\n'}'\n\n.breakpoint '{'\n    background-color: #cccccc;\n    border: 1px solid black;\n'}'\n</style>\n<title>Test results</title></head>\n<body>\n <span style=\"font-size:15px;font-family:arial,verdana,sans-serif;\">HTML-Formatted Test Logs, Based on LoggingSelenium-1.0.3, Modified By PAICDOM\\LIUYI027</span><h1>Test results </h1>",
						new Object[] { this.resultFileEncoding })
				+ "\n"
				+ formatMetrics(metrics)
				+ "<table border=1 cellspacing=0 cellpadding=2><tbody>"
				+ "<tr><td><b>Selenium-Command</b></td><td><b>Parameter-1</b></td><td><b>Parameter-2</b></td><td><b>Res.RC</b></td><td><b>Res.Selenium</b></td><td><b>Time [ms]</b></td><td><b>Class-Linenumber</b></td></tr>\n";

		return header;
	}

	public void footerLogEvent() {
		logToWriter("</tbody></table></body></html>");
	}

	String extraInformationLogEvent(String extraInformation) {
		String result = "";
		if (null != extraInformation) {
			result = MessageFormat
					.format("<span style=\"font-size:9px;font-family:arial,verdana,sans-serif;\">{0}</span>",
							new Object[] { extraInformation });
		}
		return result;
	}

	public void commandLogEvent(LoggingBean loggingBean) {
		if (!loggingBean.isExcludeFromLogging()) {
			String resultClass = loggingBean.isCommandSuccessful() ? "status_done"
					: "status_failed";
			if ("captureScreenshot".equals(loggingBean.getCommandName()))
				logToWriter(formatScreenshot(loggingBean, resultClass));
			else
				logToWriter(formatCommandAsHtml(loggingBean, resultClass, ""));
		}
	}

	public void booleanCommandLogEvent(LoggingBean loggingBean) {
		String toolTippMessage = "";
		String resultClass = "";
		if (loggingBean.isCommandSuccessful()) {
			resultClass = "status_passed";
		} else {
			resultClass = "status_maybefailed";
			toolTippMessage = "How this &quot;false&quot; result from Selenium is treated by the test cannot be determined here.";
		}

		logToWriter(formatCommandAsHtml(loggingBean, resultClass, toolTippMessage));
	}

	public String getScreenShotBaseUri() {
		return this.screenShotBaseUri;
	}

	public void setScreenShotBaseUri(String screenShotBaseUri) {
		this.screenShotBaseUri = (screenShotBaseUri == null ? "" : screenShotBaseUri);
	}

	public String generateFilenameForAutomaticScreenshot(String baseName) {
		String constWaitTimeoutScreenshotFileName = "automatic" + baseName + "Screenshot"
				+ timeStampForFileName() + ".png";
		return this.automaticScreenshotPath + this.localFsPathSeparator
				+ constWaitTimeoutScreenshotFileName;
	}

	public String getAutomaticScreenshotPath() {
		return this.automaticScreenshotPath;
	}

	public void setAutomaticScreenshotPath(String automaticScreenshotPath) {
		this.automaticScreenshotPath = new File(automaticScreenshotPath).getAbsolutePath();
	}

	String formatScreenshot(LoggingBean loggingBean, String resultClass) {
		return MessageFormat
				.format("<tr class=\"{0}\"><td colspan=\"{1}\" valign=\"center\" align=\"center\" halign=\"center\">{2}</td><td>{3}</td><td>{4}</td></tr>\n",
						new Object[] { resultClass, Integer.valueOf(5),
								formatScreenshotFileImgTag(loggingBean.getArgs()[0]),
								Long.valueOf(loggingBean.getDeltaMillis()),
								loggingBean.getCallingClass() });
	}

	String formatScreenshotFileImgTag(String absFsPathToScreenshot) {
		String screenshotPathNormalized = absFsPathToScreenshot.replace(this.localFsPathSeparator,
				"/");
		String screenShotName = screenshotPathNormalized.substring(screenshotPathNormalized
				.lastIndexOf("/") + "/".length());
		String screenshotRelativeUrl;
		if ("".equals(this.screenShotBaseUri))
			screenshotRelativeUrl = screenShotName;
		else {
			screenshotRelativeUrl = this.screenShotBaseUri + "/" + screenShotName;
		}

		return MessageFormat
				.format("<a href=\"{0}\"><img src=\"{1}\" width=\"{2}\" height=\"{3}\" alt=\"Selenium Screenshot\" title=\"Selenium Screenshot\"/><br/>{4}</a>",
						new Object[] { screenshotRelativeUrl, screenshotRelativeUrl,
								Integer.valueOf(200), Integer.valueOf(200), screenShotName });
	}

	String formatCommandAsHtml(LoggingBean loggingBean, String resultClass, String toolTippMessage) {
		StringBuilder htmlWrappedCommand = new StringBuilder();
		htmlWrappedCommand.append("<tr class=\"" + resultClass + "\" title=\"" + toolTippMessage
				+ "\" alt=\"" + toolTippMessage + "\">" + "<td>"
				+ quoteHtml(loggingBean.getCommandName()) + "</td>");

		int writtenColumns = 0;
		if (loggingBean.getArgs() != null) {
			for (int i = 0; i < loggingBean.getArgs().length; i++) {
				writtenColumns++;
				htmlWrappedCommand.append("<td>" + quoteHtml(loggingBean.getArgs()[i]) + "</td>");
			}
		}

		htmlWrappedCommand.append(generateEmptyColumns(7 - writtenColumns - 5));

		htmlWrappedCommand.append("<td>" + quoteHtml(loggingBean.getSrcResult()) + "</td><td>"
				+ quoteHtml(loggingBean.getSelResult()) + "</td><td title=\""
				+ "time delta reporting is alpha and subject to change" + "\" alt=\""
				+ "time delta reporting is alpha and subject to change" + "\">"
				+ loggingBean.getDeltaMillis() + "</td><td>" + loggingBean.getCallingClass()
				+ "</td></tr>\n");

		return htmlWrappedCommand.toString();
	}

	void logToWriter(String formattedLogEvent) {
		try {
			this.resultsWriter.write(formattedLogEvent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String generateEmptyColumns(int numColsToGenerate) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < numColsToGenerate; i++) {
			result.append("<td>&nbsp;</td>");
		}
		return result.toString();
	}

	public static final String timeStampForFileName() {
		Date currentDateTime = new Date(System.currentTimeMillis());
		return FILENAME_DATETIME_FORMAT.format(currentDateTime);
	}

	public void methodLogEvent(LoggingBean loggingBean) {
	}

	public static final String quoteHtml(String unquoted) {
		String quoted = unquoted == null ? "" : unquoted;
		quoted = quoted.replace("&", "&amp;");
		quoted = quoted.replace("<", "&lt;");
		quoted = quoted.replace(">", "&gt;");
		return quoted;
	}

	public static SimpleDateFormat getFILENAME_DATETIME_FORMAT() {
		return FILENAME_DATETIME_FORMAT;
	}

	public static void setFILENAME_DATETIME_FORMAT(SimpleDateFormat newFormat) {
		FILENAME_DATETIME_FORMAT = newFormat;
	}
}