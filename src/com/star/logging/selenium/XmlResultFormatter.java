package com.star.logging.selenium;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XmlResultFormatter implements ResultsFormatter {
	static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"%s\"?>\n";
	static final String ROOT_ELEMENT = "testResult";
	static final SimpleDateFormat FILENAME_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	private Writer resultsWriter;
	private String resultFileEncoding = "ISO-8859-1";
	private XStream xstream;
	private String automaticScreenshotPath = ".";

	private String screenShotBaseUri = "";

	public XmlResultFormatter(Writer myResultsWriter) {
		this.resultsWriter = myResultsWriter;
		this.xstream = new XStream();
		this.xstream.alias("loggingBean", LoggingBean.class);
		this.xstream.alias("testMetricsBean", TestMetricsBean.class);
	}

	public XmlResultFormatter(Writer myResultsWriter, String myResultFileEncoding) {
		this(myResultsWriter);
		this.resultFileEncoding = myResultFileEncoding;
	}

	public void booleanCommandLogEvent(LoggingBean loggingBean) {
	}

	public void commandLogEvent(LoggingBean loggingBean) {
	}

	public void commentLogEvent(LoggingBean loggingBean) {
	}

	public void methodLogEvent(LoggingBean loggingBean) {
		this.xstream.toXML(loggingBean, this.resultsWriter);
	}

	public void footerLogEvent() {
		logToWriter(String.format("</%s>\n", new Object[] { "testResult" }));
	}

	public String generateFilenameForAutomaticScreenshot(String baseName) {
		String constWaitTimeoutScreenshotFileName = "automatic" + baseName + "Screenshot"
				+ timeStampForFileName() + ".png";
		return this.automaticScreenshotPath + File.separator + constWaitTimeoutScreenshotFileName;
	}

	String timeStampForFileName() {
		Date currentDateTime = new Date(System.currentTimeMillis());
		return FILENAME_DATETIME_FORMAT.format(currentDateTime);
	}

	public String getAutomaticScreenshotPath() {
		return this.automaticScreenshotPath;
	}

	public String getScreenShotBaseUri() {
		return this.screenShotBaseUri;
	}

	public void headerLogEvent(TestMetricsBean metricsBean) {
		logToWriter(String.format("<?xml version=\"1.0\" encoding=\"%s\"?>\n",
				new Object[] { this.resultFileEncoding }));
		logToWriter(String.format("<%s>\n", new Object[] { "testResult" }));
		this.xstream.toXML(metricsBean, this.resultsWriter);
		logToWriter("\n");
	}

	void logToWriter(String message) {
		try {
			this.resultsWriter.write(message);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setAutomaticScreenshotPath(String automaticScreenshotPath) {
		this.automaticScreenshotPath = new File(automaticScreenshotPath).getAbsolutePath();
	}

	public void setScreenShotBaseUri(String screenShotBaseUri) {
		this.screenShotBaseUri = (screenShotBaseUri == null ? "" : screenShotBaseUri);
	}
}