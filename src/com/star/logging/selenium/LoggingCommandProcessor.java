package com.star.logging.selenium;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

public class LoggingCommandProcessor implements CommandProcessor {
	static final String AUTO_SCREENSHOT_WAIT_TIMEOUT_FILENAME_PART = "WaitTimeout";
	private CommandProcessor realCommandProcessor;
	private ResultsFormatter formatter;
	final TestMetricsBean seleniumTestMetrics = new TestMetricsBean();
	static final String SELENIUM_RC_OK_RESULT = "OK";
	static final String SELENIUM_RC_OK_RESULT_PREFIX_WITH_COMMA = "OK,";
	static final String SELENIUM_CORE_BOOLEAN_RESULT_TRUE = "true";
	static final String SELENIUM_CORE_BOOLEAN_RESULT_FALSE = "false";
	static final String WAIT_CLASS_NAME = "Wait";
	boolean logMethodsAsComments = true;

	String[] excludeCommandsFromLogging = { "getHtmlSource" };

	List<LoggingBean> loggingEventsQueue = new ArrayList<LoggingBean>();

	private LoggingNotifier callbackNotifier = null;
	private Object callbackInstance = null;

	public LoggingCommandProcessor(CommandProcessor commandProcessor, ResultsFormatter myFormatter) {
		this.formatter = myFormatter;
		this.realCommandProcessor = commandProcessor;
	}

	public LoggingCommandProcessor(String serverHost, int serverPort, String browserStartCommand,
			String browserUrl, ResultsFormatter myFormatter) {
		this.formatter = myFormatter;
		this.realCommandProcessor = new HttpCommandProcessor(serverHost, serverPort, browserStartCommand,
				browserUrl);
	}

	public String doCommand(String commandName, String[] args) {
		String result = "";
		long cmdStartMillis = System.currentTimeMillis();
		if (SeleniumExtensions.COMMAND_EXTENSION_LOG_COMMENT.getName().equals(commandName)) {
			String comment = args[0] != null ? args[0] : "";
			String extraInfo = "";
			if (args.length > 1) {
				extraInfo = args[1] != null ? args[1] : "";
			}
			logComment(comment, extraInfo, cmdStartMillis);
		} else if (SeleniumExtensions.COMMAND_EXTENSION_LOG_AUTO_SCREENSHOT.getName().equals(commandName)) {
			String baseName = args[0] != null ? args[0] : "";
			doAutomaticScreenshot(baseName);
		} else if (SeleniumExtensions.COMMAND_EXTENSION_LOG_ASSERTION.getName().equals(commandName)) {
			String[] loggingArgs = { args[1], args[2] };
			doExceptionLogging(args[0], loggingArgs, "", null, cmdStartMillis);
		} else if (SeleniumExtensions.COMMAND_EXTENSION_LOG_RESOURCE.getName().equals(commandName)) {
			result = "OK";
			long now = System.currentTimeMillis();
			LoggingBean b = new LoggingBean();
			b.setCallingClass(getRealCallingClassWithLineNumberAsString(getCurrentCallingClassAsStackTraceElement()));
			b.setWaitInvolved(isWaitInvolved());
			b.setSourceMethod("RESOURCE");
			b.setCommandName("captureScreenshot");
			b.setResult(result);
			b.setArgs(args);
			b.setCommandSuccessful(true);
			b.setCmdStartMillis(now);
			b.setCmdEndMillis(now);
			this.loggingEventsQueue.add(b);
		} else {
			this.seleniumTestMetrics.incCommandsProcessed();
			try {
				result = this.realCommandProcessor.doCommand(commandName, args);
			} catch (RuntimeException e) {
				doExceptionLogging(commandName, args, "", e, cmdStartMillis);
				throw e;
			}
			doLogging(commandName, args, result, cmdStartMillis);
		}
		return result;
	}

