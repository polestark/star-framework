package com.star.frame.tools;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class CountCommitTest{
	
	private final ReadXMLDocument xml = new ReadXMLDocument();
	private static List<String> fileList = new ArrayList<String>();
	
	/**
	 * set the java file reading encoding.
	 * @param encode the charset of the java files.
	 */
	public void setReadCharSet(String encode){
		xml.setReadCharSet("UTF-8");
	}

	/**
	 * get the java file reading encoding.
	 * 
	 * @return the charset of the java files.
	 */
	public String getReadCharSet(){
		return xml.getReadCharSet();
	}

	/**
	 * reset static virible when use recursion method.
	 */
	public void fileListReset(){
		fileList.removeAll(fileList);
	}
	
	/**
	 * get all java files under specified folder and put it into arraylist.
	 * 
	 * @param workFolder the src file folders of the project.
	 * @throws Exception
	 */
	public List<String> testClassFiles(String workFolder) throws Exception{
		File file = new File(workFolder);
		if (file.isDirectory()){
			for (File files : file.listFiles()){
				testClassFiles(files.getAbsolutePath());
			}
		}else{
			if (file.getAbsolutePath().endsWith("java") && file.getAbsolutePath().contains("webtestunit")){
				fileList.add(file.getAbsolutePath());
			}
		}
		return fileList;
	}

	/**
	 * count the number of java files that include test method.
	 * 
	 * @param workFolder the src file folders of the project.
	 * @throws Exception
	 */
	public int countTestClass(String workFolder) throws Exception{
		return testClass(workFolder).size();
	}
	
	public List<String> testClass(String workFolder) throws Exception{
		String eachLine = null;
		fileListReset();
		String className = null;
		List<String> files = testClassFiles(workFolder);
		List<String> tests = new ArrayList<String>();
		int beginIndex = workFolder.length() + 1;
		for (int i = 0; i < files.size(); i++) {
			File file = new File(files.get(i));
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, getReadCharSet());
			BufferedReader reader = new BufferedReader(isr);
			boolean added = false;
			while ((eachLine = reader.readLine()) != null && !added) {
				className = file.getAbsolutePath().replace(".java", "").substring(beginIndex).replace("\\", ".");
				if (eachLine.trim().indexOf("@Test") == 0) {
					added = true;
					tests.add(className);
				}
			}
			reader.close();
			isr.close();
			fis.close();
		}
		return tests;
	}

	/**
	 * count the number of test method of specified java file.
	 * 
	 * @param fileName java file name.
	 * @throws Exception
	 */
	public int countTestMethod(String fileName) throws Exception{
		String eachLine = null;
		int testCount = 0;
		FileInputStream fis = new FileInputStream(new File(fileName));
		InputStreamReader isr = new InputStreamReader(fis, getReadCharSet());
		BufferedReader reader = new BufferedReader(isr);
		while((eachLine = reader.readLine()) != null){
			if (eachLine.trim().indexOf("@Test") == 0) {
				testCount ++;
			}
		}			
		reader.close();
		isr.close();
		fis.close();
		return testCount;
	}
}