package com.star.logging.selenium;

import java.util.List;
import org.apache.commons.lang.StringUtils;

public class EventQueuePost {
	private ResultsFormatter formatter;
	boolean logMethodsAsComments = true;
	@SuppressWarnings("unused")
	private static final String WAIT_PREFIX = "(Wait)";

	public EventQueuePost(ResultsFormatter outputFormatter, List<LoggingBean> loggingEventsQueue,
			TestMetricsBean seleniumTestMetrics, boolean logMethodsAsComments) {
		this.formatter = outputFormatter;
		this.logMethodsAsComments = logMethodsAsComments;
		formatAllGatheredInformations(loggingEventsQueue, seleniumTestMetrics);
	}

	void formatAndOutputCommand(LoggingBean loggingBean) {
		if (SeleniumExtensions.COMMAND_EXTENSION_LOG_COMMENT.getName().equals(
				loggingBean.getCommandName())) {
			this.formatter.commentLogEvent(loggingBean);
			return;
		}

		boolean srcResult = false;
		String result = loggingBean.getResult();

		if (result.startsWith("OK")) {
			if ((result.endsWith("true")) || (result.endsWith("false"))) {
				loggingBean.setCommandSuccessful(result.endsWith("true"));
				this.formatter.booleanCommandLogEvent(loggingBean);
				return;
			}
			srcResult = true;
		}

		loggingBean.setCommandSuccessful(srcResult);
		this.formatter.commandLogEvent(loggingBean);
	}

	void formatAllGatheredInformations(List<LoggingBean> loggingEventsQueue,
			TestMetricsBean seleniumTestMetrics) {
		this.formatter.headerLogEvent(seleniumTestMetrics);

		boolean insideWait = false;
		long startWaitMillis = 0L;
		LoggingBean lastWaitLoggingEvent = null;
		String currentMethodName = "";
		LoggingBean rootLoggingBean = new LoggingBean();
		boolean completeResult = true;
		for (LoggingBean currentLoggingEvent : loggingEventsQueue) {
			if (currentLoggingEvent.isWaitInvolved()) {
				lastWaitLoggingEvent = currentLoggingEvent;
				if (!insideWait) {
					insideWait = true;
					startWaitMillis = currentLoggingEvent.getCmdStartMillis();
				}
			} else {
				if (insideWait) {
					insideWait = false;
					lastWaitLoggingEvent.setWaitDeltaMillis(currentLoggingEvent.getCmdEndMillis()
							- startWaitMillis);
					startWaitMillis = 0L;

					String s = lastWaitLoggingEvent.getCommandName();
					if (!s.startsWith("(Wait)")) {
						lastWaitLoggingEvent.setCommandName("(Wait)" + s);
					}
					formatAndOutputCommand(lastWaitLoggingEvent);
				}
				if (!currentLoggingEvent.getSourceMethod().equals(currentMethodName)) {
					if (StringUtils.isNotEmpty(currentMethodName)) {
						postProcessMethod(completeResult, rootLoggingBean, currentMethodName);
						completeResult = true;
					}

					currentMethodName = currentLoggingEvent.getSourceMethod();
					rootLoggingBean = new LoggingBean();
					rootLoggingBean.setCallingClass(currentLoggingEvent.getCallingClass());
					logNewMethodEntered(currentMethodName, rootLoggingBean);
				}
				formatAndOutputCommand(currentLoggingEvent);
				rootLoggingBean.addChild(currentLoggingEvent);
			}
			completeResult = (completeResult) && (currentLoggingEvent.isCommandSuccessful());
		}

		postProcessMethod(completeResult, rootLoggingBean, currentMethodName);
		this.formatter.footerLogEvent();
	}

	void postProcessMethod(boolean completeMethodResult, LoggingBean currentLoggingBean,
			String currentMethod) {
		currentLoggingBean.setCommandSuccessful(completeMethodResult);
		currentLoggingBean.setArgs(new String[] { "executing "
				+ currentLoggingBean.getCallingClass() + "::" + currentMethod });
		this.formatter.methodLogEvent(currentLoggingBean);
	}

	void logNewMethodEntered(String currentMethodName, LoggingBean loggingBean) {
		if (this.logMethodsAsComments) {
			loggingBean.setCommandName(SeleniumExtensions.COMMAND_EXTENSION_LOG_COMMENT.getName());
			loggingBean.setArgs(new String[] { "executing " + currentMethodName + "()" });
			formatAndOutputCommand(loggingBean);
		}
	}
}