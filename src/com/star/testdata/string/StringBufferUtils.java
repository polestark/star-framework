package com.star.testdata.string;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class StringBufferUtils {
	
	private static final CertificateCode CODE = new CertificateCode();
	
	/**
	 * get current time string in specified date format.
	 * 
	 * @param dateFormat the formatter of date, such as:yyyy-MM-dd HH:mm:ss:SSS
	 */
	public String formatedTime(String dateFormat) {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(new Date());
	}

	/**
	 * get specified time string in specified date format.
	 * 
	 * @param addDays days after or before current date, use + and - to add.
	 * @param dateFormat the formatter of date, such as:yyyy-MM-dd HH:mm:ss:SSS.
	 */
	public String addDaysByFormatter(int addDays, String dateFormat) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, addDays);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(cal.getTime());
	}

	/**
	 * get first day of next month in specified date format.
	 * 
	 * @param dateFormat the formatter of date, such as:yyyy-MM-dd HH:mm:ss:SSS.
	 */
	public String firstDayOfNextMonth(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(cal.getTime());
	}

	/**
	 * get first day of specified month and specified year in specified date format.
	 * 
	 * @param year the year of the date.
	 * @param month the month of the date.
	 * @param dateFormat the formatter of date, such as:yyyy-MM-dd HH:mm:ss:SSS.
	 */
	public String firstDayOfMonth(int year, int month, String dateFormat) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.YEAR, year);
		cal.add(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(cal.getTime());
	}

	/**
	 * get first day of specified month of current year in specified date format.
	 * 
	 * @param month the month of the date.
	 * @param dateFormat the formatter of date, such as:yyyy-MM-dd HH:mm:ss:SSS.
	 */
	public String firstDayOfMonth(int month, String dateFormat) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(cal.getTime());
	}

	/**
	 * get the system current milliseconds.
	 */
	public String getMilSecNow() {
		return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * count the times for a string appears in anothor string.
	 * 
	 * @param	myString the string to search. 
	 * @param	myChar the char to count in the string.
	 */
	public int countStrRepeat(String myString, String myChar) {
		int count = 0, start = 0;
		while ((start = myString.indexOf(myChar, start)) >= 0) {
			start += myChar.length();
			count++;
		}
		return count;
	}

	/**
	 * generate specified length string with numbers.
	 * 
	 * @param	lengthOfNumber the length of the number string to be created.
	 */
	public String getRndNumByLen(int lengthOfNumber) {
		int i, count = 0;

		StringBuffer randomStr = new StringBuffer("");
		Random rnd = new Random();

		while (count < lengthOfNumber) {
			i = Math.abs(rnd.nextInt(9));
			if (i == 0 && count == 0) {
			} else {
				randomStr.append(String.valueOf(i));
				count++;
			}
		}
		return randomStr.toString();
	}

	/**
	 * generate specified length string with chars.
	 * 
	 * @param	lengthOfString the length of the string to be created. 
	 */
	public String getRndStrByLen(int lengthOfString) {
		int i, count = 0;
		final String chars = "1,2,3,4,5,6,7,8,9,0,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
		String[] charArr = chars.split(",");

		StringBuffer randomStr = new StringBuffer("");
		Random rnd = new Random();

		while (count < lengthOfString) {
			i = Math.abs(rnd.nextInt(35) % charArr.length);
			randomStr.append(charArr[i]);
			count++;
		}
		return randomStr.toString();
	}

	/**
	 * generate random number for idno.
	 * 
	 * @param	capCity province name or capital city name of the province.
	 * @param	birthDay birth date of the person to get idnos.
	 * @param	sexCode sex of the person to get idnos.
	 */
	public String getCertiCode(String capCity, String birthDay, String sexCode) {
		return CODE.getCertiCode(capCity, birthDay, sexCode);
	}

	/**
	 * generate random number for idno.
	 * 
	 * @param	cityCode the city code of the chinese areas.
	 * @param	birthDay birth date of the person to get idnos.
	 * @param	sexCode sex of the person to get idnos.
	 */
	public String getCertiCode(int cityCode, String birthDay, String sexCode) {
		return CODE.getCertiCode(cityCode, birthDay, sexCode);
	}

	/**
	 * oracle lpad method in java.
	 * 
	 * @param	appointedStr original string
	 * @param	finalLen goal length of the string
	 * @param	fillWith chars to be filled with when string is shorter the goal length. 
	 */
	public String strLeftExpand(String appointedStr, int finalLen, char fillWith) {
		return CODE.strLeftExpand(appointedStr, finalLen, fillWith);
	}

	/**
	 * oracle rpad method in java.
	 * 
	 * @param	appointedStr original string
	 * @param	finalLen goal length of the string
	 * @param	fillWith chars to be filled with when string is shorter the goal length.
	 */
	public String strRightExpand(String appointedStr, int finalLen, char fillWith) {
		return CODE.strRightExpand(appointedStr, finalLen, fillWith);
	}

	/**
	 * replace element from list.
	 * @param list the original list.
	 * @param index the position to replace element.
	 * @param newElement the new element for the list.
	 */
	public List<String> listElementReplace(List<String> list, int index, String newElement){
		list.remove(index);
		list.add(index, newElement);
		return list;
	}
}