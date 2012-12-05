package com.star.toolapi.webdriver;

/**
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
import java.nio.charset.Charset;
import java.util.ResourceBundle;
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

	protected static final StringBufferUtils STRUTIL = new StringBufferUtils();
	protected static final Win32GuiByVbs VBS = new Win32GuiByVbs();
	protected static final Win32GuiByAu3 AU3 = new Win32GuiByAu3(); 
	protected static final BrowserGuiAuto IEAU3 = new BrowserGuiAuto();
	protected static final ParseProperties property = new ParseProperties("config/config.properties");
	
	protected final String ROOT_DIR = System.getProperty("user.dir");
	protected final String LOG_NAME = new File(property.get("log")).getName();
	protected final String LOG_REL = "./" + LOG_NAME + "/";
	protected final String LOG_ABS = ROOT_DIR + "/" + LOG_NAME + "/";

	private final InternetExplorerDriverLogLevel level = InternetExplorerDriverLogLevel.
														valueOf(property.get("SERVER_LOG_LEVEL"));
	private final LoggingManager LOG = new LoggingManager(WebDriverController.class.getName());
	private final RemoteControlConfiguration RCC = new RemoteControlConfiguration();
	private final Boolean SERVER_OUTPUT_ON = Boolean.parseBoolean(property.get("SERVER_OUTPUT_ON"));
	private final Boolean USE_DRIVERSERVER = Boolean.parseBoolean(property.get("USE_DRIVERSERVER"));
	private final String SMARK = "~";
	private final String EXEFILE = "./lib/IEDriverServer.exe";

	private static DesiredCapabilities capability;
	private static HtmlFormatter4WD html;
	private static String fName;
	private static long startTime;
	private static long endTime;
	
	/**
	 * choose a port to start the selenium server.
	 * 
	 * @param clsName the runtime class name
	 * @throws RuntimeException
	 */
	protected void startServer(String clsName) {
		log4wd = remoteMessageRecord(clsName);
		File log = new File(LOG_ABS + clsName + "_" + STRUTIL.getMilSecNow() + ".log");

		if (USE_DRIVERSERVER) {
			Builder builder = new InternetExplorerDriverService.Builder();
			System.setProperty("webdriver.ie.driver", EXEFILE);
			try {
				service = SERVER_OUTPUT_ON ? builder.usingAnyFreePort().withLogFile(log).withLogLevel(level)
						.build() : builder.usingAnyFreePort().withLogLevel(level).build();
				service.start();
			} catch (Throwable t) {
				LOG.error(t);
				throw new RuntimeException(t);
			}
		} else {
			String portStr[] = property.get("serverPort").split(";");
			for (int i = 0; i < portStr.length; i++) {
				try {
					RCC.setPort(Integer.parseInt(portStr[i]));
					RCC.setDebugMode(false);
					RCC.setSingleWindow(false);
					RCC.setEnsureCleanSession(true);
					RCC.setReuseBrowserSessions(false);
					if (SERVER_OUTPUT_ON) {
						RCC.setDontTouchLogging(false);
						RCC.setBrowserSideLogEnabled(true);
						RCC.setLogOutFile(log);
					}
					RCC.setTrustAllSSLCertificates(true);
					server = new SeleniumServer(false, RCC);
					server.start();
					break;
				} catch (Throwable t) {
					if (i == (portStr.length - 1)) {
						LOG.error(t);
						throw new RuntimeException(t);
					}
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
			if (USE_DRIVERSERVER) {
				service.stop();
			} else {
				server.stop();
			}
			handler.close();
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
			if (USE_DRIVERSERVER){
				driver = new RemoteWebDriver(service.getUrl(), capability);				
			}else{
				URL url = new URL("http://localhost:" + server.getPort() + "/wd/hub");
				driver = new RemoteWebDriver(url, capability);
			}
			actionDriver = new Actions(driver);
			pass("webdriver new instance created");
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
		}
	}

	/**
	 * start webdirver using browser iexplore
	 */
	protected void startWebDriver() {
		VBS.killWin32Process("werfault");
		VBS.killWin32Process("iexplore");
		
		//判断是否在虚拟机上运行，如果是则初始化代理设置！
		if (VBS.getEnvironment("USERNAME").toLowerCase().contains("autotest")){
			VBS.executeVbsFile("./assist/ResetProxy.vbs");
		}
		startWebDriver("ie");
	}

	/**
	 * closeWebDriver, close current session opened by webdriver.
	 * 
	 * @throws RuntimeException
	 */
	protected void closeWebDriver() {
		try {
			if (driver != null) {
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
			if (driver != null) {
				driver.quit();
				pass("all webdriver session closed");
			}
		} catch (Throwable t) {
			LOG.error(t);
			throw new RuntimeException(t.getMessage());
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
					sb.append(msgContent[3]);
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
				 * 
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
				+ message.replace(SMARK, "-").replace("&", "&amp;"));
	}
}