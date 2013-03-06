package com.star.core.webdriver.user;

public enum JScriptCollection{
	MAXIMIZE_WINDOW("if(document.all) { self.moveTo(0, 0); "
					+ "self.resizeTo(screen.availWidth, screen.availHeight); self.focus();}"),
	CLICK_BY_JAVASCRIPT("return arguments[0].click();"),
	ENSRUE_BEFORE_ALERT("window.alert = function() {}"),
	ENSURE_BEFORE_WINCLOSE("window.close = function(){ window.opener=null; window.open('','_self'); window.close();}"),
	ENSURE_BEFORE_CONFIRM("window.confirm = function() {return true}"),
	DISMISS_BEFORE_CONFIRM("window.confirm = function() {return false}"),
	ENSURE_BEFORE_PROMPT("window.prompt = function() {return true}"),
	DISMISS_BEFORE_PROMPT("window.prompt = function() {return false}"),
	BROWSER_READY_STATUS("return document.readyState"),
	MAKE_ELEMENT_UNHIDDEN("arguments[0].style.visibility = 'visible'; arguments[0].style.height = '1px'; " 
					+ "arguments[0].style.width = '1px'; arguments[0].style.opacity = 1");

	private String javaScript;

	private JScriptCollection(String jsContext) {
		this.javaScript = jsContext;
	}

	public String getValue() {
		return this.javaScript;
	}
}