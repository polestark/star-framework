package com.star.cases;

import java.io.IOException;

import org.junit.Test;
import com.star.testdata.fileio.CSVFileHanlder;

public class UserTestCase {

	CSVFileHanlder csv = new CSVFileHanlder("E:\\test.csv", "UTF-8");

	@Test
	public void testCSV() throws IOException {
		csv.appendLineToCSV("我艹你妹");
	}
}