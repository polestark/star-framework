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
import com.paic.dbproxy.client.DBAgent;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;

public class DataBaseUtils {

	private static final LoggingManager LOG = new LoggingManager(DataBaseUtils.class.getName());
	private static ParseProperties property = new ParseProperties("config\\config.properties");
	private String choice = "gbs";

	/* connect to database use webservice */
	private final String DB_PROXY = property.get("dbProxy");

	/**
	 * construct with initialize database choose operation.
	 * 
	 * @param	dbChoice	the db choice set in your config
	 **/
	public DataBaseUtils(String dbChoice) {
		setDBChoice(dbChoice);
	}

	/**
	 * set database choice
	 * 
	 * @param	dbChoice the db choice set in your config
	 **/
	private void setDBChoice(String dbChoice){
		this.choice = dbChoice;
	}

	/**
	 * get database choice
	 * 
	 * @return	daatbase choice in string
	 **/
	private String getDBChoice(){
		return this.choice.toLowerCase();
	}

	/**
	 * create database connection.
	 * 
	 * @throws	RuntimeException
	 **/
	protected synchronized DBAgent dbConn() {
		DBAgent dbcon = null;
		try {
			String dbUSER = property.get(getDBChoice() + "User");
			String dbDPWD = property.get(getDBChoice() + "Pwd");
			String dbDDNS = property.get(getDBChoice() + "Dns");
			String dbPORT = property.get(getDBChoice() + "Port");
			String dbDSID = property.get(getDBChoice() + "Sid");
			dbcon = new DBAgent(dbUSER, dbDPWD, dbDDNS, Integer.parseInt(dbPORT), dbDSID);			
			dbcon.setUrl(DB_PROXY);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Create DataBase Connection Failed:" + ex.getMessage());
		}
		return dbcon;
	}

	/**
	 * put data into list use webservice.
	 * 
	 * @param	sql exam:
	 *       		"select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10".split(",")
	 * @throws	RuntimeException
	 **/
	public List<?> queryToList(String sql, String params) {
		List<?> tempList = null;
		String[] paraArray = null;
		try {
			if (params.contains(",")) {
				paraArray = params.split(",");
			} else {
				paraArray = new String[] { params };
			}
			tempList = dbConn().query(sql, paraArray);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("queryToList Failed:" + ex.getMessage());
		}
		return tempList;
	}

	/**
	 * put data into map use webservice.
	 * 
	 * @param	sql exam:
	 *       		"select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10".split(",")
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
	 * @param	sql exam:
	 *       		"select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10".split(",")
	 * @throws	RuntimeException
	 **/
	public Map<?, ?> queryToMap(String sql, String params) {
		return queryToMap(sql, params, 1);
	}

	/**
	 * execute dml(insert/delete/update)/ddl sql statement.
	 * 
	 * @param	sql exam:
	 *       		"update plan set paln_code = plan_code where plan_code like ?"
	 * @param	params exam: "P055%"
	 * @throws	RuntimeException
	 **/
	public int execModify(String sql, String params) {
		int tempNum = 0;
		try {
			tempNum = dbConn().execute(sql, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("execModify Failed:" + ex.getMessage());
		}
		return tempNum;
	}

	/**
	 * execute procedure, get output in parameter list.
	 * 
	 * @param	procName exam: package_name.procedure_name
	 * @param	paramDesc exam: i:input, o:output; etc: "iiii" means 4 input parameters
	 * @param	params all out parameter will be put in string typed array.
	 * exams: execProcedure("gpos_package_calculate.calc_susp_days", "iiiooo",
	 *         "GP02000000469462,2012-01-01,2012-05-05,null,null,null");
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
	 * execute function, get output in string.
	 * 
	 * @param	funcName exam: add_months
	 * @param	paramDesc exam: i:input, o:output; etc: "iiii" means 4 input parameters
	 * @param	params out will be a string.
	 * exams: 	execFunction("add_months", "ii", "sysdate,3")
	 * @throws	RuntimeException
	 **/
	public String execFunction(String funcName, String paramDesc, String params) {
		String tempStr = null;
		try {
			tempStr = dbConn().callFunction(funcName, paramDesc, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("execFunction Failed:" + ex.getMessage());
		}
		return tempStr;
	}
}