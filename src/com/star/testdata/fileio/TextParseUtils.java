package com.star.testdata.fileio;

/**
 * 文本文件读取：
 * 1、将文本文件内容读取存入Map之后存入List；
 * 2、将文本文件内容List根据配置得到指定行数读出为Map供运行时使用；
 * 
 * @author 测试仔刘毅
 **/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.star.logging.frame.LoggingManager;

public class TextParseUtils {

	private String fileName;
	public Map<String, String> paraMap;
	private final LoggingManager LOG = new LoggingManager(TextParseUtils.class.getName());

	/**
	 * class construct with initialize parameters.
	 * 
	 * @param	fName the file name to be parsed
	 * @throws	RuntimeException
	 **/
	public TextParseUtils(String fName) {
		this.fileName = fName;
	}

	/**
	 * add map to arraylist.
	 * @throws	RuntimeException
	 **/
	@SuppressWarnings("resource")
	public List<Map<String, String>> MyList(){
		File f = new File(fileName);
		List<Map<String, String>> paraList = new ArrayList<Map<String, String>>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String strKey = br.readLine();
			String[] keys = strKey.split(";");
			String strParm = null;			
			while ((strParm = br.readLine()) != null && strParm.length() > 0) {
				String[] parms = strParm.split(";");
				paraList.add(creatParaMap(keys, parms));
			}			
		}catch(Exception e){
			LOG.error(e);			
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
		return paraList;
	}

	/**
	 * put data into map.
	 * 
	 * @param	keys the keyname array to put into map
	 * @param	parms the keyvalue array to put into map
	 * @throws	RuntimeException
	 **/
	public Map<String, String> creatParaMap(String[] keys, String[] parms) {
		Map<String, String> paraMap = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			paraMap.put(String.valueOf(keys[i]), String.valueOf(parms[i]));
		}
		return paraMap;
	}

	/**
	 * get datamap from datalist using configed line number.
	 * 
	 * @param	index the line number of your file to read
	 * @throws	RuntimeException
	 **/
	public Map<String, String> getConfigedParaMap(String index){
		Iterator<Map<String, String>> it = MyList().iterator();
		if (index == null) {
			index = "1";
		}
		for (int i = 0; i < Integer.valueOf(index); i++) {
			if (it.hasNext()) {
				paraMap = (Map<String, String>) it.next();
				System.out.println(paraMap.toString());
			}
		}
		return paraMap;
	}

	/**
	 * override getConfigedParaMap using default line number 1.
	 * 
	 * @throws	RuntimeException
	 **/
	public Map<String, String> getConfigedParaMap(){
		return getConfigedParaMap(null);
	}
}