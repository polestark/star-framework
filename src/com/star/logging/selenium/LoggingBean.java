package com.star.logging.selenium;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

public class LoggingBean {
	private String commandName = "";
	private String[] args;
	private String result = "";

	private String callingClass = "";
	private boolean commandSuccessful;
	private boolean waitInvolved;
	private long cmdStartMillis;
	private long cmdEndMillis;
	private String sourceMethod;
	private boolean excludeFromLogging = false;

	private long waitDeltaMillis = 0L;

	private List<LoggingBean> children = new ArrayList<LoggingBean>();

	public void addChild(LoggingBean loggingBean) {
		this.children.add(loggingBean);
	}

	public List<LoggingBean> getChildren() {
		return this.children;
	}

	public boolean hasChildren() {
		return getChildren().size() > 0;
	}

	public String getCommandName() {
		return this.commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String[] getArgs() {
		return (String[]) (String[]) ArrayUtils.clone(this.args);
	}

	public void setArgs(String[] args) {
		this.args = ((String[]) ArrayUtils.clone(args));
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCallingClass() {
		return this.callingClass;
	}

	public void setCallingClass(String callingClass) {
		this.callingClass = callingClass;
	}

	public boolean isCommandSuccessful() {
		return this.commandSuccessful;
	}

	public void setCommandSuccessful(boolean commandSuccessful) {
		this.commandSuccessful = commandSuccessful;
	}

	public String getSrcResult() {
		String[] results = this.result.split(",");
		String srcResult = "";
		if (results.length > 0) {
			srcResult = results[0];
		}
		return srcResult;
	}

	public String getSelResult() {
		int firstCommaIndex = this.result.indexOf(",");
		return this.result.substring(firstCommaIndex + 1);
	}

	public String toString() {
		return "commandName=" + this.commandName + ", args=" + ArrayUtils.toString(this.args);
	}

	public long getCmdStartMillis() {
		return this.cmdStartMillis;
	}

	public void setCmdStartMillis(long cmdStartMillis) {
		this.cmdStartMillis = cmdStartMillis;
	}

	public long getCmdEndMillis() {
		return this.cmdEndMillis;
	}

	public void setCmdEndMillis(long cmdEndMillis) {
		this.cmdEndMillis = cmdEndMillis;
	}

	public boolean isWaitInvolved() {
		return this.waitInvolved;
	}

	public void setWaitInvolved(boolean waitInvolved) {
		this.waitInvolved = waitInvolved;
	}

	public String getSourceMethod() {
		return this.sourceMethod;
	}

	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	public long getDeltaMillis() {
		if (getWaitDeltaMillis() > 0L) {
			return getWaitDeltaMillis();
		}
		return this.cmdEndMillis - this.cmdStartMillis;
	}

	public boolean isExcludeFromLogging() {
		return this.excludeFromLogging;
	}

	public void setExcludeFromLogging(boolean excludeFromLogging) {
		this.excludeFromLogging = excludeFromLogging;
	}

	public long getWaitDeltaMillis() {
		return this.waitDeltaMillis;
	}

	public void setWaitDeltaMillis(long waitDeltaMillis) {
		this.waitDeltaMillis = waitDeltaMillis;
	}
}