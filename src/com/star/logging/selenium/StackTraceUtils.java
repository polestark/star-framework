package com.star.logging.selenium;

public final class StackTraceUtils {
	public static final String LINE_NUMBER_SEPARATOR = "#";

	public static void debugStackTrace() {
		StackTraceElement[] testElements = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : testElements)
			System.err.println(stackTraceElementWithLinenumberAsString(stackTraceElement));
	}

	public static String stackTraceElementWithLinenumberAsString(StackTraceElement stackTraceElement) {
		if (null != stackTraceElement) {
			return stackTraceElement.getClassName() + "#" + stackTraceElement.getLineNumber();
		}
		return "Internal ERROR stackTraceElement should not be null";
	}

	public static boolean isClassName(StackTraceElement stackTraceElement, String wantedClassName) {
		String className = stackTraceElement.getClassName();
		return className.contains(wantedClassName);
	}

	public static StackTraceElement getCurrentCallingClassAsStackTraceElement(
			StackTraceElement[] testElements, String preceedingClassName) {
		boolean found = false;
		StackTraceElement currentCallingClassAsStackTraceElement = null;
		for (StackTraceElement stackTraceElement : testElements) {
			if (found) {
				currentCallingClassAsStackTraceElement = stackTraceElement;
				break;
			}
			if (isClassName(stackTraceElement, preceedingClassName)) {
				found = true;
				currentCallingClassAsStackTraceElement = stackTraceElement;
			}
		}
		return currentCallingClassAsStackTraceElement;
	}

	public static boolean isClassInStackTrace(StackTraceElement[] testElements, String className) {
		boolean result = false;
		for (StackTraceElement stackTraceElement : testElements) {
			if (stackTraceElement.getClassName().endsWith(className)) {
				result = true;
			}
		}
		return result;
	}
}