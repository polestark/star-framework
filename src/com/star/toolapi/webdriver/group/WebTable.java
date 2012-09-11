package com.star.toolapi.webdriver.group;

/**
 * WebTable测试类：
 * 1、rowCount--获取指定WebTable的行数；
 * 2、colCount--获取指定WebTable指定行的列数；
 * 3、childItem--获取WebTable指定行、列中指定类型的元素，同类元素如有多个则需要以序号标注，
 * 	    元素类型包含：cell/weblist/webedit/webcheckbox/webbutton/link/image等，可自行扩展；
 * 4、cellText--获取单元格的文本内容。
 * 
 * @author 测试仔刘毅
 */

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class WebTable{
	
	private static By tabBy = null;
	private static List<WebElement> tabRows = null;
	
	/**
	 * construct with parameters initialize
	 * 
	 * @param	driver	the WebDriver instance
	 * @param	tabFinder	the By locator of the table
	 */
	public WebTable(WebDriver driver, By tabFinder){
		tabBy = tabFinder;
		tabRows = driver.findElement(tabBy).findElements(By.tagName("tr"));
	}
	
	/**
	 * get row count of a webtable
	 * 
	 * @return the row count of the table
	 */
	public int rowCount() {
		return tabRows.size();
	}

	/**
	 * get column count of a specified webtable row.
	 * 
	 * @param rowNum row index of your table to count
	 * @return the column count of the row in table
	 */
	public int colCount(int rowNum) {
		return tabRows.get(rowNum - 1).findElements(By.tagName("td")).size();
	}

	/**
	 * get the element in the table cell by row and col index.
	 * 
	 * @param row row index of the table.
	 * @param col column index of the table.
	 * @param type the element type, such as "img"/"a"/"input"...
	 * @param index element index in the specified cell, begins with 1.
	 * @return the table cell WebElement
	 */
	public WebElement childItem(int row, int col, String type, int index) {
		List<WebElement> cells = tabRows.get(row - 1).findElements(By.tagName("td"));
		if (type.contains("cell")) {
			return cells.get(col - 1);
		} else {			
			return childsGetter(cells.get(col - 1), type).get(index - 1);
		}
	}

	/**
	 * get the cell text of the table on specified row and column.
	 * 
	 * @param row row index of the table.
	 * @param col column index of the table.
	 * @return the cell text
	 */
	public String cellText(int row, int col) {
		return childItem(row, col, "cell", 0).getText();
	}

	/**
	 * button/edit/checkbox are using the same html tag "input", others may be the same,
	 * this method will get the WebElements List accord the user element classes.
	 * 
	 * @param father the father element to get childs
	 * @param elementClass link/button/edit/checkbox/image/list and so on
	 * @return	the WebElements List
	 */
	@SuppressWarnings("null")
	private List<WebElement> childsGetter(WebElement father, String elementClass){
		List<WebElement> elements = father.findElements(By.tagName(elementTagGetter(elementClass)));
		List<WebElement> childs = null;
		for (int i = 0; i < elements.size(); i ++){
			if (elements.get(i).getAttribute("type").contains(elementClass)){
				childs.add(elements.get(i));
			}
		}
		return childs;
	}

	/**
	 * get the tag of element by webelement type.
	 * 
	 * @param elementType link/button/edit/checkbox/image/list and so on
	 * @throws IllegalArgumentException
	 */
	private String elementTagGetter(String elementType){
		if (elementType.toLowerCase().trim().contains("link")){
			return "a";
		}else if(elementType.toLowerCase().trim().contains("button")){
			return "input";
		}else if(elementType.toLowerCase().trim().contains("edit")){
			return "input";
		}else if(elementType.toLowerCase().trim().contains("checkbox")){
			return "input";
		}else if(elementType.toLowerCase().trim().trim().contains("image")){
			return "img";
		}else if(elementType.toLowerCase().trim().contains("list")){
			return "select";
		}else if(elementType.toLowerCase().trim().contains("text")){
			return "textarea";
		}else{
			throw new IllegalArgumentException("please input the correct element type!");
		}
	}
}