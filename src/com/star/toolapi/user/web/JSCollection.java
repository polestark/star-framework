package com.star.toolapi.user.web;

public enum JSCollection{
	MAXIMIZEWINDOW("if(document.all) { self.moveTo(0, 0); "
					+ "self.resizeTo(screen.availWidth, screen.availHeight); self.focus();}"),
	CLICKBYJAVASCRIPT("return arguments[0].click();"),
	ENSRUEBEFOREALERT("window.alert = function() {}"),
	ENSUREBEFOREWINCLOSE("window.close = function(){ window.opener=null; window.open('','_self'); window.close();}"),
	ENSUREBEFORECONFIRM("window.confirm = function() {return true}"),
	DISMISSBEFORECONFIRM("window.confirm = function() {return false}"),
	ENSUREBEFOREPROMPT("window.prompt = function() {return true}"),
	DISMISBEFOREPROMPT("window.prompt = function() {return false}"),
	MAKEELEMENTUNHIDDEN("arguments[0].style.visibility = 'visible'; arguments[0].style.height = '1px'; " 
					+ "arguments[0].style.width = '1px'; arguments[0].style.opacity = 1");

	private String javaScript;

	private JSCollection(String jsContext) {
		this.javaScript = jsContext;
	}

	public String getName() {
		return this.javaScript;
	}
}