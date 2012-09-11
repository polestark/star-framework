package com.star.testdata.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.paic.dbproxy.client.DBAgent;
import com.star.logging.frame.LoggingManager;
import com.star.support.config.ParseProperties;

public class DataBaseUtils {

	private static final LoggingManager LOG = new LoggingManager(DataBaseUtils.class.getName());
	private static final ParseProperties PROP = new ParseProperties("config\\config.properties");
	private String dbChoice;

	/* connect to database use webservice */
	private static final String DB_PROXY = PROP.get("dbProxy");

	private static final String GBS_USER = PROP.get("gbsUser");
	private static final String GBS_DPWD = PROP.get("gbsPwd");
	private static final String GBS_DDNS = PROP.get("gbsDns");
	private static final String GBS_PORT = PROP.get("gbsPort");
	private static final String GBS_DSID = PROP.get("gbsSid");

	private static final String EGIS_USER = PROP.get("egisUser");
	private static final String EGIS_DPWD = PROP.get("egisPwd");
	private static final String EGIS_DDNS = PROP.get("egisDns");
	private static final String EGIS_PORT = PROP.get("egisPort");
	private static final String EGIS_DSID = PROP.get("egisSid");

	/**
	 * construct with initialize database choose operation.
	 * 
	 * @param	dbChoice	the db choice set in your config
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	public DataBaseUtils(String dbChoice) {
		this.setDbChoice(dbChoice);
	}

	/**
	 * @param	dbChoice  the dbChoice to set
	 **/
	public void setDbChoice(String dbChoice) {
		this.dbChoice = dbChoice;
	}

	/**
	 * @return the dbChoice
	 **/
	public String getDbChoice() {
		return this.dbChoice;
	}

	/**
	 * create database connection.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	private DBAgent dbConn() {
		DBAgent dbcon = null;
		try {
			if (getDbChoice().toLowerCase().contains("gbs")) {
				dbcon = new DBAgent(GBS_USER, GBS_DPWD, GBS_DDNS, Integer.parseInt(GBS_PORT), GBS_DSID);
			} else if (getDbChoice().toLowerCase().contains("egis")) {
				dbcon = new DBAgent(EGIS_USER, EGIS_DPWD, EGIS_DDNS, Integer.parseInt(EGIS_PORT), EGIS_DSID);
			} else {
				LOG.error("please append config.properties to add a new database setting!");
				return null;
			}
			dbcon.setUrl(DB_PROXY);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Assert Failed:" + ex.getMessage());
		}

		return dbcon;
	}

	/**
	 * put data into list use webservice.
	 * 
	 * @param	sql exam:
	 *       		"select plan_code from plan where plan_code like ? and rownum <= ?"
	 * @param	params exam: "P055%,10".split(",")
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
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
			throw new RuntimeException("Assert Failed:" + ex.getMessage());
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
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
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
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	public Map<?, ?> queryToMap(String sql, String params) {
		return queryToMap(sql, params, 1);
	}

	/**
	 * execute dml(insert/delete/update)/ddl sql statement.
	 * 
	 * @param	sql exam:
	 *       		"update plan set paln_code = plan_code where plan_code like ?"
	 * @param	params exam: "P055%"
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	public int execModify(String sql, String params) {
		int tempNum = 0;
		try {
			tempNum = dbConn().execute(sql, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Assert Failed:" + ex.getMessage());
		}
		return tempNum;
	}

	/**
	 * execute procedure, get output in parameter list.
	 * 
	 * @param	procName exam: package_name.procedure_name
	 * @param	paramDesc exam: i:input, o:output; etc: "iiii" means 4 input parameters
	 * @param	params all out parameter will be put in string typed array.
	 * @exams: execProcedure("gpos_package_calculate.calc_susp_days", "iiiooo",
	 *         "GP02000000469462,2012-01-01,2012-05-05,null,null,null");
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	public List<String> execProcedure(String procName, String paramDesc, String params) {
		String[] paraArray = params.split(",");
		List<String> dataList = new ArrayList<String>();
		int k = 0;
		try {
			dbConn().callProcedure(procName, paramDesc.toLowerCase(), paraArray);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Assert Failed:" + ex.getMessage());
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
	 * @exams: 	execFunction("add_months", "ii", "sysdate,3")
	 * @author 	PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 */
	public String execFunction(String funcName, String paramDesc, String params) {
		String tempStr = null;
		try {
			tempStr = dbConn().callFunction(funcName, paramDesc, params.split(","));
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException("Assert Failed:" + ex.getMessage());
		}
		return tempStr;
	}
}