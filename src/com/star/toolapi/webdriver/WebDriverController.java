package com.star.toolapi.webdriver;

/**======================================================================================
 * 框架说明：
 * 1、使用RemoteWebDriver代替WebDriver，可直接使用JavascriptExecutor、TakesScreenshot，使用
 * 	  RemoteControlConfiguration配置SeleniumServer，截取HttpClient日志，便于测试开发调试；
 * 2、WebDriver启动时可选择浏览器，支持ie/ff/chrome/opera/safari/htmlunit模式，默认为ie；
 * 3、SeleniumServer启动时创建logger，记录操作日志，停止时可选择是否转换为html格式日志，同时，
 * 	     服务器的文本格式日志可根据配置项选择是否打开（建议不打开），xml格式的日志也可选择是否保留；
 * 4、测试初始化和结束销毁是使用TestNG的BeforeTest(alwaysRun=true)形式来确保其始终执行的，建
 * 	     议不直接使用JUnit，更不要在测试代码中加入可能导致JVM crash的事务，否则日志可能记录不完整。
 * 
 * @author 测试仔刘毅
 */

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.RemoteControlConfiguration;
import com.star.logging.frame.LoggingManager;
import com.star.logging.webdriver.HtmlFormatter4WD;
import com.star.logging.webdriver.XMLFormatter4WD;
import com.star.support.config.ParseProperties;
import com.star.testdata.string.StringBufferUtils;

public class WebDriverController {

	protected static RemoteWebDriver driver;
	protected static SeleniumServer server;
	protected static Handler handler;
	protected static Logger log4wd;

	protected static final StringBufferUtils STRUTIL = new StringBufferUtils();
	protected static final ParseProperties property = new ParseProperties("config/config.properties");
	protected static final String ROOT_DIR = System.getProperty("user.dir");
	protected static final String LOG_MARK = new File(property.get("log")).getName();
	protected static final String LOG_DIR = ROOT_DIR + "/" + LOG_MARK + "/";

	private static final LoggingManager LOG = new LoggingManager(WebDriverController.class.getName());
	private static final RemoteControlConfiguration RCC = new RemoteControlConfiguration();
	private static final String PORT = property.get("serverPort");
	private static final String CLOSETXT = property.get("closeTextLog");
	private static HtmlFormatter4WD html;
	private static String fName;
	private static long startTime;
	private static long endTime;

	/**======================================================================================
	 * choose a port to start the selenium server.
	 * 
	 * @param clsName
	 *            the runtime class name
	 * @throws RuntimeException
	 */
	protected void startServer(String clsName) {
		log4wd = xmlLogForWebRiver(clsName);
		String portStr[] = PORT.split(";");
		for (int i = 0; i < portStr.length; i++) {
			try {
				RCC.setPort(Integer.parseInt(portStr[i]));
				RCC.setDebugMode(false);
				RCC.setSingleWindow(false);
				RCC.setEnsureCleanSession(true);
				RCC.setReuseBrowserSessions(false);
				if (!Boolean.parseBoolean(CLOSETXT)) {
					RCC.setDontTouchLogging(false);
					RCC.setServerLogDebugMode(false);
					RCC.setBrowserSideLogEnabled(true);
					RCC.setOutputEncoding("gbk");
					RCC.setLogOutFileName(distinctName(LOG_DIR, clsName, ".log"));
				}
				RCC.setTrustAllSSLCertificates(true);
				server = new SeleniumServer(false, RCC);
				server.start();
				break;
			} catch (Throwable t) {
				if (i == (portStr.length - 1)) {
					LOG.error(t);
					throw new RuntimeException("selenium server can not start:" + t.getMessage());
				}
			}
		}
	}

