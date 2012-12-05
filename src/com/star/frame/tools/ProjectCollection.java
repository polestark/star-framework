package com.star.frame.tools;

/**
 * 扫描汇总指定工作区中工程里所有的java文件、用于测试运行的java文件和测试方法数。
 * 
 * @author 测试仔刘毅
 */

import java.util.List;

public class ProjectCollection{	
	private static ReadWorkFolder tws = new ReadWorkFolder();

	public static void main(String[] args) throws Exception {
		tws.setReadCharSet("UTF-8");
		final String projects = "star-framework,STAR";
		final String[] project = projects.split(",");
		
		for (int i = 0; i < project.length; i++) {
			String sourceFolder = "E:\\Automation\\" + project[i] + "\\src";
			tws.circleReset();
			List<String> testClass = tws.testClassFiles(sourceFolder);
			tws.circleReset();
			int classCount = tws.countTestClass(sourceFolder);

			int methodCount = 0;
			for (int j = 0; j < testClass.size(); j++) {
				methodCount += tws.countTestMethod(testClass.get(j));
			}
			System.out.println(project[i] + ":");
			System.out.println("	total class: " + testClass.size());
			System.out.println("	test class: " + classCount);
			System.out.println("	test method: " + methodCount);
		}
	}
}
