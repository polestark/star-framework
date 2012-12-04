package com.star.demo.testcase;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
//import com.star.testdata.fileio.ExcelParseUtils;
import com.star.testdata.string.StringBufferUtils;

public class TestDebugForFrame{
	
	//ExcelParseUtils xls = new ExcelParseUtils("D:", "test.xls");
	StringBufferUtils str = new StringBufferUtils();
	
	@Test
	public void frameWork() {
		List<String> list = new ArrayList<String>();
		
		for (int i = 1; i < 10; i ++){
			list.add(String.valueOf(i));
		}
		
		System.out.println(str.listElementReplace(list, 4, "abc"));
	}
}
