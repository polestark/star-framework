package com.star.toolapi.webdriver;

/**
 * 框架说明：
 * 1、使用RemoteWebDriver代替WebDriver，可直接使用JavascriptExecutor、TakesScreenshot，使用
 * 	  RemoteControlConfiguration配置SeleniumServer，截取HttpClient日志，便于测试开发调试；
 * 2、WebDriver启动时可选择浏览器，支持ie/ff/chrome/opera/safari/htmlunit模式，默认为ie；
 * 3、SeleniumServer启动时创建logger，记录操作日志，停止时可选择是否转换为html格式日志，同时，
 * 	     服务器的文本格式日志可根据配置项选择是否打开（建议不打开），xml格式的日志也可选择是否保留；
 * 4、测试初始化和结束销毁是使用TestNG的BeforeTest(alwaysRun=true)形式来确保其始终执行的，建
 * 	  议不直接使用JUnit，更不要在测试代码中加入可能导致JVM crash的事务，否则日志可能记录不完整。
 * 
 * @author 测试仔刘毅
 */

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;

import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService.Builder;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.server.RemoteControlConfiguration;

import com.star.frame.assertion.StarNewAssertion;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;
import com.star.support.externs.BrowserGuiAuto;
import com.star.support.externs.Win32GuiByAu3;
import com.star.support.externs.Win32GuiByVbs;
import com.star.logging.webdriver.HtmlFormatter4WD;
import com.star.testdata.string.StringBufferUtils;

public class WebDriverController {

	protected static InternetExplorerDriverService service;
	protected static RemoteWebDriver driver;
	protected static Actions actionDriver;
	protected static SeleniumServer server;
	protected static Handler handler;
	protected static Logger log4wd;
	protected static StarNewAssertion ASSERT;

	protected static final StringBufferUtils STRUTIL = new StringBufferUtils();
	protected static final Win32GuiByVbs VBS = new Win32GuiByVbs();
	protected static final Win32GuiByAu3 AU3 = new Win32GuiByAu3(); 
	protected static final BrowserGuiAuto IEAU3 = new BrowserGuiAuto();
	protected static final ParseProperties CONFIG = new ParseProperties("config/config.properties");
	protected static int maxWaitfor = 10;
	protected static int maxLoadTime = 90;
	protected static int stepTimeUnit = 1;
	
	protected final String ROOT_DIR = System.getProperty("user.dir");
	protected final String LOG_NAME = new File(CONFIG.get("log")).getName();
	protected final String LOG_REL = "./" + LOG_NAME + "/";
	protected final String LOG_ABS = ROOT_DIR + "/" + LOG_NAME + "/";
	protected final String EXECUTOR = VBS.getEnvironment("USERNAME");
	protected final String COMPUTER = VBS.getEnvironment("COMPUTERNAME");
	protected final String FORMATTER = "_yyyyMMddHHmmssSSS";

	private final InternetExplorerDriverLogLevel level = InternetExplorerDriverLogLevel.
														valueOf(CONFIG.get("SERVER_LOG_LEVEL"));
	private final LoggingManager LOG = new LoggingManager(WebDriverController.class.getName());
	private final RemoteControlConfiguration RCC = new RemoteControlConfiguration();
	
	//是否打开server端详细文本日志的配置项
	private final Boolean SERVER_OUTPUT_ON = Boolean.parseBoolean(CONFIG.get("SERVER_OUTPUT_ON"));
	//是否使用selenium2.22.0版本以上的IEDriverServer模式的配置项
	private final Boolean USE_DRIVERSERVER = Boolean.parseBoolean(CONFIG.get("USE_DRIVERSERVER"));
	
	private final String SMARK = "~";
	private final String EXEFILE = "./lib/IEDriverServer.exe";

	private static DesiredCapabilities capability;
	private static HtmlFormatter4WD html;
	private static String fName;
	private static long startTime;
	private static long endTime;
	private static String className;

	/**
	 * config timeout setting for page load, default is 90 seconds</BR>
	 * 配置页面加载的超时时间，默认是90秒钟。
	 * 
	 * @param 	timeout max wait time setting in seconds
	 */
	protected void setMaxLoadTime(int timeout) {
		WebDriverController.maxLoadTime = timeout;
	}

	/**
	 * config timeout setting for each step, default is 10 seconds</BR>
	 * 配置单个步骤运行的最大超时时间，默认是10秒钟。
	 * 
	 * @param 	timeout max wait time setting in seconds
	 */
	protected void setMaxWaitTime(int timeout) {
		WebDriverController.maxWaitfor = timeout;
	}

	/**
	 * set sleep interval for loop wait.
	 * 
	 * @param 	interval milliseconds for each sleep
	 */
	protected void setSleepInterval(int interval) {
		WebDriverWebPublic.stepTimeUnit = interval;
	}
	
