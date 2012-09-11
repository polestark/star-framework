package com.star.toolapi.selenium;

/**
 * 框架说明：
 * 1、每个TestClass自启动SeleniumServer，使用RemoteControlConfiguration配置其各项参数；
 * 2、使用LoggingSelenium，LoggingSelenium和LoggingDefaultSelenium继承用户自定义扩展的
 * 	  ExtendSelenium和ExtendDefaultSelenium，ExtendSelenium和ExtendDefaultSelenium继承
 * 	  Selenium和DefaultSelenium；
 * 3、测试初始化和销毁使用TestNG的BeforeTest(alwaysRun=true)形式来确保其始终执行的，建
 * 	     议不直接使用JUnit，更不能在测试代码中加入可能导致JVM crash的事物，否则日志记录将不完整。
 * 4、LoggingSelenium启动之前会创建BufferedWriter，在TestClass停止的时候务必要关闭它。
 * 
 * @author 测试仔刘毅
 **/

import java.io.File;
import java.io.BufferedWriter;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.RemoteControlConfiguration;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.star.logging.frame.LoggingManager;
import com.star.logging.selenium.HtmlResultFormatter;
import com.star.logging.selenium.LoggingCommandProcessor;
import com.star.logging.selenium.LoggingDefaultSelenium;
import com.star.logging.selenium.LoggingSelenium;
import com.star.logging.selenium.LoggingUtils;
import com.star.logging.selenium.ResultsFormatter;
import com.star.support.config.ParseProperties;
import com.star.testdata.string.StringBufferUtils;

public class SeleniumController {

	protected static LoggingSelenium selenium = null;
	protected static SeleniumServer server = null;

	protected static final StringBufferUtils SBF = new StringBufferUtils();
	protected static final RemoteControlConfiguration RCC = new RemoteControlConfiguration();
	protected static final ParseProperties PROPERTY = new ParseProperties("config/config.properties");
	protected static final String ROOT_DIR = System.getProperty("user.dir");
	protected static final String LOG_DIR = ROOT_DIR + "/log/";
	
	private static BufferedWriter bufferWriter;
	private static ResultsFormatter lresFormatter;
	private static HttpCommandProcessor hcmdProcessor;
	private static LoggingCommandProcessor lcmdProcessor;
	private static final LoggingManager LOG = new LoggingManager(SeleniumController.class.getName());
	protected static final String ENCODING = "UTF-8";
	protected static final String SERVERIP = PROPERTY.get("serverAddr");
	protected static final String RBROWSER = PROPERTY.get("runBrowser");
	protected static final String SETUPURL = PROPERTY.get("setUpAddr");
	protected static final String SERVPORT = PROPERTY.get("serverPort");
	protected static final String CLOSETXT = PROPERTY.get("closeTextLog");

	/***************************************************************************************
	 * create LoggingSelenium instance.
	 * 
	 * @param 	resName result file name to write logs
	 * @param 	port the server prot to start selenium servers
	 * @return 	LoggingSelenium instance
	 * @throws	RuntimeException
	 */
	protected LoggingSelenium getSelenium(String resName, String port) {
		LoggingDefaultSelenium lSelenium = null;
		if (!new File(LOG_DIR).exists()) {
			new File(LOG_DIR).mkdirs();
		}
		try {
			bufferWriter = LoggingUtils.createWriter(resName, ENCODING, true);
			lresFormatter = new HtmlResultFormatter(bufferWriter, ENCODING);
			lresFormatter.setScreenShotBaseUri("");
			lresFormatter.setAutomaticScreenshotPath(LOG_DIR);
			hcmdProcessor = new HttpCommandProcessor(SERVERIP, Integer.parseInt(port), RBROWSER, SETUPURL);
			lcmdProcessor = new LoggingCommandProcessor(hcmdProcessor, lresFormatter);
			lSelenium = new LoggingDefaultSelenium(lcmdProcessor);
		} catch (Exception e) {
			LOG.error(e, "can not create loggingselenium instance!");
			throw new RuntimeException("can not create loggingselenium instance:" + e.getMessage());
		}
		return lSelenium;
	}

