package com.star.core.webdriver;

import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;

import com.star.tools.ReadConfiguration;

public class WebDriverSettings {
	public final static ReadConfiguration config = new ReadConfiguration(
			"/com/star/core/webdriver/webdirver_config.properties");

	public int MAX_WAIT;// 单步操作超时时间
	
	public int MAX_LOAD_WAIT;// 页面加载超时时间
	
	public int SLEEP_UNIT;// 单次循环思考时间

	public Boolean USE_DRIVERSERVER; // 是否需要使用IEDriverServer模式（与之对应的是IEDriver.dll）
	
	public Boolean SERVER_OUTPUT_ON; // 是否打开server端文本日志
	
	public InternetExplorerDriverLogLevel SERVER_LOG_LEVEL;// server端文本日志级别
	
	public boolean IGNORE_PROTECTED_MODE; // 是否忽略IE保护模式的影响
	
	public boolean IGNORE_ZOOM_SETTING;// 是否忽略IE缩放比例的影响
	
	public boolean JAVASCRIPT_ENABLED;// 是否允许执行JS
	
	public boolean ASYNC_JS_ENABLED;// 是否允许执行异步JS
	
	public boolean ELEMENT_CACHE_CLEAN;// 是否清理对象缓存
	
	public boolean HANDLES_ALERTS;// 是否处理弹出对话框
	
	public String UNEXPECTED_ALERT_BEHAVIOUR;// 对异常（预期之外的）弹出框的处理方式
	
	public boolean CSSS_ELECTORS_ENABLED;// 是否支持CSS定位
	
	public boolean TAKES_SCREENSHOT;// 是否支持浏览器截图
	
	public boolean NATIVE_EVENTS_ENABLED;// 是否支持本地事件：运行时人工干预
	
	public boolean ENABLE_PERSIST_ENTHOVER;// 是否支持鼠标稳定的悬停事件
	
	public boolean REQUIRE_WINDOW_FOCUS;// 是否支持IE始终在任务栏激活且在桌面聚焦
	
	public int BROWSER_ATTACHT_IMEOUT;// 浏览器通讯超时时间

	public WebDriverSettings() {
		this.MAX_WAIT = Integer.parseInt(config.get("STEP_TIMEOUT"));
		this.MAX_LOAD_WAIT = Integer.parseInt(config.get("PAGE_LOAD_TIMEOUT"));
		this.SLEEP_UNIT = Integer.parseInt(config.get("SLEEP_INTERVAL"));
		this.SERVER_LOG_LEVEL = InternetExplorerDriverLogLevel.valueOf(config.get("SERVER_LOG_LEVEL"));
		this.SERVER_OUTPUT_ON = Boolean.parseBoolean(config.get("SERVER_OUTPUT_ON"));
		this.USE_DRIVERSERVER = Boolean.parseBoolean(config.get("USE_DRIVERSERVER"));
		this.IGNORE_PROTECTED_MODE = Boolean.parseBoolean(config.get("IGNORE_PROTECTED_MODE_SETTINGS"));
		this.IGNORE_ZOOM_SETTING = Boolean.parseBoolean(config.get("IGNORE_ZOOM_SETTING"));
		this.JAVASCRIPT_ENABLED = Boolean.parseBoolean(config.get("JAVASCRIPT_ENABLED"));
		this.ASYNC_JS_ENABLED = Boolean.parseBoolean(config.get("ALLOW_ASYNCHRONOUS_JAVASCRIPT"));
		this.ELEMENT_CACHE_CLEAN = Boolean.parseBoolean(config.get("ENABLE_ELEMENT_CACHE_CLEANUP"));
		this.HANDLES_ALERTS = Boolean.parseBoolean(config.get("HANDLES_ALERTS"));
		this.UNEXPECTED_ALERT_BEHAVIOUR = config.get("UNEXPECTED_ALERT_BEHAVIOUR");
		this.CSSS_ELECTORS_ENABLED = Boolean.parseBoolean(config.get("CSS_SELECTORS_ENABLED"));
		this.TAKES_SCREENSHOT = Boolean.parseBoolean(config.get("TAKE_SSCREENSHOT"));
		this.NATIVE_EVENTS_ENABLED = Boolean.parseBoolean(config.get("NATIVE_EVENTS"));
		this.ENABLE_PERSIST_ENTHOVER = Boolean.parseBoolean(config.get("ENABLE_PERSISTENT_HOVER"));
		this.REQUIRE_WINDOW_FOCUS = Boolean.parseBoolean(config.get("REQUIRE_WINDOW_FOCUS"));
		this.BROWSER_ATTACHT_IMEOUT = Integer.parseInt(config.get("BROWSER_ATTACH_TIMEOUT"));
	}
}