	/**
	 * choose a port to start the selenium server.
	 * 
	 * @param clsName the runtime class name
	 * @throws RuntimeException
	 */
	protected void startServer(String clsName) {
		WebDriverController.className = clsName;
		log4wd = remoteMessageRecord(className);
		File log = new File(LOG_ABS + className + "_" + STRUTIL.getMilSecNow() + ".log");

		if (USE_DRIVERSERVER) {
			useDriverServer(log);
		} else {
			String[] portStr = CONFIG.get("serverPort").split(";");
			useRemoteServer(log, portStr);
		}
	}

	/**
	 * start iedirver service with log print and exception handled.
	 * 
	 * @param logFile the log file File.
	 * @throws RuntimeException
	 */
	private void useDriverServer(File logFile){
		try {
			startService(SERVER_OUTPUT_ON, logFile);
			System.out.println("server on " + EXECUTOR + "@" + COMPUTER 
					+ " has started at: " + service.getUrl().toString());
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * start iedirver service.
	 * 
	 * @param needLog bool if log needed.
	 * @param logFile the log file File.
	 * @throws Exception
	 */
	private void startService(boolean needLog, File logFile) throws Exception{
		System.setProperty("webdriver.ie.driver", EXEFILE);
		Builder builder = new InternetExplorerDriverService.Builder();
		if (needLog) {
			service = builder.usingAnyFreePort().withLogFile(logFile).withLogLevel(level).build();
		} else {
			service = builder.usingAnyFreePort().withLogLevel(level).build();
		}
		service.start();	
	}

	/**
	 * start remote server.
	 * 
	 * @param logFile the log file File.
	 * @param	portArray the usable port array.
	 * 
	 * @throws RuntimeException
	 */
	private void useRemoteServer(File logFile, String[] portArray){
		//配置文件中指定可以使用哪些备用端口，逐个尝试。
		Exception exception = null;
		for (int index = 0; index < portArray.length - 1; index ++) {
			try {
				setRemoteControl(logFile, Integer.parseInt(portArray[index]));
				server = new SeleniumServer(false, RCC);
				server.start();
				System.out.println("server on " + EXECUTOR + "@" + COMPUTER + " has started at: " 
						+ "http://localhost:" + server.getPort() + "/wd/hub");
				return;
			} catch (Exception e) {
				exception = e;
			}
		}
		LOG.error(exception);
		throw new RuntimeException(exception);
	}

	/**
	 * config the remote server options.
	 * 
	 * @param logFile the log file File.
	 * @param	port the port to be used for server.
	 * 
	 * @throws RuntimeException
	 */
	private void setRemoteControl(File logFile, int port) throws Exception{
		RCC.setPort(port);
		RCC.setDebugMode(false);
		RCC.setSingleWindow(false);
		RCC.setEnsureCleanSession(true);
		RCC.setReuseBrowserSessions(false);
		if (SERVER_OUTPUT_ON) {
			RCC.setDontTouchLogging(false);
			RCC.setBrowserSideLogEnabled(true);
			RCC.setLogOutFile(logFile);
		}
		RCC.setTrustAllSSLCertificates(true);
	}

	/**
	 * start webdirver
	 * 
	 * @throws RuntimeException
	 */
	protected void startWebDriver(String browser) {
		if (EXECUTOR.toLowerCase().contains("autotest")){
			setBrowserRemotely(browser);
		}else{
			setBrowserLocally(browser);
		}		
		try {
			createDriverInstanse();
			driver.manage().timeouts().implicitlyWait(maxWaitfor, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(maxWaitfor, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(maxLoadTime, TimeUnit.SECONDS);
			actionDriver = new Actions(driver);

	        ASSERT = new StarNewAssertion(driver, LOG_ABS, className, log4wd, SMARK);
	        
			pass("webdriver new instance created");	
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * set browser mode on visual machines: close browsers already opened.
	 * 
	 * @throws IllegalArgumentException
	 */
	private void setBrowserRemotely(String browser){
		if (browser.toLowerCase().contains("ie") || browser.toLowerCase().contains("internetexplorer")) {
			capability = DesiredCapabilities.internetExplorer();
			VBS.killWin32Process("iexplore");
		} else if (browser.toLowerCase().contains("ff") || browser.toLowerCase().contains("firefox")) {
			capability = DesiredCapabilities.firefox();
			VBS.killWin32Process("firefox");
		} else if (browser.toLowerCase().contains("chrome")) {
			capability = DesiredCapabilities.chrome();
			VBS.killWin32Process("chrome");
		} else if (browser.toLowerCase().contains("safari")) {
			capability = DesiredCapabilities.safari();
			VBS.killWin32Process("safari");
		} else if (browser.toLowerCase().contains("opera")) {
			capability = DesiredCapabilities.opera();
			VBS.killWin32Process("opera");
		} else if (browser.toLowerCase().contains("htmlunit")) {
			capability = DesiredCapabilities.htmlUnit();
		} else {
			throw new IllegalArgumentException("you are using wrong mode of browser paltform!");
		}		
	}

	/**
	 * set browser mode on local machines: do not close browsers already opened.
	 * 
	 * @throws IllegalArgumentException
	 */
	private void setBrowserLocally(String browser){
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
	}

	/**
	 * start webdirver after capability settings completed.
	 * 
	 * @throws	Exception
	 */
	private void createDriverInstanse() throws Exception{
		if (USE_DRIVERSERVER){//是否使用IEDirverServer
			driver = new RemoteWebDriver(service.getUrl(), capability);	
		}else{
			URL url = new URL("http://localhost:" + server.getPort() + "/wd/hub");
			driver = new RemoteWebDriver(url, capability);
		}
	}

	/**
	 * start webdirver using browser iexplore
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
		if (driver != null) {
			driver.close();
			pass("closed current webdriver session");
		}
	}

	/**
	 * quitWebDriver, close webdriver instance and clear all sessions.
	 * 
	 * @throws RuntimeException
	 */
	protected void destroyWebDriver() {
		if (driver != null) {
			driver.quit();
			pass("all webdriver session closed");
		}
	}

	/**
	 * stop the remote webdriver server.
	 */
	private void termiServer(){
		if (server != null){
			server.stop();
		}
	}

	/**
	 * stop the iedriver service.
	 */
	private void termiService(){
		if (service != null){
			service.stop();
		}
	}

	/**
	 * stop the selenium server
	 */
	protected void stopServer() {
		if (USE_DRIVERSERVER) {
			termiService();
		} else {
			termiServer();
		}
		if (handler != null){
			handler.close();
		}
	}

	/**
	 * prepare to start tests, for testng beforetest.
	 * 
	 * @param className the class name for log record file name
	 * @throws RuntimeException
	 */
	protected void testCunstruction(String className) {
		fName = LOG_ABS + className + ".xml";
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

	/**
	 * user defined log to append standard server log.
	 * 
	 * @param clsName extra log filename to append
	 * @return Logger
	 * @throws RuntimeException
	 */
	private Logger remoteMessageRecord(String clsName) {
		Logger logger = Logger.getLogger(this.getClass().getName());
		try {
			Formatter xmlFormatter = new Formatter() {

				/**
				 * user defined format to append xml log files
				 * 
				 * @param record currnet thread info
				 **/
				@Override
				public String format(LogRecord record) {
					String[] msgContent = record.getMessage().split(SMARK);
					StringBuffer sb = new StringBuffer(500);
					String message = msgContent[3];
					
					//XML特殊字符的处理
					message = message.replace("&", "&amp;");
					message = message.replace("<", "&lt;");
					message = message.replace(">", "&gt;");
					message = message.replace("'", "&apos;");
					message = message.replace("\"", "&quot;");
					
					sb.append("<record>\n");					
					sb.append("  <date>");// log current time
					sb.append(STRUTIL.formatedTime("HH:mm:ss.SSS"));
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
					if (parameters != null && parameters.length != 0
							&& record.getMessage().indexOf("{") == -1) {
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
				 * @param h the logger file handler
				 **/
				@Override
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
				@Override
				public String getTail(Handler h) {
					return "</log>\n";
				}
			};
			handler = new FileHandler(LOG_ABS + clsName + ".xml", false);
			handler.setLevel(Level.FINE);
			handler.setFormatter(xmlFormatter);
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
	 * @param millis time to wait, in millisecond
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
	 * @param message the message to be recroded to logs
	 */
	protected void pass(String message) {
		report("passed", message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void fail(String message) {
		report("failed", message);
	}

	/**
	 * record to logs with fail info messages and exit run.
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void failAndExit(String message) {
		report("failed", message);
		throw new RuntimeException(message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void warn(String message) {
		report("warned", message);
	}

	/**
	 * record to logs with fail info messages.
	 * 
	 * @param status the result status to be logged
	 * @param message the message to be recroded to logs
	 */
	private void report(String status, String message) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		int first = 3, last = 3;
		String methodName = trace[first].getMethodName();

		for (int i = first; i < trace.length; i++) {
			if (trace[i].getClassName().contains(".reflect.")) {
				last = ((i - 1) <= first) ? first : (i - 1);
				break;
			}
		}
		String traceClass = trace[last].getClassName() + " # " + trace[last].getLineNumber();
		log4wd.info(traceClass + SMARK + methodName + SMARK + status + SMARK
				+ message.replace(SMARK, "-").replace("&", "&"));
	}
}