package com.star.testdata.fileio;

/**
 * Excel文件读写操作：
 * 1、使用poi组建，基于poi-3.7.jar；
 * 2、实现对单元格的读写；
 * 3、读取整个sheet页的数据存入List和Map，供测试运行时使用；
 * 4、所有单元格的类型全部强制转换为字符串类型。
 * 
 * @author 测试仔刘毅
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.star.support.config.ParseProperties;
import com.star.logging.frame.LoggingManager;

public class ExcelParseUtils {

	private String fileName;
	private Workbook xlWBook = null;
	private Sheet xlSheet = null;
	private Row xlRow = null;
	private Cell xlCell = null;
	private static final LoggingManager LOG = new LoggingManager(ExcelParseUtils.class.getName());
	private static final ParseProperties PROPERTY = new ParseProperties("config/config.properties");

	/**
	 * class construct with initlize to set filename for excel file operations.
	 * 
	 * @param	fileName the excel file name with whole path
	 */
	public ExcelParseUtils(String filepath, String fileName) {
		this.fileName = filepath + "/" + fileName;
	}

	/**
	 * class construct with initlize to set filename for excel file operations.
	 * 
	 * @param	subFolder the excel file path under public file path, named testdata.xls
	 */
	public ExcelParseUtils(String subFolder) {
		this.fileName = PROPERTY.get("datapath") + subFolder + "testdata.xls";
	}

	/**
	 * write excel sheet, specified value to specified cell.
	 * 
	 * @param	sheetName excel sheet name
	 * @param	row row index which to be changed
	 * @param	col column index which to be changed
	 * @param	value value to be put into cell
	 * @throws	RuntimeException
	 * notice you can use this method to set cell value both in xls and xlsx,
	 *         but when xlsx you must use dom4j.jar, otherwise there is a
	 *         ClassNotFoundException
	 */
	public void setExcelValue(String sheetName, int row, int col, String value) {
		FileOutputStream fileOut = null;
		FileInputStream fso = null;
		try {
			fso = new FileInputStream(fileName);
			xlWBook = WorkbookFactory.create(fso);
			if (xlWBook == null) {
				LOG.error("file [" + fileName + "] does not exist!");
				return;
			}
			xlSheet = xlWBook.getSheet(sheetName);
			if (xlSheet == null) {
				xlSheet = xlWBook.createSheet(sheetName);
			}
			xlRow = xlSheet.getRow(row - 1);
			if (xlRow == null) {
				xlRow = xlSheet.createRow((short) row - 1);
			}
			xlCell = xlRow.getCell(col - 1);
			if (xlCell == null) {
				xlCell = xlRow.createCell(col - 1);
			}
			xlCell.setCellType(1);// set cell type as string
			xlCell.setCellValue(value);
			fileOut = new FileOutputStream(fileName);
			xlWBook.write(fileOut);
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("set excel value failed:" + e.getMessage());
		} finally {
			try {
				if (fileOut != null) {
					fileOut.close();
				}
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException("close FileOutputStream failed:" + e.getMessage());
			}
			try {
				if (fso != null) {
					fso.close();
				}
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException("close FileInputStream failed:" + e.getMessage());
			}
		}
	}

	/**
	 * get excel cell value of specified cell.
	 * 
	 * @param	sheetName excel sheet name
	 * @param	row row index which to be changed
	 * @param	col column index which to be changed
	 * @return	excel cell value string
	 * @throws	RuntimeException
	 * notice you can use this method to get cell value both in xls and xlsx,
	 *         but when xlsx you must use dom4j.jar, otherwise there is a
	 *         ClassNotFoundException
	 */
	public String getExcelValue(String sheetName, int row, int col) {
		String text = null;
		FileInputStream fso = null;
		try {
			fso = new FileInputStream(fileName);
			xlWBook = WorkbookFactory.create(fso);
			if (xlWBook == null) {
				LOG.error("file [" + fileName + "] does not exist!");
				return null;
			}
			xlSheet = xlWBook.getSheet(sheetName);
			if (xlSheet == null) {
				LOG.error("sheet [" + sheetName + "] does not exist!");
				return null;
			}
			xlRow = xlSheet.getRow(row - 1);
			if (xlRow != null) {
				xlCell = xlRow.getCell(col - 1);
				if (xlCell != null) {
					text = xlCell.toString();
				}
			}
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("read excel failed:" + e.getMessage());
		} finally {
			try {
				if (fso != null) {
					fso.close();
				}
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException("close FileInputStream failed:" + e.getMessage());
			}
		}
		return text;
	}

	/**
	 * put data into map.
	 * 
	 * @param	keys map key names
	 * @param	parms map key values
	 * @throws	RuntimeException
	 */
	public Map<String, String> creatMap(List<String> keys, List<String> parms) {
		if (keys.size() != parms.size()){
			LOG.error("incorrect parameters, the size of list not equals!");
			throw new RuntimeException("incorrect parameters, the size of list not equals!");	
		}
		Map<String, String> paraMap = new HashMap<String, String>();				
		for (int i = 0; i < keys.size(); i ++){
			paraMap.put(keys.get(i), parms.get(i));
		}		
		return paraMap;
	}

	/**
	 * read excel Xls and add the result into arraylist.
	 * 
	 * @param	sheetName excel sheet name
	 * @throws	RuntimeException
	 */
	public List<Map<String, String>> excelToList(String sheetName) {
		Row firstxlRow = null;
		FileInputStream fso = null;
		List<Map<String, String>> paraList = new ArrayList<Map<String, String>>();

		try {
			fso = new FileInputStream(fileName);
			xlWBook = WorkbookFactory.create(fso);
			if (xlWBook == null) {
				LOG.error("file [" + fileName + "] does not exist!");
				return null;
			}
			xlSheet = xlWBook.getSheet(sheetName);
			if (xlSheet == null) {
				LOG.error("sheet [" + sheetName + "] does not exist!");
				return null;
			}
			firstxlRow = xlSheet.getRow(xlSheet.getFirstRowNum());
			int firstCell = firstxlRow.getFirstCellNum();
			int lastCell = firstxlRow.getLastCellNum();
			List<String> keyList = new ArrayList<String>();

			for (int cNum = firstCell; cNum < lastCell; cNum++) {
				if (firstxlRow.getCell(cNum).toString() == null) {
					break;
				}
				keyList.add(firstxlRow.getCell(cNum).toString());
			}

			for (int i = xlSheet.getFirstRowNum() + 1; i < xlSheet.getPhysicalNumberOfRows(); i++) {
				xlRow = xlSheet.getRow(i);
				List<String> valueList = new ArrayList<String>();
				if (xlRow == null) {
					break;
				}
				for (int j = firstCell; j < lastCell; j++) {
					xlCell = xlRow.getCell(j);
					if (xlCell == null) {
						valueList.add(null);
						continue;
					}else{
						valueList.add(xlCell.toString());
					}
				}
				paraList.add(creatMap(keyList,valueList));
			}			
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("read excel failed:" + e.getMessage());
		} finally {
			try {
				if (fso != null) {
					fso.close();
				}
			} catch (Exception e) {
				LOG.error(e);
				throw new RuntimeException("close FileInputStream failed:" + e.getMessage());
			}
		}
		return paraList;
	}

	/**
	 * get the specified excel Xls sheet with specified row data into map.
	 * 
	 * @param	sheetName excel sheet name
	 * @param	index index of the row you want to use
	 * @throws	RuntimeException
	 */
	public Map<String, String> excelDataMap(String sheetName, int index) {
		Iterator<Map<String, String>> it = excelToList(sheetName).iterator();
		Map<String, String> paraMap = null;

		for (int i = 0; i < index; i++) {
			if (it.hasNext()) {
				paraMap = (Map<String, String>) it.next();
			}
		}
		return paraMap;
	}

	/**
	 * override the excelDataMapXls method, using default rownum 1.
	 * 
	 * @param	sheetName excel sheet name
	 * @throws	RuntimeException
	 */
	public Map<String, String> excelDataMap(String sheetName) {
		return excelDataMap(sheetName, 1);
	}
}