	/***************************************************************************************
	 * choose a port to start the selenium server.
	 * 
	 * @param portList the list of port can be used as selenium servers
	 * @param className the testcase's class name running to use this server
	 * @throws	RuntimeException
	 */
	protected void startServer(String portList, String className) {
		String portStr[] = portList.split(";");
		for (int i = 0; i < portStr.length; i++) {
			try {
				RCC.setPort(Integer.parseInt(portStr[i]));
				RCC.setDebugMode(false);
				RCC.setSingleWindow(false);
				RCC.setEnsureCleanSession(true);
				RCC.setReuseBrowserSessions(false);
				if (!Boolean.parseBoolean(CLOSETXT)){
					RCC.setDontTouchLogging(false);
					RCC.setOutputEncoding("gbk");
					RCC.setBrowserSideLogEnabled(true);
					RCC.setLogOutFileName(distinctName(LOG_DIR, className, ".log"));					
				}
				RCC.setTrustAllSSLCertificates(true);
				server = new SeleniumServer(false, RCC);
				server.start();
				selenium = getSelenium(distinctName(LOG_DIR, className, ".html"), portStr[i]);
				break;
			} catch (Exception t) {
				pause(1000);
			}
		}
		if (server == null || selenium == null) {
			LOG.error("selenium server can not start!");
			throw new RuntimeException("selenium server can not start!");
		}
	}

	/**
	 * wait milli seconds.
	 * 
	 * @param	millis time to wait, in millisecond
	 */
	protected void pause(long millis) {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("thread sleep failed!");
		}
	}

	/**
	 * start selenium.
	 * 
	 * @throws	RuntimeException
	 */
	protected void startSelenium() {
		try {
			selenium.start();
			selenium.setBrowserLogLevel("error");
			selenium.useXpathLibrary("javascript-xpath");
			selenium.windowMaximize();
		} catch (Exception t) {
			LOG.error(t);
			throw new RuntimeException("selenium can not start:" + t.getMessage());
		}
	}

	/**
	 * close selenium opened browser session.
	 * 
	 * @throws	RuntimeException
	 */
	protected void closeSelenium() {
		if (hcmdProcessor != null) {
			hcmdProcessor.stop();
		}
	}

	/**
	 * stop selenium
	 * 
	 * @throws	SeleniumException
	 */
	protected void stopSelenium() {
		if (selenium != null) {
			selenium.stop();
		}
	}

	/**
	 * stop the selenium server
	 * 
	 * @throws	SeleniumException
	 */
	protected void stopServer() {
		if (server != null) {
			server.stop();
		}
	}


	/**
	 * test initialize: start selenium-server, create log bufferwriter
	 * 
	 * @throws	RuntimeException
	 */
	public void testConstruction(String testClassName){
		startServer(SERVPORT, testClassName);
	}

	/**
	 * test clear: stop selenium,close log bufferwriter, stop selenium-server.
	 * 
	 * @throws	RuntimeException
	 */
	public void testTermination() {
		closeSelenium();
		stopSelenium();
		stopServer();
		try {
			if (bufferWriter != null) {
				bufferWriter.close();
			}
		} catch (Exception t) {
			LOG.error(t);
			throw new RuntimeException("can not start clsoe bufferWriter:" + t.getMessage());
		}
	}

	/***************************************************************************************
	 * choose a port to start the selenium server.
	 * 
	 * @param dir file location
	 * @param fileName file name to judge
	 * @param fileType file type such as ".html"
	 * @return if file exists then add mark by time
	 */
	private String distinctName(String dir, String fileName, String fileType) {
		String resultName = dir + fileName + fileType;
		if (new File(resultName).exists()) {
			resultName = dir + fileName + SBF.formatedTime("-yyyyMMdd-HHmmssSSS") + fileType;
		}
		return resultName;
	}
}