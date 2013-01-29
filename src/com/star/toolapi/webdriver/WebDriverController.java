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
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;

import org.openqa.selenium.TimeoutException;
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
import com.star.logging.webdriver.UserXMLFormatter;
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
	protected static int maxWaitfor = 10;//单步操作超时时间
	protected static int maxLoadTime = 90;//页面加载超时时间
	protected static int stepTimeUnit = 500;//单次循环思考时间
	protected final String ROOT_DIR = System.getProperty("user.dir");
	protected final String LOG_NAME = new File(CONFIG.get("log")).getName();
	protected final String LOG_REL = "./" + LOG_NAME + "/";
	protected final String LOG_ABS = ROOT_DIR + "/" + LOG_NAME + "/";
	protected final String EXECUTOR = VBS.getEnvironment("USERNAME");
	protected final String COMPUTER = VBS.getEnvironment("COMPUTERNAME");

	private final InternetExplorerDriverLogLevel level = InternetExplorerDriverLogLevel.
														valueOf(CONFIG.get("SERVER_LOG_LEVEL"));
	private final LoggingManager LOG = new LoggingManager(WebDriverController.class.getName());
	private final RemoteControlConfiguration RCC = new RemoteControlConfiguration();
	//是否打开server端详细文本日志的配置项
	private final Boolean SERVER_OUTPUT_ON = Boolean.parseBoolean(CONFIG.get("SERVER_OUTPUT_ON"));
	//是否使用selenium2.22.0版本以上的IEDriverServer模式的配置项
	private final Boolean USE_DRIVERSERVER = Boolean.parseBoolean(CONFIG.get("USE_DRIVERSERVER"));
	private final String seperateMark = "~";
	private static DesiredCapabilities capability;
	private static HtmlFormatter4WD html;
	private static String fName;
	private static long startTime;
	private static long endTime;
	private static String className;

	/**
	 * Description: config timeout setting for page load, default is 90 seconds</BR>
	 * 内容描述：配置页面加载的超时时间，默认是90秒钟。
	 * 
	 * @param timeout max wait time setting in seconds
	 */
	protected void setMaxLoadTime(int timeout) {
		WebDriverController.maxLoadTime = timeout;
	}

	/**
	 * Description: config timeout setting for each step, default is 10 seconds</BR>
	 * 内容描述：配置单个步骤运行的最大超时时间，默认是10秒钟。
	 * 
	 * @param timeout max wait time setting in seconds
	 */
	protected void setMaxWaitTime(int timeout) {
		WebDriverController.maxWaitfor = timeout;
	}

	/**
	 * Description: set sleep interval for loop wait.</BR>
	 * 内容描述：配置每个步骤中每次循环的最小时间单位。
	 * 
	 * @param interval milliseconds for each sleep
	 */
	protected void setSleepInterval(int interval) {
		WebDriverWebPublic.stepTimeUnit = interval;
	}

	/**
	 * Description: set page load timeout.</BR>
	 * 内容描述：设置页面加载超时时间.
	 * 
	 * @param seconds timeout in timeunit of seconds.
	 */
	protected void setPageLoadTimeout(int seconds){
		driver.manage().timeouts().pageLoadTimeout(seconds, TimeUnit.SECONDS);		
	}
	
	/**
	 * Description: set element locate timeout.</BR>
	 * 内容描述：设置对象查找超时时间.
	 * 
	 * @param seconds timeout in timeunit of seconds.
	 */
	protected void setElementLocateTimeout(int seconds){
		driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);		
	}

	/**
	 * Description: set scripts execute timeout.</BR>
	 * 内容描述：设置脚本执行超时时间.
	 * 
	 * @param seconds timeout in timeunit of seconds.
	 */
	protected void setScriptingTimeout(int seconds){
		driver.manage().timeouts().setScriptTimeout(seconds, TimeUnit.SECONDS);		
	}

	/**
	 * Description: start the selenium server.</BR>
	 * 内容描述：启动selenium/webdriver的代理服务。
	 * 
	 * @param clsName the runtime class name
	 * @throws Exception
	 */
	protected void startServer(String clsName) throws Exception{
		WebDriverController.className = clsName;
		log4wd = getLogger(className);
		File log = new File(LOG_ABS + className + "_" + STRUTIL.getMilSecNow() + ".log");

		if (USE_DRIVERSERVER) {
			useDriverServer(log);
		} else {
			String[] portStr = CONFIG.get("serverPort").split(";");
			useRemoteServer(log, portStr);
		}
	}

	/**
	 * Description: start webdirver</BR>
	 * 内容描述：启动WebDriver实例。
	 * 
	 * @param browser the browser mode
	 * @throws RuntimeException
	 */
	protected void startWebDriver(String browser) {
		try {
			setBuildEnvChoice(browser);
			String url = driverObjectInitalize();//about:blank is useless on some machines.
			driverStatusTest(driver, browser, url, 1);
			
			setPageLoadTimeout(maxLoadTime);
			setElementLocateTimeout(maxWaitfor);
			setScriptingTimeout(maxWaitfor);
			
			actionDriver = new Actions(driver);
			ASSERT = new StarNewAssertion(driver, LOG_ABS, className, log4wd, seperateMark);
			pass("webdriver new instance created");	
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * Description: start webdirver using browser iexplore</BR>
	 * 内容描述：默认选择IE模式创建WebDriver实例。
	 */
	protected void startWebDriver() {
		startWebDriver("ie");
	}

	/**
	 * Description: closeWebDriver, close current session opened by webdriver.</BR>
	 * 内容描述：关闭当前WebDriver创建的当前浏览器进程。
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
	 * Description: quitWebDriver, close webdriver instance and clear all sessions.</BR>
	 * 内容描述：销毁WebDriver实例。
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
	 * Description: stop the selenium server</BR>
	 * 内容描述：停止WebDriver的服务，无论什么模式。
	 */
	protected void stopServer() throws Exception {
		if (USE_DRIVERSERVER) {
			terminateService();
		} else {
			terminateServer();
		}
		if (handler != null){
			handler.close();
		}
	}

	/**
	 * Description: prepare to start tests, for testng beforetest.</BR>
	 * 内容描述：测试初始化，创建日志对象，启动工具服务。
	 * 
	 * @param className the class name for log record file name
	 * @throws RuntimeException
	 */
	protected void testCunstruction(String className) {
		fName = LOG_ABS + className + ".xml";
		html = new HtmlFormatter4WD(fName, "date;millis;method;status;message;class");
		startTime = System.currentTimeMillis();
		try {
			startServer(className);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Description: stop all started tests, for testng aftertest.</BR>
	 * 内容描述：测试销毁，停止测试工具服务，将日志转换为HTML格式。
	 * 
	 * @throws RuntimeException
	 */
	protected void testTermination() {
		try {
			stopServer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			endTime = System.currentTimeMillis();
			html.xmlTansToHtml(startTime, endTime);			
		}
	}

	/**
	 * Description: wait milli seconds.</BR>
	 * 内容描述：进程等待，避免使用。
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
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作成功，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void pass(String message) {
		report("passed", message);
	}

	/**
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作失败，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void fail(String message) {
		report("failed", message);
	}

	/**
	 * Description: record to logs with fail info messages and exit run.</BR>
	 * 内容描述：报告操作失败，并且退出本次运行，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void failAndExit(String message) {
		report("failed", message);
		throw new RuntimeException(message);
	}

	/**
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作告警，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void warn(String message) {
		report("warned", message);
	}

	/**
	 * Description: get system properties by specified keyname</BR>
	 * 内容描述：读取系统中指定名称的变量，如果没有设置则抛出Exception。
	 * 
	 * @param keyName the name of the property.
	 * @throws RuntimeException
	 */
	private String getExecutableEnv(String keyName) {
		Properties properties = System.getProperties();
		if (properties.containsKey(keyName)){
			return properties.getProperty(keyName).toString() + "\\IEDriverServer.exe";
		}else{
			throw new RuntimeException("the file IEDriverServer.exe was not placed correctly!");
		}
	}

	/**
	 * Description: config the executable exe file of IEDriverServer.exe</BR>
	 * 内容描述：指定IEDriverServer.exe所在的位置，并且配置环境变量。
	 * @throws Exception
	 */
	private void setExecutableIEDriverServer() throws Exception{
		String executable = "./lib/IEDriverServer.exe";
		if (!new File(executable).exists()) {
			executable = getExecutableEnv("EXECUTABLE");
		}
		System.setProperty("webdriver.ie.driver", executable);
	}

	/**
	 * Description: start iedirver service with log print and exception handled.</BR>
	 * 内容描述：启动IEDirverServer模式的WebDriver代理服务。
	 * 
	 * @param logFile the log file File.
	 * @throws Exception
	 */
	private void useDriverServer(File logFile) throws Exception{
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
	 * Description: start iedirver service.</BR>
	 * 内容描述：启动IEDirverServer服务。
	 * 
	 * @param needLog bool if log needed.
	 * @param logFile the log file File.
	 * @throws Exception
	 */
	private void startService(boolean needLog, File logFile) throws Exception{
		setExecutableIEDriverServer();
		Builder builder = new InternetExplorerDriverService.Builder();
		if (needLog) {
			service = builder.usingAnyFreePort().withLogFile(logFile).withLogLevel(level).build();
		} else {
			service = builder.usingAnyFreePort().withLogLevel(level).build();
		}
		service.start();	
	}

	/**
	 * Description: try some time to start remote server.</BR>
	 * 内容描述：配置文件中指定可以使用哪些端口，逐个尝试启动RemoteWebDriver Server。
	 * 
	 * @param logFile the log file File.
	 * @param portArray the usable port list array.
	 * @throws Exception
	 */
	private void useRemoteServer(File logFile, String[] portArray) throws Exception{
		Exception exception = new RuntimeException("init value");
		int i = 0;
		while (null != exception && i < portArray.length){
			exception = startRemoteServer(logFile, Integer.parseInt(portArray[i]));
			i ++;
		}
		if (null != exception){
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Description: try to start remote server after remote server configed.</BR>
	 * 内容描述：配置服务选项之后启动RemoteWebDriver Server。
	 * 
	 * @param logFile the log file File.
	 * @param port the port to start RemoteWebDriver server.
	 */
	private Exception startRemoteServer(File logFile, int port) {
		try {
			setRemoteControl(logFile, port);
			server = new SeleniumServer(false, RCC);
			server.start();
			System.out.println("server on " + EXECUTOR + "@" + COMPUTER + " has started at: " 
							 + "http://localhost:" + server.getPort() + "/wd/hub");
			return null;
		} catch (Exception e) {
			return e;
		}
	}

	/**
	 * Description: config the remote server options.</BR>
	 * 内容描述：为RemoteWebDriver Server做服务端选项么配置。
	 * 
	 * @param logFile the log file File.
	 * @param port the port to be used for server.
	 * @throws Exception
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
	 * Description: judge and set the env choice of local or remote. </BR>
	 * 内容描述：根据配置选择是在本地运行还是远程代理环境。
	 * 
	 * @throws Exception
	 */
	private void setBuildEnvChoice(String browser) throws Exception{
		if (EXECUTOR.toLowerCase().contains("autotest")){
			setBrowserRemotely(browser);
		}else{
			setBrowserLocally(browser);
		}		
	}

	/**
	 * Description: start webdirver after capability settings completed.</BR>
	 * 内容描述：在做好配置之后创建WebDriver实例。
	 * 
	 * @throws	Exception
	 */
	private String driverObjectInitalize() throws Exception{
		if (USE_DRIVERSERVER){//是否使用IEDirverServer
			driver = new RemoteWebDriver(service.getUrl(), capability);	
			return service.getUrl().toString();
		}else{
			URL url = new URL("http://localhost:" + server.getPort() + "/wd/hub");
			driver = new RemoteWebDriver(url, capability);
			return "http://localhost:" + server.getPort() + "/wd/hub";
		}
	}

	/**
	 * Description: catch page load timeout Exception and restart a new session.</BR>
	 * 内容描述：通过页面跳转是否超时来测试WebDriver启动时是否发生挂死异常。
	 *
	 * @param driver RemoteWebDriver object.
	 * @param browser the browser mode.
	 * @param testUrl the url used to navigate by the driver.get method.
	 * @throws Exception
	 */
	private boolean hasLoadPageSucceeded(RemoteWebDriver driver, String browser, String testUrl) throws Exception {
		try {
			driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
			driver.get(testUrl);
			return true;
		} catch (TimeoutException te) {
			LOG.error("******************本次启动WebDriver异常挂起******************");
			setBuildEnvChoice(browser);
			driverObjectInitalize();
			return false;
		}		
	}

	/**
	 * Description: catch page load timeout Exception and restart a new session.</BR>
	 * 内容描述：循环一定次数测试WebDriver启动是否挂死。
	 *
	 * @param driver RemoteWebDriver object.
	 * @param browser the browser mode.
	 * @param testUrl the url used to navigate by the driver.get method.
	 * @param repeatTimes retry times.
	 * @throws Exception
	 */
	private void driverStatusTest(RemoteWebDriver driver, String browser, String testUrl, int repeatTimes) throws Exception {
		int index = 0;
		boolean suspended = true;
		while (index < repeatTimes && suspended){
			suspended = !hasLoadPageSucceeded(driver, browser, testUrl);
			index ++;
		}
		if (index == repeatTimes && suspended){
			throw new RuntimeException("can not start webdriver successfully, it's suspended!");
		}
	}

	/**
	 * set browser mode on visual machines: close browsers already opened.</BR>
	 * 内容描述：选择在远程代理环境执行，需要比本地多一步杀浏览器进程的操作。
	 * 
	 * @throws Exception
	 */
	private void setBrowserRemotely(String browser) throws Exception{
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
	 * Description: set browser mode on local machines: do not close browsers already opened.</BR>
	 * 内容描述：选择在本机执行，有人工干预，无需杀掉浏览器进程。
	 * 
	 * @throws Exception
	 */
	private void setBrowserLocally(String browser) throws Exception{
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
	 * Description: stop the remote webdriver server.</BR>
	 * 内容描述：停止RemoteWebDriver Server。
	 */
	private void terminateServer() throws Exception{
		if (server != null){
			server.stop();
		}
	}

	/**
	 * Description: stop the iedriver service.</BR>
	 * 内容描述：停止IEDirverServer的服务。
	 */
	private void terminateService() throws Exception{
		if (service != null){
			service.stop();
		}
	}

	/**
	 * Description: user defined log to append standard server log.</BR>
	 * 内容描述：创建日志对象。
	 * 
	 * @param clsName extra log filename to append
	 * @return Logger
	 * @throws Exception
	 */
	private Logger getLogger(String clsName) throws Exception{
		Logger logger = Logger.getLogger(this.getClass().getName());
		UserXMLFormatter formatter = new UserXMLFormatter(seperateMark);
		handler = new FileHandler(LOG_ABS + clsName + ".xml", false);
		handler.setLevel(Level.FINE);
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		return logger;
	}

	/**
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作信息，根据不同的状态记录对应日志。
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
		log4wd.info(traceClass + seperateMark + methodName + seperateMark + status 
				+ seperateMark + message.replace(seperateMark, "-").replace("&", "&"));
	}
}