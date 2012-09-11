package com.star.logging.selenium;

import org.apache.commons.lang.ArrayUtils;

public class TestMetricsBean {
	private long startTimeStamp;
	private long endTimeStamp;
	private static final int REVISION_PREFIX_LENGTH = "$Revision: ".length();
	private long commandsProcessed = 0L;
	private long failedCommands = 0L;
	private long verificationsProcessed = 0L;
	private String userAgent;
	private String seleniumCoreVersion;
	private String seleniumCoreRevision;
	private String seleniumRcVersion;
	private String seleniumRcRevision;
	private String lastFailedCommandMessage;
	String[] commandsExcludedFromLogging = new String[0];

	public long getStartTimeStamp() {
		return this.startTimeStamp;
	}

	public void setStartTimeStamp(long startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public long getEndTimeStamp() {
		return this.endTimeStamp;
	}

	public void setEndTimeStamp(long endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
	}

	public long getCommandsProcessed() {
		return this.commandsProcessed;
	}

	public void setCommandsProcessed(long commandsProcessed) {
		this.commandsProcessed = commandsProcessed;
	}

	public void incCommandsProcessed() {
		this.commandsProcessed += 1L;
	}

	public long getFailedCommands() {
		return this.failedCommands;
	}

	public void setFailedCommands(long failedCommands) {
		this.failedCommands = failedCommands;
	}

	public void incFailedCommands() {
		this.failedCommands += 1L;
	}

	public long getTestDuration() {
		long testDuration = 0L;
		if ((this.startTimeStamp > 0L) && (this.endTimeStamp > this.startTimeStamp)) {
			testDuration = this.endTimeStamp - this.startTimeStamp;
		}
		return testDuration;
	}

	public long getVerificationsProcessed() {
		return this.verificationsProcessed;
	}

	public void setVerificationsProcessed(long verificationsProcessed) {
		this.verificationsProcessed = verificationsProcessed;
	}

	public void incVerificationsProcessed() {
		this.verificationsProcessed += 1L;
	}

	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getSeleniumCoreVersion() {
		return this.seleniumCoreVersion;
	}

	public void setSeleniumCoreVersion(String seleniumCoreVersion) {
		this.seleniumCoreVersion = seleniumCoreVersion;
	}

	public String getSeleniumCoreRevision() {
		return this.seleniumCoreRevision;
	}

	public void setSeleniumCoreRevision(String seleniumCoreRevision) {
		this.seleniumCoreRevision = seleniumCoreRevision;
	}

	public String getSeleniumRcVersion() {
		return this.seleniumRcVersion;
	}

	public void setSeleniumRcVersion(String seleniumRcVersion) {
		this.seleniumRcVersion = seleniumRcVersion;
	}

	public String getSeleniumRcRevision() {
		return this.seleniumRcRevision;
	}

	public void setSeleniumRcRevision(String seleniumRcRevision) {
		this.seleniumRcRevision = seleniumRcRevision;
	}

	public String getLastFailedCommandMessage() {
		return this.lastFailedCommandMessage;
	}

	public void setLastFailedCommandMessage(String lastFailedCommandMessage) {
		this.lastFailedCommandMessage = lastFailedCommandMessage;
	}

	public String getLoggingSeleniumRevision() {
		return "$Revision: 96 $".substring(REVISION_PREFIX_LENGTH, "$Revision: 96 $".length() - 2);
	}

	public String[] getCommandsExcludedFromLogging() {
		return (String[]) (String[]) ArrayUtils.clone(this.commandsExcludedFromLogging);
	}

	public void setCommandsExcludedFromLogging(String[] commandsExcludedFromLogging) {
		this.commandsExcludedFromLogging = ((String[]) (String[]) ArrayUtils
				.clone(commandsExcludedFromLogging));
	}
}