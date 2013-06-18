package com.star.tools.codescan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.star.tools.ReadXMLDocument;

public class CountTestNGTest {
	private final ReadXMLDocument xml = new ReadXMLDocument();

	private String workRoot;
	private String systemName;
	private String taskName;
	private String sourceFolder;
	private static int count = 0;

	/**
	 * @param workRoot the project workspace path.
	 * @param systemName the project name.
	 * @param taskName the task file name.
	 */
	public CountTestNGTest(String workRoot, String systemName, String taskName) {
		this.workRoot = workRoot;
		this.systemName = systemName;
		this.taskName = taskName;
		this.sourceFolder = workRoot + systemName + "\\src\\";
	}

	public void clearCount() {
		CountTestNGTest.count = 0;
	}

	/**
	 * set the java file reading encoding.
	 * 
	 * @param encode the charset of the java files.
	 */
	public void setReadCharSet(String encode) {
		xml.setReadCharSet("UTF-8");
	}

	/**
	 * get the java file reading encoding.
	 * 
	 * @return the charset of the java files.
	 */
	public String getReadCharSet() {
		return xml.getReadCharSet();
	}

	/**
	 * read xml file, find test and put to list.
	 * 
	 * @return the TestNG tests list.
	 * @throws Exception
	 **/
	public List<String> testNGClasses() throws Exception {
		Document document = xml.loadXMLDocument(workRoot + systemName + "\\task\\" + taskName
				+ ".xml");
		if (null == document) {
			return null;
		}
		List<String> classList = new ArrayList<String>();
		NodeList nodeList = document.getElementsByTagName("class");
		for (int i = 0; i < nodeList.getLength(); i++) {
			classList.add(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
		}
		return classList;
	}

	/**
	 * count the number of test method of specified java file.
	 * 
	 * @param testClass java file name.
	 * @throws Exception
	 */
	public int testNGMethods(String testClass, Sheet sheetList) throws Exception {
		if (!new File(sourceFolder + testClass.replace(".", "\\") + ".java").exists()) {
			System.err.println("the file: 【" + testClass + "】 does not exist!");
			return 0;
		}
		String eachLine = null;
		int testCount = 0;
		String fileName = sourceFolder + testClass.replace(".", "\\") + ".java";
		FileInputStream fis = new FileInputStream(new File(fileName));
		InputStreamReader isr = new InputStreamReader(fis, getReadCharSet());
		BufferedReader reader = new BufferedReader(isr);
		String testName = null;

		while ((eachLine = reader.readLine()) != null) {
			if (eachLine.trim().indexOf("@Test") == 0) {
				testCount++;
				eachLine = reader.readLine();
				while (eachLine == null) {
					eachLine = reader.readLine();
				}
				if (eachLine.indexOf("public") > 0) {
					eachLine = eachLine.substring(eachLine.indexOf("public") + 7);
					eachLine = eachLine.replace("	", "~").replace(" ", "~").split("~")[1];
					if (eachLine.indexOf("(") > 0) {
						testName = testClass + "." + eachLine.substring(0, eachLine.indexOf("("));
					} else {
						testName = testClass + "." + eachLine;
					}
				}
				if (testName != null) {
					Row row = sheetList.createRow(count);
					Cell cell = row.createCell(0);
					cell.setCellValue(testName);
					count++;
				}
			}
		}
		reader.close();
		isr.close();
		fis.close();
		return testCount;
	}
}