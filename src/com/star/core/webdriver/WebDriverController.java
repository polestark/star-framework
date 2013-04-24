package com.star.core.webdriver;

/**
 * @author 测试仔刘毅
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Handler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService.Builder;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.star.core.webdriver.helper.WebDriverListener;
import com.star.frame.assertion.Assert4STAR;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;
import com.star.support.externs.BrowserGuiAuto;
import com.star.support.externs.Win32GuiByAu3;
import com.star.support.externs.Win32GuiByVbs;
import com.star.logging.webdriver.LoggerModeChoice;
import com.star.testdata.string.StringBufferUtils;
import com.star.tools.ReadConfiguration;
import com.star.tools.StackTraceUtils;

public class WebDriverController {
	protected static final ParseProperties CONFIG = new ParseProperties("config/config.properties");
	private final static ReadConfiguration config = new ReadConfiguration(
			"/com/star/core/webdriver/webdirver_config.properties");

	protected static InternetExplorerDriverService service;
	protected static WebDriver driver;
	protected static Actions actionDriver;
	protected static SeleniumServer server;
	protected static Handler handler;
	protected static Logger logger;
	protected static Assert4STAR ASSERT;
	
	protected static final StringBufferUtils STRUTIL = new StringBufferUtils();
	protected static final Win32GuiByVbs VBS = new Win32GuiByVbs();
	protected static final Win32GuiByAu3 AU3 = new Win32GuiByAu3(); 
	protected static final BrowserGuiAuto IEAU3 = new BrowserGuiAuto();
	protected static final String FORMATTER = config.get("DATE_FORMATTER");
	
	protected int maxWaitfor = Integer.parseInt(config.get("STEP_TIMEOUT"));//单步操作超时时间
	protected int maxLoadTime = Integer.parseInt(config.get("PAGE_LOAD_TIMEOUT"));//页面加载超时时间
	protected int stepTimeUnit = Integer.parseInt(config.get("SLEEP_INTERVAL"));//单次循环思考时间
	
	protected final String LOG_NAME = new File(CONFIG.get("log")).getName();
	protected final String LOG_REL = ".\\" + LOG_NAME + "\\";
	protected final String LOG_ABS = System.getProperty("user.dir") + "\\" + LOG_NAME + "\\";
	protected final String EXECUTOR = VBS.getEnvironment("USERNAME");
	protected final String COMPUTER = VBS.getEnvironment("COMPUTERNAME");
	private final InternetExplorerDriverLogLevel level = InternetExplorerDriverLogLevel.
			valueOf(config.get("SERVER_LOG_LEVEL"));
	//是否打开server端详细文本日志的配置项
	private final Boolean SERVER_OUTPUT_ON = Boolean.parseBoolean(config.get("SERVER_OUTPUT_ON"));
	//是否使用selenium2.22.0版本以上的IEDriverServer模式的配置项
	private final Boolean USE_DRIVERSERVER = Boolean.parseBoolean(config.get("USE_DRIVERSERVER"));

	private final StackTraceUtils stack = new StackTraceUtils();
	private final LoggingManager LOG = new LoggingManager(WebDriverController.class.getName());
	
	private RemoteControlConfiguration remote;
	private static DesiredCapabilities capabilities;
	private static LoggerModeChoice logHelper;
	private static long startTime;
	private static long endTime;
	private static String className;
	private final int DRIVER_STATUS_TEST_TIMES = 2;
	private final int DRIVER_START_TIMEOUT = 30000;

	/**
	 * Description: config timeout setting for page load, default is 90 seconds</BR>
	 * 内容描述：配置页面加载的超时时间，默认是90秒钟。
	 * 
	 * @param timeout max wait time setting in seconds
	 */
	protected void setMaxLoadTime(int timeout) {
		this.maxLoadTime = timeout;
	}

	/**
	 * Description: config timeout setting for each step, default is 10 seconds</BR>
	 * 内容描述：配置单个步骤运行的最大超时时间，默认是10秒钟。
	 * 
	 * @param timeout max wait time setting in seconds
	 */
	protected void setMaxWaitTime(int timeout) {
		this.maxWaitfor = timeout;
	}

	/**
	 * Description: set sleep interval for loop wait.</BR>
	 * 内容描述：配置每个步骤中每次循环的最小时间单位。
	 * 
	 * @param interval milliseconds for each sleep
	 */
	protected void setSleepInterval(int interval) {
		this.stepTimeUnit = interval;
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
		logHelper = new LoggerModeChoice(className,LOG_ABS,"GBK");
		logHelper.LogInit(startTime);
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
	 * @param browserMode the browser mode
	 */
	private void startWebDriver(String browserMode){
		try {
			setBrowserMode(browserMode);
			initalizeWebDriver(DRIVER_START_TIMEOUT);
			
			//the address "about:blank" is sometimes useless.
			ensureWebDriverStatus(browserMode, getServerAddress(), DRIVER_STATUS_TEST_TIMES);
			
			setPageLoadTimeout(maxLoadTime);
			setElementLocateTimeout(maxWaitfor);
			setScriptingTimeout(maxWaitfor);
			
			actionDriver = new Actions(driver);
			ASSERT = new Assert4STAR(driver, LOG_ABS, className, logHelper);
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
	}

	/**
	 * Description: prepare to start tests, for testng beforetest.</BR>
	 * 内容描述：测试初始化，创建日志对象，启动工具服务。
	 * 
	 * @param className the class name for log record file name
	 * @throws RuntimeException
	 */
	protected void testCunstruction(String className) {
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
			logHelper.LogDestory(endTime);
		}
	}

	/**
	 * Description: wait milliseconds.</BR>
	 * 内容描述：进程等待，尽量避免使用。
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
	 * Description: wait milliseconds.</BR>
	 * 内容描述：进程等待，尽量避免使用。
	 * 
	 * @param thread the thread to wait for other event.
	 * @param millis time to wait, in millisecond
	 */
	protected void waitFor(Thread thread, long millis){
		try {
			thread.join(millis);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Description: wait milliseconds.</BR>
	 * 内容描述：进程等待，尽量避免使用。
	 * 
	 * @param millis time to wait, in millisecond
	 */
	protected void waitFor(long millis){
		waitFor(Thread.currentThread(), millis);
	}

	/**
	 * Description: record to logs with passed info messages.</BR>
	 * 内容描述：报告操作成功，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void pass(String message) {
		logHelper.LogWrite(stack.traceRecord(Thread.currentThread().getStackTrace(), "passed", message));
	}

	/**
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作失败，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void fail(String message) {
		logHelper.LogWrite(stack.traceRecord(Thread.currentThread().getStackTrace(), "failed", message));
	}

	/**
	 * Description: record to logs with fail info messages and exit run.</BR>
	 * 内容描述：报告操作失败，并且退出本次运行，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void failAndExit(String message) {
		logHelper.LogWrite(stack.traceRecord(Thread.currentThread().getStackTrace(), "failed", message));
		throw new RuntimeException(message);
	}

	/**
	 * Description: record to logs with fail info messages.</BR>
	 * 内容描述：报告操作告警，记录对应信息日志。
	 * 
	 * @param message the message to be recroded to logs
	 */
	protected void warn(String message) {
		logHelper.LogWrite(stack.traceRecord(Thread.currentThread().getStackTrace(), "warned", message));
	}
	
	/**
	 * system console output messages.
	 * @param message the message info.
	 */
	protected void consolePrint(String message){
		System.out.println(message);
	}

	/**
	 * system error output messages.
	 * @param message the message info.
	 */
	protected void consoleError(String message){
		System.err.println(message);
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
			consolePrint("server on " + EXECUTOR + "@" + COMPUTER 
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
	private void startService(boolean needLog, File logFile) throws Exception {
		setExecutableIEDriverServer();
		Builder builder = new InternetExplorerDriverService.Builder();
		if (needLog) {
			service = builder.usingAnyFreePort().withLogFile(logFile).withLogLevel(level).build();
		} else {
			service = builder.usingAnyFreePort().withLogLevel(level).build();
		}
		service.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				service.stop();
			}
		});
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
			remote = new RemoteControlConfiguration();
			setRemoteControl(remote, logFile, port);
			server = new SeleniumServer(false, remote);
			server.start();
			consolePrint("server on " + EXECUTOR + "@" + COMPUTER + " has started at: " 
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
	private void setRemoteControl(RemoteControlConfiguration rcc, File logFile, int port) throws Exception{
		rcc.setPort(port);
		rcc.setDebugMode(false);
		rcc.setSingleWindow(false);
		rcc.setEnsureCleanSession(true);
		rcc.setReuseBrowserSessions(false);
		if (SERVER_OUTPUT_ON) {
			rcc.setDontTouchLogging(false);
			rcc.setBrowserSideLogEnabled(true);
			rcc.setLogOutFile(logFile);
		}
		rcc.setTrustAllSSLCertificates(true);
	}

	/**
	 * Description: start webdirver after capabilities settings completed.</BR>
	 * 内容描述：在做好配置之后创建WebDriver实例。
	 */
	private void initalizeWebDriver() {
		WebDriverListener listener = new WebDriverListener(LOG_ABS, className);
		if (USE_DRIVERSERVER) {// 是否使用IEDirverServer
			driver = new EventFiringWebDriver(new RemoteWebDriver(service.getUrl(), capabilities)).register(listener);
		} else {
			try {
				URL url = new URL("http://localhost:" + server.getPort() + "/wd/hub");
				driver = new EventFiringWebDriver(new RemoteWebDriver(url, capabilities)).register(listener);
			} catch (MalformedURLException e) {
				throw new RuntimeException("illegal url!");				
			}
		}
	}
	
	/**
	 * Description: start and see if webdirver start successfully.</BR>
	 * 内容描述：创建并且判断WebDriver实例是否启动成功。
	 * 
	 * @param timeout timeout for start webdriver.
	 * @param redoCount retry times for start webdriver.
	 * @throws	Exception
	 */
	private void initalizeWebDriver(long timeout, int redoCount) throws Exception {
		for (int i = 0; i < redoCount; i++) {
			Thread thread_start = new Thread(new Runnable() {
				public void run() {// 用一个独立的线程启动WebDriver
					initalizeWebDriver();
				}
			});
			thread_start.start();
			waitFor(thread_start, timeout);//为启动WebDriver设定超时时间
			if (!thread_start.isAlive()) {
				return;
			} else {
				thread_start.interrupt();
				consoleError("start Webdriver failed 【" + i + "】 times!");
			}
			if (thread_start.isAlive() && i == redoCount){// 如果最终没能启动成功则抛出错误
				thread_start.interrupt();
				throw new RuntimeException("can not start webdriver, check your platform configurations!");
			}
		}
	}
	
	/**
	 * Description: start and see if webdirver start successfully, default retry 2 times.</BR>
	 * 内容描述：创建并且判断WebDriver实例是否启动成功，默认重试次数为2次。
	 * 
	 * @param timeout timeout for start webdriver.
	 * @throws	Exception
	 */
	private void initalizeWebDriver(long timeout) throws Exception {
		initalizeWebDriver(timeout, 2);
	}
	
	/**
	 * Description: get the url of the webdriver server/service.
	 * 内容描述：获取WebDriver服务器的URL。
	 *
	 * @return the url of the webdriver server/service.
	 */
	private String getServerAddress() throws Exception{
		if (USE_DRIVERSERVER){//是否使用IEDirverServer
			return service.getUrl().toString();
		}else{
			return "http://localhost:" + server.getPort() + "/wd/hub";
		}
	}

	/**
	 * Description: catch page load timeout Exception and restart a new session.</BR>
	 * 内容描述：通过页面跳转是否超时来测试WebDriver启动时是否发生挂死异常。
	 *
	 * @param browserMode the browser mode.
	 * @param testUrl the url used to navigate by the driver.get method.
	 * @throws Exception
	 */
	private boolean pageLoadStateHealth(String browserMode, String testUrl) throws Exception {
		try {
			driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
			driver.get(testUrl);
			return true;
		} catch (TimeoutException te) {
			setBrowserMode(browserMode);
			initalizeWebDriver(DRIVER_START_TIMEOUT);
			return false;
		}
	}

	/**
	 * Description: catch page load timeout Exception and restart a new session.</BR>
	 * 内容描述：循环一定次数测试WebDriver启动是否挂死。
	 *
	 * @param browserMode the browser mode.
	 * @param testUrl the url used to navigate by the driver.get method.
	 * @param actionCount max time to test driver status.
	 * @throws Exception
	 */
	private void ensureWebDriverStatus(String browserMode, String testUrl, int actionCount) throws Exception {
		int index = 0;
		boolean suspended = true;
		while (index <= actionCount && suspended) {
			suspended = !pageLoadStateHealth(browserMode, testUrl);
			index++;
		}
		if (index > actionCount && suspended) {
			throw new RuntimeException("can not start webdriver successfully, it's suspended!");
		}
	}

	/**
	 * set browser mode on visual machines: close browsers already opened.</BR>
	 * 内容描述：选择在远程代理环境执行，需要比本地多一步杀浏览器进程的操作。
	 * 
	 * @throws Exception
	 */
	private void setBrowserMode(String browser) throws Exception{
		if (browser.toLowerCase().contains("ie") || browser.toLowerCase().contains("internetexplorer")) {
			capabilities = DesiredCapabilities.internetExplorer();
			VBS.killWin32Process("iexplore");
		} else if (browser.toLowerCase().contains("ff") || browser.toLowerCase().contains("firefox")) {
			capabilities = DesiredCapabilities.firefox();
			VBS.killWin32Process("firefox");
		} else if (browser.toLowerCase().contains("chrome")) {
			capabilities = DesiredCapabilities.chrome();
			VBS.killWin32Process("chrome");
		} else if (browser.toLowerCase().contains("safari")) {
			capabilities = DesiredCapabilities.safari();
			VBS.killWin32Process("safari");
		} else if (browser.toLowerCase().contains("opera")) {
			capabilities = DesiredCapabilities.opera();
			VBS.killWin32Process("opera");
		} else if (browser.toLowerCase().contains("htmlunit")) {
			capabilities = DesiredCapabilities.htmlUnit();
		} else {
			throw new IllegalArgumentException("you are using wrong mode of browser paltform!");
		}
		setCapabilities(capabilities);
	}

	/**
	 * Description: set DesiredCapabilities platform.</BR>
	 * 内容描述：配置WebDriver运行平台配置信息。
	 *
	 * @param capabilities the DesiredCapabilities object.
	 */
	private void setCapabilities(DesiredCapabilities capabilities) throws Exception {
		capabilities.setCapability("ignoreProtectedModeSettings", true);
		capabilities.setCapability("ignoreZoomSetting", true);
		capabilities.setCapability("javascriptEnabled", true);
		capabilities.setCapability("allowAsynchronousJavaScript", true);
		capabilities.setCapability("enableElementCacheCleanup", true);
		capabilities.setCapability("handlesAlerts", true);
		capabilities.setCapability("unexpectedAlertBehaviour", "dismiss");
		capabilities.setCapability("cssSelectorsEnabled", true);
		capabilities.setCapability("takesScreenshot", true);
		capabilities.setCapability("nativeEvents", true);
		capabilities.setCapability("enablePersistentHover", true);
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
}