	public boolean getBoolean(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		this.seleniumTestMetrics.incCommandsProcessed();
		this.seleniumTestMetrics.incVerificationsProcessed();
		boolean result = false;
		try {
			result = this.realCommandProcessor.getBoolean(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + result, cmdStartMillis);
		return result;
	}

	public boolean[] getBooleanArray(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		this.seleniumTestMetrics.incCommandsProcessed();
		boolean[] results;
		try {
			results = this.realCommandProcessor.getBooleanArray(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + ArrayUtils.toString(results), cmdStartMillis);

		return results;
	}

	public Number getNumber(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		this.seleniumTestMetrics.incCommandsProcessed();
		Number result;
		try {
			result = this.realCommandProcessor.getNumber(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + result, cmdStartMillis);
		return result;
	}

	public Number[] getNumberArray(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		this.seleniumTestMetrics.incCommandsProcessed();
		Number[] results;
		try {
			results = this.realCommandProcessor.getNumberArray(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + ArrayUtils.toString(results), cmdStartMillis);

		return results;
	}

	public String getString(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		this.seleniumTestMetrics.incCommandsProcessed();
		String result;
		try {
			result = this.realCommandProcessor.getString(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + ArrayUtils.toString(result), cmdStartMillis);

		return result;
	}

	public String[] getStringArray(String commandName, String[] args) {
		long cmdStartMillis = System.currentTimeMillis();
		String[] results;
		try {
			results = this.realCommandProcessor.getStringArray(commandName, args);
		} catch (RuntimeException e) {
			doExceptionLogging(commandName, args, "", e, cmdStartMillis);
			throw e;
		}
		doLogging(commandName, args, "OK," + ArrayUtils.toString(results), cmdStartMillis);

		return results;
	}

	public void start() {
		this.realCommandProcessor.start();
		this.seleniumTestMetrics.setStartTimeStamp(System.currentTimeMillis());
		// logExecutionEnvironment();
	}

	public void stop() {
		seleniumTestFinished();
		this.realCommandProcessor.stop();
		this.seleniumTestMetrics.setCommandsExcludedFromLogging(this.excludeCommandsFromLogging);

		new EventQueuePost(this.formatter, this.loggingEventsQueue, this.seleniumTestMetrics,
				this.logMethodsAsComments);
	}

	public String[] getExcludedCommands() {
		return (String[]) this.excludeCommandsFromLogging.clone();
	}

	public void setExcludedCommands(String[] excludedCommands) {
		this.excludeCommandsFromLogging = ((String[]) excludedCommands.clone());
	}

	public static LoggingBean presetLoggingBean(String commandName, String[] args, String result,
			long cmdStartMillis, long cmdEndMillis) {
		LoggingBean loggingBean = new LoggingBean();
		loggingBean.setCommandName(commandName);
		loggingBean.setArgs(args);
		loggingBean.setResult(result);
		loggingBean.setCmdStartMillis(cmdStartMillis);
		loggingBean.setCmdEndMillis(cmdEndMillis);
		return loggingBean;
	}

	void logExecutionEnvironment() {
		String userAgent = getEvalNoException("navigator.userAgent");
		this.seleniumTestMetrics.setUserAgent(userAgent);

		String seleniumCoreVersion = getEvalNoException("window.top.Selenium.coreVersion");
		this.seleniumTestMetrics.setSeleniumCoreVersion(seleniumCoreVersion);
		String seleniumCoreRevision = getEvalNoException("window.top.Selenium.coreRevision");
		this.seleniumTestMetrics.setSeleniumCoreRevision(seleniumCoreRevision);

		String seleniumRcVersion = getEvalNoException("window.top.Selenium.rcVersion");
		this.seleniumTestMetrics.setSeleniumRcVersion(seleniumRcVersion);
		String seleniumRcRevision = getEvalNoException("window.top.Selenium.rcRevision");
		this.seleniumTestMetrics.setSeleniumRcRevision(seleniumRcRevision);
	}

	String getEvalNoException(String jsExpr) {
		String propertyValue;
		try {
			propertyValue = this.realCommandProcessor.getString("getEval", new String[] { jsExpr });
		} catch (Exception exc) {
			propertyValue = "UNKNOWN";
		}
		return propertyValue;
	}

	void doExceptionLogging(String commandName, String[] args, String result, Throwable exception,
			long cmdStartMillis) {
		Boolean screenshot = Boolean.valueOf(true);
		if (null != this.callbackNotifier) {
			screenshot = this.callbackNotifier.errorLogging(this.callbackInstance, commandName, args, result,
					exception, cmdStartMillis);
		}
		if (screenshot.booleanValue())
			doAutomaticScreenshot("Error");
		String errorMessage;
		String resultContent;
		if (null != exception) {
			resultContent = "ERROR," + result + " " + exception.getClass().getName() + " - "
					+ exception.getMessage();
			errorMessage = exception.getMessage();
		} else {
			resultContent = "ERROR," + result;
			if (args.length > 0)
				errorMessage = "ERROR: " + args[0];
			else {
				errorMessage = "INTERNAL ERROR: real error-msg could not be determined";
			}
		}
		doLogging(commandName, args, resultContent, cmdStartMillis);
		this.seleniumTestMetrics.incFailedCommands();
		this.seleniumTestMetrics.setLastFailedCommandMessage(errorMessage);
	}

	void doLogging(String commandName, String[] args, String result, long cmdStartMillis) {
		LoggingBean currentCommand = presetLoggingBean(commandName, args, result, cmdStartMillis,
				System.currentTimeMillis());

		currentCommand.setExcludeFromLogging(isCommandExcludedFromLogging(commandName));
		currentCommand
				.setCallingClass(getRealCallingClassWithLineNumberAsString(getCurrentCallingClassAsStackTraceElement()));
		currentCommand.setWaitInvolved(isWaitInvolved());
		String sourceMethodName = "unknown";
		StackTraceElement classOrNull = getCurrentCallingClassAsStackTraceElement();
		if (null != classOrNull) {
			sourceMethodName = classOrNull.getMethodName();
		}
		currentCommand.setSourceMethod(sourceMethodName);
		this.loggingEventsQueue.add(currentCommand);
	}

	void logComment(String comment, String extraInfo, long cmdStartMillis) {
		doLogging(SeleniumExtensions.COMMAND_EXTENSION_LOG_COMMENT.getName(), new String[] { comment,
				extraInfo }, "", cmdStartMillis);
	}

	boolean isCommandExcludedFromLogging(String commandName) {
		return Arrays.asList(this.excludeCommandsFromLogging).contains(commandName);
	}

	StackTraceElement getCurrentCallingClassAsStackTraceElement() {
		return StackTraceUtils.getCurrentCallingClassAsStackTraceElement(Thread.currentThread()
				.getStackTrace(), "DefaultSelenium");
	}

	String getRealCallingClassWithLineNumberAsString(StackTraceElement currentCallingClassAsStackTraceElement) {
		return StackTraceUtils
				.stackTraceElementWithLinenumberAsString(currentCallingClassAsStackTraceElement);
	}

	boolean isWaitInvolved() {
		return StackTraceUtils.isClassInStackTrace(Thread.currentThread().getStackTrace(), ".Wait");
	}

	void seleniumTestFinished() {
		this.seleniumTestMetrics.setEndTimeStamp(System.currentTimeMillis());

		if (this.loggingEventsQueue.size() > 0) {
			LoggingBean lastBeanInQueue = (LoggingBean) this.loggingEventsQueue.get(this.loggingEventsQueue
					.size() - 1);
			if (lastBeanInQueue.isWaitInvolved()) {
				lastBeanInQueue.setResult("ERROR,wait timed out");
				doAutomaticScreenshot("WaitTimeout");
				this.seleniumTestMetrics.setLastFailedCommandMessage(lastBeanInQueue.getResult());
				this.seleniumTestMetrics.incFailedCommands();
			}
		}
	}

	void doAutomaticScreenshot(String baseFileName) {
		String autoScreenshotFullPath = this.formatter.generateFilenameForAutomaticScreenshot(baseFileName);
		Boolean internal = Boolean.valueOf(true);
		if (null != this.callbackNotifier) {
			String pathFile = this.formatter.generateFilenameForAutomaticScreenshot("WaitTimeout");
			internal = this.callbackNotifier.makeScreenshot(this.callbackInstance, pathFile);
		}
		if (internal.booleanValue())
			doCommand("captureScreenshot", new String[] { autoScreenshotFullPath });
	}

	public boolean isTestFailed() {
		return this.seleniumTestMetrics.getFailedCommands() > 0L;
	}

	public void setCallbackNotifier(LoggingNotifier callbackLoggingNotifier, Object callbackLoggingInstance) {
		this.callbackNotifier = callbackLoggingNotifier;
		this.callbackInstance = callbackLoggingInstance;
	}

	public boolean isLogMethodsAsComments() {
		return this.logMethodsAsComments;
	}

	public void setLogMethodsAsComments(boolean logMethodsAsComments) {
		this.logMethodsAsComments = logMethodsAsComments;
	}

	@Override
	public String getRemoteControlServerLocation() {
		return null;
	}

	@Override
	public void setExtensionJs(String extensionJs) {
	}

	@Override
	public void start(String optionsString) {
	}

	@Override
	public void start(Object optionsObject) {
	}
}