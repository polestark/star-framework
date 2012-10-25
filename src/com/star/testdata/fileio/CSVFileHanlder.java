package com.star.testdata.fileio;

import java.io.File;
import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import com.star.logging.frame.LoggingManager;

public class CSVFileHanlder {

	private static final LoggingManager LOG = new LoggingManager(ExcelParseUtils.class.getName());
	private static File file;

	/**
	 * class construct with initlize to set filename for csv file operations.
	 * 
	 * @param	name the file name to be read or write
	 */
	public CSVFileHanlder(String fileName){
		CSVFileHanlder.file = new File(fileName);
	}

	/**
	 * read specified row and column value of csv file
	 * 
	 * @param	row the row index of the file
	 * @param	col the column index of the file
	 * 
	 * @throws	RuntimeException
	 */
	public String readCSVCellValue(int row, int col) {
		String line = null;
		String[] chars = null;

		try {
			int i = 1;
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			while (buffer.ready() && (line = buffer.readLine()) != null) {
				chars = line.split(",");
				if (row == i) {
					return chars[col - 1];
				}
				i++;
			}
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * read specified line value of csv file
	 * 
	 * @param	row the row index of the file
	 * 
	 * @throws	RuntimeException
	 */
	public String readCSVLineValue(int row) {
		String line = null;
		try {
			int i = 1;
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			while (buffer.ready() && (line = buffer.readLine()) != null) {
				if (row == i) {
					return line.toString();
				}
				i++;
			}
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * read the whole content to list of the csv file name.
	 * 
	 * @throws RuntimeException
	 */
	public List<String> readCSVToList() {
		String line = null;
		List<String> csvList = new ArrayList<String>();

		try {
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			while (buffer.ready() && (line = buffer.readLine()) != null) {
				csvList.add(line.toString());
			}
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
		return csvList;
	}

	/**
	 * read the whole content to string of the csv file name.
	 */
	public String readCSVText() {
		return readCSVToList().toString();
	}

	/**
	 * get the row count of the csv file name.
	 */
	public int csvRowCount(){
		return readCSVToList().size();
	}

	/**
	 * get the column count of the csv file name.
	 */
	public int csvColumnCount() {
		List<String> content = readCSVToList();
		if (content.toString().equals("[]")) {
			return 0;
		} else {
			if (content.get(0).toString().contains(",")) {
				return content.get(0).toString().split(",").length;
			} else if (content.get(0).toString().trim().length() != 0) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * parse the data list and put it to the csv file, for no append.
	 * 
	 * @param dataList data list to be put into the csv file
	 * 
	 * @throws RuntimeException
	 */
	public void putListToCSV(List<String> dataList){
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
			for (int i = 0; i < dataList.size(); i ++){
				buffer.write(dataList.get(i));
				buffer.newLine();
			}
			buffer.flush();
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * parse the data list and put it to the csv file, for append. 
	 * 
	 * @param	value the string text to be put into the csv file
	 * @param	row line number of the csv file to be modified
	 * 
	 * @throws	RuntimeException
	 */
	public void putLineToCSV(String value, int row) {
		List<String> original = readCSVToList();
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
			for (int i = 0; i < original.size(); i++) {
				if (i != row - 1) {
					buffer.write(original.get(i));
				} else {
					buffer.write(value);
				}
				buffer.newLine();
			}
			buffer.flush();
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * parse the data list and put it to the csv file, for append. 
	 * 
	 * @param	cellValue the string text to be put into the csv file
	 * @param	row line number of the csv file
	 * @param	col column number of the csv file
	 * 
	 * @throws	RuntimeException
	 */
	public void putValueToCSV(String cellValue, int row, int col){
		List<String> original = readCSVToList();
		StringBuffer sb = new StringBuffer();
		String [] text = null;
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
			for (int i = 0; i < original.size(); i++) {
				if (i != row - 1) {
					buffer.write(original.get(i));
				} else {
					text = original.get(i).split(",");
					for (int j = 0; j < text.length; j ++){
						if (j != col -1){
							if (j == text.length - 1){
								sb.append(text[j]);
							}else {
								sb.append(text[j]);
								sb.append(",");
							}							
						}else{
							if (j == text.length - 1){
								sb.append(cellValue);
							}else {
								sb.append(cellValue);
								sb.append(",");
							}
						}
					}
					buffer.write(sb.toString());
				}
				buffer.newLine();
			}
			buffer.flush();
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * append text to csv file, on the end of the file. 
	 * 
	 * @param	text text to append to the csv file
	 * 
	 * @throws	RuntimeException
	 */
	public void appendLineToCSV(String text){
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true));
			buffer.newLine();
			buffer.write(text);
			buffer.flush();
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}

	/**
	 * parse the data list and put it to the csv file, for append. 
	 * 
	 * @param	dataList data list to be put into the csv file
	 * 
	 * @throws	RuntimeException
	 */
	public void appendListToCSV(List<String> dataList){
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true));
			for (int i = 0; i < dataList.size(); i ++){
				buffer.newLine();
				buffer.write(dataList.get(i));
			}
			buffer.flush();
			buffer.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}		
	}
}