package com.star.testdata.jdbc;

/**
 * 数据库操作：
 * 1、查询返回；
 * 2、执行dml的修改、删除和更新；
 * 3、执行存储过程和函数；
 * 4、基于DBAgent（高宁提供）实现。
 * 
 * @author 测试仔刘毅
 **/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.paic.dbproxy.client.DB;
import com.paic.dbproxy.client.DBHelper;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;

public class DataBaseUtils {

	private final LoggingManager LOG = new LoggingManager(DataBaseUtils.class.getName());
	private final ParseProperties CONFIG = new ParseProperties("config\\config.properties");
	private String choice = "gbs";
	private DBHelper dbConfig = new DBHelper();
	private int timeBuffer = 10000;
	private String connectMode = null;
	private final String DB_PROXY = CONFIG.get("dbProxy");

	/**
	 * construct with initialize database choose operation.
	 * 
	 * @param	dbChoice	the db choice set in your config
	 **/
	public DataBaseUtils(String dbChoice) {
		this.choice = dbChoice;
	}
	
	public void setConnectMode(String mode){
		this.connectMode = mode;
	}

	public void setTimeBuffer(int timeout){
		this.timeBuffer = timeout;		
	}
	
	private void setDefaultConnectMode(){
		Properties property = System.getProperties();
		if (property.containsValue("int")){
			setConnectMode(DBHelper.MODE_DIRECT);
		}else{
			setConnectMode(DBHelper.MODE_PROXY);
		}
		dbConfig.setConnectMode(connectMode);
	}

	/**
	 * create database connection.
	 * 
	 * @throws	RuntimeException
	 **/
	private DB dbConn() {
		try {
			String dbUSER = CONFIG.get(this.choice.toLowerCase() + "User");
			String dbDPWD = CONFIG.get(this.choice.toLowerCase() + "Pwd");
			String dbDDNS = CONFIG.get(this.choice.toLowerCase() + "Dns");
			String dbPORT = CONFIG.get(this.choice.toLowerCase() + "Port");
			String dbDSID = CONFIG.get(this.choice.toLowerCase() + "Sid");
			
			if (null == connectMode) {
				setDefaultConnectMode();
			} else {
				if (connectMode.equals(DBHelper.MODE_AUTO)) {
					dbConfig.setTimeout(timeBuffer);
				}
			}

			dbConfig.setWebserviceUrl(DB_PROXY);
			return dbConfig.get(dbUSER, dbDPWD, dbDDNS, Integer.parseInt(dbPORT), dbDSID);	
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Create DataBase Connection Failed:" + ex.getMessage());
		}
	}

	/**
	 * put data into list use webservice.
	 * 
	 * @param	sql exam: "select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10"
	 * @throws	RuntimeException
	 **/
	public List<?> queryToList(String sql, String params) {
		String[] paraArray = null;
		try {
			if (params.contains(",")) {
				paraArray = params.split(",");
			} else {
				paraArray = new String[] { params };
			}
			return dbConn().query(sql, paraArray);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("queryToList Failed:" + ex.getMessage());
		}
	}

	/**
	 * put data into map use webservice.
	 * 
	 * @param	sql exam: "select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10"
	 * @param	lineNum exam: 5
	 * @throws	RuntimeException
	 **/
	public Map<?, ?> queryToMap(String sql, String params, int lineNum) {
		Map<?, ?> dataMap = null;
		List<?> dataList = queryToList(sql, params);
		Iterator<?> it = dataList.iterator();
		for (int i = 0; it.hasNext() && i < lineNum; i++) {
			dataMap = (Map<?, ?>) it.next();
		}
		return dataMap;
	}

	/**
	 * override queryToMap method use default line number 1.
	 * 
	 * @param	sql exam: "select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10"
	 * @throws	RuntimeException
	 **/
	public Map<?, ?> queryToMap(String sql, String params) {
		return queryToMap(sql, params, 1);
	}

	/**
	 * execute dml(insert/delete/update)/ddl sql statement.
	 * 
	 * @param	sql exam: "update plan set paln_code = plan_code where plan_code like ?"
	 * @param	params exam: "P055%"
	 * @throws	RuntimeException
	 **/
	public int execModify(String sql, String params) {
		try {
			return dbConn().execute(sql, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("execModify Failed:" + ex.getMessage());
		}
	}

	/**
	 * execute procedure, get output in parameter list.</BR>
	 * exams: execProcedure("gpos_package_calculate.calc_susp_days", "iiiooo",</BR>
	 *         "GP02000000469462,2012-01-01,2012-05-05,null,null,null");
	 * 
	 * @param	procName exam: package_name.procedure_name
	 * @param	paramDesc exam: i:input, o:output; etc: "iiii" means 4 input parameters
	 * @param	params all out parameter will be put in string typed array.
	 * @throws	RuntimeException
	 **/
	public List<String> execProcedure(String procName, String paramDesc, String params) {
		String[] paraArray = params.split(",");
		List<String> dataList = new ArrayList<String>();
		int k = 0;
		try {
			dbConn().callProcedure(procName, paramDesc.toLowerCase(), paraArray);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("execProcedure Failed:" + ex.getMessage());
		}
		for (int i = 0; i < paraArray.length; i++) {
			if (paramDesc.substring(i, i + 1).equalsIgnoreCase("o")) {
				dataList.add(k, paraArray[i]);
				k++;
			}
		}
		return dataList;
	}

	/**
	 * execute function, get output in string.</BR>
	 * exams: execFunction("add_months", "ii", "sysdate,3")
	 * 
	 * @param	funcName exam: add_months
	 * @param	paramDesc exam: i:input, o:output; etc: "iiii" means 4 input parameters
	 * @param	params out will be a string.
	 * @throws	RuntimeException
	 **/
	public String execFunction(String funcName, String paramDesc, String params) {
		try {
			return dbConn().callFunction(funcName, paramDesc, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("execFunction Failed:" + ex.getMessage());
		}
	}
}