	/**
	 * stop the selenium server
	 * 
	 * @throws RuntimeException
	 */
	protected void stopServer() {
		try {
			if (server != null) {
				server.stop();
			}
			if (handler != null) {
				handler.close();
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * start webdirver
	 * 
	 * @throws RuntimeException
	 */
	protected void startWebDriver(String browser) {
		DesiredCapabilities capability = null;
		if (browser.toLowerCase().contains("ie") || browser.toLowerCase().contains("internetexplorer")) {
			capability = DesiredCapabilities.internetExplorer();
		} else if (browser.toLowerCase().contains("ff") || browser.toLowerCase().contains("firefox")) {
			capability = DesiredCapabilities.firefox();
		} else if (browser.toLowerCase().contains("chrome")) {
			capability = DesiredCapabilities.chrome();
		} else if (browser.toLowerCase().contains("safari")) {
			capability = DesiredCapabilities.safari();
		} else if (browser.toLowerCase().contains("opera")) {
			capability = DesiredCapabilities.opera();
		} else if (browser.toLowerCase().contains("htmlunit")) {
			capability = DesiredCapabilities.htmlUnit();
		} else {
			throw new IllegalArgumentException("you are using wrong mode of browser paltform!");
		}
		try {
			URL url = new URL("http://localhost:" + server.getPort() + "/wd/hub");
			driver = new RemoteWebDriver(url, capability);
			pass("webdriver new session started");
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * start webdirver using browser iexplore
	 * 
	 * @throws RuntimeException
	 */
	protected void startWebDriver() {
		startWebDriver("ie");
	}

	/**
	 * closeWebDriver, close current session opened by webdriver.
	 * 
	 * @throws RuntimeException
	 */
	protected void closeWebDriver() {
		try {
			if (driver != null && driver.getWindowHandle() != null) {
				driver.close();
				pass("closed current webdriver session");
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * quitWebDriver, close webdriver instance and clear all sessions.
	 * 
	 * @throws RuntimeException
	 */
	protected void stopWebDriver() {
		try {
			if (driver != null && driver.getWindowHandle() != null) {
				driver.quit();
				pass("all webdriver session closed");
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**======================================================================================
	 * get a new distinct filename only if the file exists already
	 * 
	 * @param dir
	 *            file location
	 * @param fileName
	 *            file name to judge
	 * @param fileType
	 *            file type such as ".html"
	 * @return if file exists then add mark by time
	 * @throws RuntimeException
	 */
	protected String distinctName(String dir, String fileName, String fileType) {
		String markNow = STRUTIL.formatedTime("-yyyyMMdd-HHmmssSSS");
		return (new File(dir + fileName + "." + fileType).exists()) 
				? (dir + fileName + markNow + "." + fileType)
				: (dir + fileName + "." + fileType);
	}

	/**
	 * prepare to start tests, for testng beforetest.
	 * 
	 * @param className
	 *            the class name for log record file name
	 * @throws RuntimeException
	 */
	protected void testCunstruction(String className) {
		fName = LOG_DIR + className + ".xml";
		html = new HtmlFormatter4WD(fName, "date;millis;method;status;message;class");
		startTime = System.currentTimeMillis();
		startServer(className);
	}

	/**
	 * stop all started tests, for testng aftertest.
	 * 
	 * @throws RuntimeException
	 */
	protected void testTermination() {
		stopServer();
		endTime = System.currentTimeMillis();
		html.xmlTansToHtml(startTime, endTime);
	}

	/**======================================================================================
	 * user defined log to append standard server log.
	 * 
	 * @param clsName
	 *            extra log filename to append
	 * @return Logger
	 * @throws RuntimeException
	 */
	protected Logger xmlLogForWebRiver(String clsName) {
		Logger logger = Logger.getLogger(this.getClass().getName());
		try {
			handler = new FileHandler(LOG_DIR + clsName + ".xml", false);
			handler.setLevel(Level.FINE);
			handler.setFormatter(new XMLFormatter4WD());
			logger.addHandler(handler);
		} catch (Exception ex) {
			LOG.error("can not create logger for remotewebdriver!");
			throw new RuntimeException(ex.getMessage());
		}
		return logger;
	}

	/**
	 * wait milli seconds.
	 * 
	 * @param millis
	 *            time to wait, in millisecond
	 */
	protected void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param message
	 *            the message to be recroded to logs
	 */
	protected void pass(String message) {
		report("passed", message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param message
	 *            the message to be recroded to logs
	 */
	protected void fail(String message) {
		report("failed", message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param message
	 *            the message to be recroded to logs
	 */
	protected void warn(String message) {
		report("warned", message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param status
	 *            the result status to be logged
	 * @param message
	 *            the message to be recroded to logs
	 */
	private void report(String status, String message) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		System.setProperty("SMARK", "~");
		int first = 3, last = 3;
		String methodName = trace[first].getMethodName();
		
		for (int i = first; i < trace.length; i ++){
			if (trace[i].getClassName().contains(".reflect.")){
				last = ((i - 1) <= first) ? first : (i - 1);
				break;
			}
		}		
		String traceClass = trace[last].getClassName() + " # " + trace[last].getLineNumber();		
		log4wd.info(traceClass + System.getProperty("SMARK") 
				+ methodName + System.getProperty("SMARK")
				+ status + System.getProperty("SMARK")
				+ message.replace(System.getProperty("SMARK"), "-").replace("&", "&"));
	}
}