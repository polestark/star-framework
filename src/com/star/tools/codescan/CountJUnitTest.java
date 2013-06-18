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
import com.star.testdata.string.StringBufferUtils;

public class CountJUnitTest {
	private final ReadXMLDocument xml = new ReadXMLDocument();
	private final StringBufferUtils str = new StringBufferUtils();

	private String workRoot;
	private String systemName;
	private String buildName;
	private String sourceFolder;
	private static int count = 0;

	/**
	 * @param workRoot the project workspace path.
	 * @param systemName the project name.
	 * @param buildName the task file name.
	 */
	public CountJUnitTest(String workRoot, String systemName, String buildName) {
		this.workRoot = workRoot;
		this.systemName = systemName;
		this.buildName = buildName;
		this.sourceFolder = workRoot + systemName + "\\src\\";
	}

	public void clearCount() {
		CountJUnitTest.count = 0;
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
	 * @return the JUnit tests list.
	 * @throws Exception
	 **/
	private List<String> junitTest() throws Exception {
		Document document = xml.loadXMLDocument(workRoot + systemName + "\\" + buildName + ".xml");
		if (null == document) {
			return null;
		}
		List<String> classList = new ArrayList<String>();
		NodeList nodeList = document.getElementsByTagName("test");
		for (int i = 0; i < nodeList.getLength(); i++) {
			String fileName = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
			classList.add(fileName.replace("/", "."));
		}
		return classList;
	}

	/**
	 * read xml file, find test and put to list.
	 * 
	 * @return the JUnit tests list.
	 * @throws Exception
	 **/
	private List<String> junitBatchTest() throws Exception {
		Document document = xml.loadXMLDocument(workRoot + systemName + "\\" + buildName + ".xml");
		if (null == document) {
			return null;
		}
		List<String> classList = new ArrayList<String>();
		NodeList nodeList = document.getElementsByTagName("include");
		for (int i = 0; i < nodeList.getLength(); i++) {
			String batchName = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
			String oldName = batchName;
			if (null != batchName && !batchName.isEmpty() && batchName.contains(".java")) {
				batchName = batchName.replace("/", "~").replace("\\", "~").replace(".java", "");

				if (batchName.contains("*")) {
					String[] folders = batchName.split("~");
					String folderName = folders[0];
					String fileMark = folders[folders.length - 1];
					if (fileMark.equalsIgnoreCase("*")) {
						folderName = batchName.replace("~*", "").replace("~", "\\");
						fileMark = null;
					} else {
						String[] newFolders = batchName.replace("*", "").split("~");
						fileMark = newFolders[folders.length - 1].replace(".java", "");
						for (int k = 1; k < newFolders.length - 1; k++) {
							folderName += "\\" + newFolders[k];
						}
					}
					File[] files = new File(sourceFolder + folderName).listFiles();
					for (int n = 0; n < files.length; n++) {
						String filePath = files[n].getAbsolutePath();
						if (filePath.indexOf(".svn") < 0 && filePath.contains(".java")) {
							filePath = filePath.replace(sourceFolder, "").replace(".java", "")
									.replace("\\", ".");
							String fileName = files[n].getName().replace(".java", "");
							if (null == fileMark || fileName.indexOf(fileMark) >= 0) {
								classList.add(filePath);
							}
						}
					}
				} else {
					classList
							.add(oldName.replace("/", ".").replace("\\", ".").replace(".java", ""));
				}
			}
		}
		return classList;
	}

	/**
	 * Description: find count batchtest and test files
	 * 
	 * @return all the files under the system
	 * @throws Exception
	 */
	public List<String> junitClasses() throws Exception {
		return str.listDistinctMerge(junitBatchTest(), junitTest());
	}

	/**
	 * count the number of test method of specified java file.
	 * 
	 * @param testClass java file name.
	 * @throws Exception
	 */
	public int junitMethods(String testClass, Sheet sheetList) throws Exception {
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