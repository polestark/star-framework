package com.star.testdata.fileio;

import java.io.File;
import java.util.List;
import java.io.FileWriter;
import java.util.ArrayList;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.DocumentException;
import com.star.logging.frame.LoggingManager;

/**
 * 内容描述: XML文件读写
 * 
 * @author 测试仔刘毅
 */
public class XMLFileHandler {
	private Document _document;
	private File _file;
	private final LoggingManager LOG = new LoggingManager(XMLFileHandler.class.getName());

	/**
	 * @param fileName set xml file name
	 */
	public XMLFileHandler(String fileName) {
		if (null == fileName || fileName.isEmpty()) {
			throw new IllegalArgumentException("file can not be null!");
		}
		this._file = new File(fileName);
		this._document = readDocument(_file);
	}

	/**
	 * Description: read and return Document object.
	 * 
	 * @param file the xml File.
	 * @return Document.
	 */
	private Document readDocument(File file) {
		try {
			return new SAXReader().read(file);
		} catch (DocumentException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Description: get every element text by xpath.
	 * 
	 * @param xpath the elements' xpath.
	 * @return the text arraylist.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getElementValuesByXpath(String xpath) {
		List<String> textList = new ArrayList<String>();
		if (null == xpath || xpath.isEmpty()) {
			throw new IllegalArgumentException("the element xpath can not be null!");
		}
		List<Element> elementList = _document.selectNodes(xpath);
		for (int i = 0; i < elementList.size(); i++) {
			textList.add(elementList.get(i).getText());
		}
		return textList;
	}

	/**
	 * Description: get the first element's text located by xpath.
	 * 
	 * @param xpath the element xpath.
	 * @return the text string.
	 */
	public String getElementValueByXpath(String xpath) {
		return getElementValuesByXpath(xpath).get(0);
	}

	/**
	 * Description: modify the xml document.
	 * 
	 * @param xpath the element xpath locator.
	 * @param textValue the text value to be set.
	 */
	@SuppressWarnings("unchecked")
	public void setElementValue(String xpath, String textValue) {
		if (null == xpath || xpath.isEmpty()) {
			throw new IllegalArgumentException("the element xpath can not be null!");
		}
		List<Element> elementList = _document.selectNodes(xpath);
		if (elementList.size() != 1) {
			throw new RuntimeException("your xpath " + xpath + " returns more than one element!");
		} else {
			elementList.get(0).setText(textValue);
		}
		saveXmlFile(_document, _file);
	}

	/**
	 * Description: delete nodes from xml document.
	 * 
	 * @param nodeXpath the nodes' xpath.
	 */
	@SuppressWarnings("unchecked")
	public void removeNode(String nodeXpath) {
		if (null == nodeXpath || nodeXpath.isEmpty()) {
			throw new IllegalArgumentException("the element xpath can not be null!");
		}
		Element root = _document.getRootElement();
		List<Element> childList = root.selectNodes(nodeXpath);
		for (int j = 0; j < childList.size(); j++) {
			root.remove(childList.get(j));
		}
		saveXmlFile(_document, _file);
	}

	/**
	 * Description: save the xml documents.
	 * 
	 * @param document the Document to be saved.
	 */
	private void saveXmlFile(Document document, File file) {
		try {
			XMLWriter output = new XMLWriter(new FileWriter(_file));
			output.write(document);
			output.close();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			System.gc();
		}
	}
}