package com.star.tools.codescan;

/**
 * 扫描汇总指定工作区中工程里所有的java文件、用于测试运行的java文件和测试方法数。
 * 
 * @author 测试仔刘毅
 */

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.star.testdata.string.StringBufferUtils;

public class AutoDevelopmentSchedule {

	private static CountCommitTest tws = new CountCommitTest();
	private static CountTestNGTest ctt;
	private static CountJUnitTest cjt;
	private static StringBufferUtils str = new StringBufferUtils();
	
	private static final String time = str.formatedTime("yyyy-MM-dd");
	private static final String workRoot = "D:\\03_test_develop\\";
	
	private static int countNatualClass = 0;
	private static int countTestClass = 0;
	private static int countTestMethod = 0;
	private static int countTaskClass = 0;
	private static int countTaskMethod = 0;
	private static BufferedReader reader;

	public static void main(String[] args) throws Exception {
		tws.setReadCharSet("UTF-8");
		String fileName = "D:\\02_自动化测试管理\\养老险健康险测试案例汇总" + time + ".xls";
		String collectFile = workRoot + "TestNG_Test_List.csv";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(collectFile), "ISO-8859-1"));
		String line = null;
		int index = 1;

		HSSFWorkbook workbook = new HSSFWorkbook();
		FileOutputStream fileOut = new FileOutputStream(fileName);
		FileInputStream fso = new FileInputStream(fileName);
		Sheet sheet = workbook.createSheet("养老险健康险测试案例汇总");
		sheet.setColumnWidth((short) 0, (short) 4500);
		sheet.setColumnWidth((short) 1, (short) 3500);
		sheet.setColumnWidth((short) 2, (short) 3500);
		sheet.setColumnWidth((short) 3, (short) 4000);
		sheet.setColumnWidth((short) 4, (short) 3500);
		sheet.setColumnWidth((short) 5, (short) 4000);
		Cell cell = null;

		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

		Row row = sheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("system_name");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(1);
		cell.setCellValue("natual-class");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(2);
		cell.setCellValue("test-class");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(3);
		cell.setCellValue("run-class");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(4);
		cell.setCellValue("test-method");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(5);
		cell.setCellValue("run-method");
		cell.setCellStyle(cellStyle);

		while (reader.ready() && null != (line = reader.readLine())) {
			String[] lineContent = line.split(",");
			String projectName = lineContent[0];
			
			Sheet sheetList = workbook.createSheet(projectName + "_case_list");
			sheetList.setColumnWidth((short) 0, (short) 40000);
			
			String frameName = lineContent[1];
			row = sheet.createRow(index);
			String sourceFolder = workRoot + projectName + "\\src";
			tws.fileListReset();
			List<String> natualClasses = tws.testClassFiles(sourceFolder);
			List<String> testClasses = tws.testClass(sourceFolder);
			//tws.fileListReset();
			int classCount = testClasses.size();

			int methodCount = 0;
			for (int j = 0; j < natualClasses.size(); j++) {
				methodCount += tws.countTestMethod(natualClasses.get(j));
			}

			List<String> taskClasses = new ArrayList<String>();
			List<String> classes = null;
			int taskTestCount = 0;
			int taskMethodCount = 0;
			
			if (frameName.equalsIgnoreCase("testng")){
				for (int i = 2; i < lineContent.length; i ++){
					ctt = new CountTestNGTest(workRoot, projectName, lineContent[i]);
					classes = ctt.testNGClasses();
					if (null != lineContent[i] && !lineContent[i].isEmpty() && null != classes){
						taskClasses = str.listDistinctMerge(taskClasses, classes);
					}
				}
				taskTestCount = taskClasses.size();
				if (ctt != null){
					ctt.clearCount();
				}
				for (int n = 0; n < taskTestCount; n ++){
					String testName = taskClasses.get(n);
					if (testName.contains("webtestunit")){
						taskMethodCount += ctt.testNGMethods(testName, sheetList);					
					}
				}				
			}else{
				for (int i = 2; i < lineContent.length; i ++){
					cjt = new CountJUnitTest(workRoot, projectName, lineContent[i]);
					classes = cjt.junitClasses();
					if (null != lineContent[i] && !lineContent[i].isEmpty() && null != classes){
						taskClasses = str.listDistinctMerge(taskClasses, classes);
					}
				}
				taskTestCount = taskClasses.size();
				if (cjt != null){
					cjt.clearCount();
				}
				for (int n = 0; n < taskTestCount; n ++){
					String testName = taskClasses.get(n);
					if (testName.contains("webtestunit")){
						taskMethodCount += cjt.junitMethods(testName, sheetList);					
					}
				}
			}

			countNatualClass += natualClasses.size();
			countTestClass += classCount;
			countTaskClass += taskTestCount;
			countTestMethod += methodCount;
			countTaskMethod += taskMethodCount;

			System.out.println(projectName + ":");
			System.out.println("	natual-class: " + natualClasses.size());
			System.out.println("	test-class: " + classCount);
			System.out.println("	run-class: " + taskTestCount);
			System.out.println("	test-method: " + methodCount);
			System.out.println("	run-method: " + taskMethodCount);
			System.out.println(projectName + "未提交运行的Test:");
			Thread.currentThread().join(100);
			
			List<String> notRunned = str.listMinus(testClasses, taskClasses);
			if (null != notRunned && !notRunned.isEmpty()){
				for (int x = 0; x < notRunned.size(); x ++){
					System.out.println("	" + notRunned.get(x));
				}			
			}else{
				System.out.println("	****************无****************");				
			}

			cell = row.createCell(0);
			cell.setCellType(1);
			cell.setCellValue(projectName);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(1);
			cell.setCellType(0);
			cell.setCellValue(natualClasses.size());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(2);
			cell.setCellType(0);
			cell.setCellValue(classCount);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(3);
			cell.setCellType(0);
			cell.setCellValue(taskTestCount);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(4);
			cell.setCellType(0);
			cell.setCellValue(methodCount);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(5);
			cell.setCellType(0);
			cell.setCellValue(taskMethodCount);
			cell.setCellStyle(cellStyle);
			
			index ++;
		}
		reader.close();

		System.out.println("total:");
		System.out.println("	natual-class: " + countNatualClass);
		System.out.println("	test-class: " + countTestClass);
		System.out.println("	run-class: " + countTaskClass);
		System.out.println("	test-method: " + countTestMethod);
		System.out.println("	run-method: " + countTaskMethod);
		Thread.currentThread().join(100);

		row = sheet.createRow(index);
		cell = row.createCell(0);
		cell.setCellType(1);
		cell.setCellValue("合计");
		cell.setCellStyle(cellStyle);

		cell = row.createCell(1);
		cell.setCellType(0);
		cell.setCellValue(countNatualClass);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(2);
		cell.setCellType(0);
		cell.setCellValue(countTestClass);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(3);
		cell.setCellType(0);
		cell.setCellValue(countTaskClass);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(4);
		cell.setCellType(0);
		cell.setCellValue(countTestMethod);
		cell.setCellStyle(cellStyle);

		cell = row.createCell(5);
		cell.setCellType(0);
		cell.setCellValue(countTaskMethod);
		cell.setCellStyle(cellStyle);
		
		workbook.write(fileOut);
		fileOut.flush();
		fileOut.close();
		fso.close();
	}
}