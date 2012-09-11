package com.star.toolapi.watij;

import java.io.File;
import watij.runtime.ie.IE;
import org.apache.commons.io.FileUtils;

public class IEWebTestByWatiJ {
	
	protected static IE ie;
	
	/**
	 * start a new watij ie test.
	 * 
	 * @throws		RuntimeException
	 */
	
	protected void testConstruct(){
		File srcfile = new File("lib\\jniwrap.dll");
		File disfile = new File("C:\\windows\\system32\\jniwrap.dll");
		
		if (!srcfile.exists()){
			throw new RuntimeException("can not find jniwrap.dll, please locate it into lib folder!");
		}
		
		try{
			if (!disfile.exists()){
				FileUtils.copyFile(srcfile, disfile);
			}
		}catch(Throwable t){
			throw new RuntimeException("can not copy jniwrap.dll to C:\\windows\\system32!");			
		}
		ie = new IE();
	}

	/**
	 * close watij ie test.
	 * 
	 * @throws		RuntimeException
	 */
	protected void testTerminate(){		
		try{
			if (ie != null && ie.exists()){
				ie.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());			
		}
	}
}