package com.star.frame.tools;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ReadWorkFolder{
	
	private String charSet = "UTF-8";
	private static List<String> fileList = new ArrayList<String>();

	/**
	 * reset static virible when use recursion method.
	 */
	public void fileListReset(){
		fileList.removeAll(fileList);
	}

	/**
	 * set the java file reading encoding.
	 * @param encode the charset of the java files.
	 */
	public void setReadCharSet(String encode){
		this.charSet = encode;
	}

	/**
	 * get the java file reading encoding.
	 * @return the charset of the java files.
	 */
	public String getReadCharSet(){
		return this.charSet;
	}
	
	/**
	 * get all java files under specified folder and put it into arraylist.
	 * @param workFolder the src file folders of the project.
	 */
	public List<String> testClassFiles(String workFolder){
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
	 * @param workFolder the src file folders of the project.
	 */
	public int countTestClass(String workFolder) {
		int classCount = 0;
		String eachLine = null;
		List<String> files = testClassFiles(workFolder);
		try {
			for (int i = 0; i < files.size(); i++) {
				FileInputStream fis = new FileInputStream(new File(files.get(i)));
				InputStreamReader isr = new InputStreamReader(fis, charSet);
				BufferedReader reader = new BufferedReader(isr);
				boolean added = false;
				while ((eachLine = reader.readLine()) != null && !added) {
					if (eachLine.trim().indexOf("@Test") == 0) {
						classCount++;
						added = true;
					}
				}
				reader.close();
				isr.close();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classCount;
	}

	/**
	 * count the number of test method of specified java file.
	 * @param fileName java file name.
	 */
	public int countTestMethod(String fileName){
		String eachLine = null;
		int testCount = 0;
		try{
			FileInputStream fis = new FileInputStream(new File(fileName));
			InputStreamReader isr = new InputStreamReader(fis, charSet);
			BufferedReader reader = new BufferedReader(isr);
			while((eachLine = reader.readLine()) != null){
				if (eachLine.trim().indexOf("@Test") == 0) {
					testCount ++;
				}
			}			
			reader.close();
			isr.close();
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return testCount;
	}
}