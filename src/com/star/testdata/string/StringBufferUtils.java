package com.star.testdata.string;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StringBufferUtils {
	public static final Map<String, Integer> areaCode = new HashMap<String, Integer>();

	/**
	 * get current time format with pattern : formatedTime("yyyy-MM-dd HH:mm:ss").
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String formatedTime(String dateFormat) {
		SimpleDateFormat sdt = new SimpleDateFormat(dateFormat);
		String sysDate = sdt.format(new Date());
		return sysDate;
	}

	/**
	 * get the system current milliseconds.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String getMilSecNow() {
		String sysDateStr = String.valueOf(System.currentTimeMillis());
		return sysDateStr;
	}

	/**
	 * count the times for a string appears in anothor string.
	 * 
	 * @author 	PAICDOM/LIUYI027
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
	 * @author 	PAICDOM/LIUYI027
	 */
	public String getRndNumByLen(int rndLen) {
		int i, count = 0;

		StringBuffer randomStr = new StringBuffer("");
		Random rnd = new Random();

		while (count < rndLen) {
			i = Math.abs(rnd.nextInt(10));
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
	 * @author 	PAICDOM/LIUYI027
	 */
	public String getRndStrByLen(int rndLen) {
		int i, count = 0;
		final String chars = "1,2,3,4,5,6,7,8,9,0,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
		String[] charArr = chars.split(",");

		StringBuffer randomStr = new StringBuffer("");
		Random rnd = new Random();

		while (count < rndLen) {
			i = Math.abs(rnd.nextInt(10) % charArr.length);
			randomStr.append(charArr[i]);
			count++;
		}
		return randomStr.toString();
	}

	/**
	 * oracle lpad method in java.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String strLeftExpand(String appointedStr, int finalLen, char fillWith) {
		String tempStr = appointedStr;
		while (tempStr.length() < finalLen) {
			tempStr = String.valueOf(fillWith) + tempStr;
		}
		return tempStr;
	}

	/**
	 * oracle rpad method in java.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String strRightExpand(String appointedStr, int finalLen, char fillWith) {
		String tempStr = appointedStr;
		while (tempStr.length() < finalLen) {
			tempStr = tempStr + String.valueOf(fillWith);
		}
		return tempStr;
	}

	/**
	 * generate idnos.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	static {
		StringBufferUtils.areaCode.put("北京市", 110000);
		StringBufferUtils.areaCode.put("天津市", 120000);
		StringBufferUtils.areaCode.put("石家庄市", 130101);
		StringBufferUtils.areaCode.put("太原市", 140101);
		StringBufferUtils.areaCode.put("呼和浩特市", 150101);
		StringBufferUtils.areaCode.put("沈阳市", 210101);
		StringBufferUtils.areaCode.put("长春市", 220101);
		StringBufferUtils.areaCode.put("哈尔滨市", 230101);
		StringBufferUtils.areaCode.put("上海市", 310100);
		StringBufferUtils.areaCode.put("南京市", 320101);
		StringBufferUtils.areaCode.put("杭州市", 330101);
		StringBufferUtils.areaCode.put("合肥市", 340101);
		StringBufferUtils.areaCode.put("福州市", 350101);
		StringBufferUtils.areaCode.put("南昌市", 360101);
		StringBufferUtils.areaCode.put("济南市", 370101);
		StringBufferUtils.areaCode.put("郑州市", 410101);
		StringBufferUtils.areaCode.put("武汉市", 420101);
		StringBufferUtils.areaCode.put("长沙市", 430101);
		StringBufferUtils.areaCode.put("广州市", 440101);
		StringBufferUtils.areaCode.put("南宁市", 450101);
		StringBufferUtils.areaCode.put("重庆市", 500100);
		StringBufferUtils.areaCode.put("成都市", 510101);
		StringBufferUtils.areaCode.put("贵阳市", 520101);
		StringBufferUtils.areaCode.put("昆明市", 530101);
		StringBufferUtils.areaCode.put("拉萨市", 540101);
		StringBufferUtils.areaCode.put("西安市", 610101);
		StringBufferUtils.areaCode.put("兰州市", 620101);
		StringBufferUtils.areaCode.put("西宁市", 630101);
		StringBufferUtils.areaCode.put("银川市", 640101);
		StringBufferUtils.areaCode.put("乌鲁木齐市", 650101);
		StringBufferUtils.areaCode.put("台湾省", 710000);
		StringBufferUtils.areaCode.put("香港特别行政区", 810000);
		StringBufferUtils.areaCode.put("澳门特别行政区", 820000);
	}

	/**
	 * generate random number for idno.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String getCertiCode(String capCity, String birthDay, String sexCode) {
		StringBuilder generater = new StringBuilder();
		generater.append(StringBufferUtils.areaCode.get(capCity));
		generater.append(birthDay);
		generater.append(this.rndCodeWithSex(sexCode));
		generater.append(this.veriCodeCalc(generater.toString().toCharArray()));
		return generater.toString();
	}

	/**
	 * generate verify no for idno.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public char veriCodeCalc(char[] chars) {
		if (chars.length < 17) {
			return ' ';
		}
		int[] c = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 };
		char[] r = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
		int[] n = new int[17];
		int result = 0;
		for (int i = 0; i < n.length; i++) {
			n[i] = Integer.parseInt(chars[i] + "");
		}
		for (int i = 0; i < n.length; i++) {
			result += c[i] * n[i];
		}
		return r[result % 11];
	}

	/**
	 * generate sex number for idno.
	 * 
	 * @author 	PAICDOM/LIUYI027
	 */
	public String rndCodeWithSex(String sexCode) {
		String rndString = "";
		StringBufferUtils sbu = new StringBufferUtils();
		int rndNum = (int) Math.rint(Math.random() * (998 - 1) + 1);
		if (sexCode == "男" || sexCode == "M") {
			rndString = sbu.strLeftExpand(String.valueOf(rndNum + 1), 3, '0');
		} else {
			rndString = sbu.strLeftExpand(String.valueOf(rndNum), 3, '0');
		}
		return rndString;
	}
}