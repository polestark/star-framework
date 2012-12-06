package com.star.frame.tools;

/**
 * 扫描汇总指定工作区中工程里所有的java文件、用于测试运行的java文件和测试方法数。
 * 
 * @author 测试仔刘毅
 */

import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.star.testdata.string.StringBufferUtils;

public class ProjectCollection{	
	private static ReadWorkFolder tws = new ReadWorkFolder();
	private static final String time = new StringBufferUtils().formatedTime("yyyy-MM-dd");	
	private static final String projects = "chs_annuity,egis_abbs,egis_channel,egis_cspi," +
						"egis_finance,egis_iprs,egis_nbu,egis_pas,egis_pis,egis_pos,egis_pts," +
						"egis_query,ehis_claim,ehis_hcs,ehis_nbs,ehis_uws,pss_ann";
	private static final String[] project = projects.split(",");

	public static void main(String[] args) throws Exception {
		tws.setReadCharSet("UTF-8");
		String fileName = "D:\\养老险健康险测试案例汇总" + time + ".xls";

		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			FileOutputStream fileOut = new FileOutputStream(fileName);
			FileInputStream fso = new FileInputStream(fileName);
			Sheet sheet = workbook.createSheet("养老险健康险测试案例汇总");
			sheet.setColumnWidth((short) 0, (short) 4500);
			sheet.setColumnWidth((short) 1, (short) 3500);
			sheet.setColumnWidth((short) 2, (short) 3500);
			sheet.setColumnWidth((short) 3, (short) 3500);
			Cell cell = null;
			
			HSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			
			Row row = sheet.createRow(0);
			cell = row.createCell(0);
			cell.setCellValue("系统名");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(1);
			cell.setCellValue("total-class");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(2);
			cell.setCellValue("tests-class");
			cell.setCellStyle(cellStyle);
			cell = row.createCell(3);
			cell.setCellValue("test-method");
			cell.setCellStyle(cellStyle);

			for (int i = 0; i < project.length; i++) {
				row = sheet.createRow(i + 1);
				String sourceFolder = "E:\\Automation\\" + project[i] + "\\src";
				tws.fileListReset();
				List<String> testClass = tws.testClassFiles(sourceFolder);
				tws.fileListReset();
				int classCount = tws.countTestClass(sourceFolder);

				int methodCount = 0;
				for (int j = 0; j < testClass.size(); j++) {
					methodCount += tws.countTestMethod(testClass.get(j));
				}
				System.out.println(project[i] + ":");
				System.out.println("	total-class: " + testClass.size());
				System.out.println("	tests-class: " + classCount);
				System.out.println("	test-method: " + methodCount);
				
				cell = row.createCell(0);
				cell.setCellType(1);
				cell.setCellValue(project[i]);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(1);
				cell.setCellType(0);
				cell.setCellValue(testClass.size());
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(2);
				cell.setCellType(0);
				cell.setCellValue(classCount);
				cell.setCellStyle(cellStyle);
				
				cell = row.createCell(3);
				cell.setCellType(0);
				cell.setCellValue(methodCount);
				cell.setCellStyle(cellStyle);
			}
			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			fso.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}