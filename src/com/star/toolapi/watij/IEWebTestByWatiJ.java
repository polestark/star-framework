package com.star.toolapi.watij;

/**
 * 框架说明：
 * 1、使用WatiJ原生API，暂不采取二次封装；
 * 2、执行用户必须有C:\\windows\\system32的读写权限，以便初次加载复制jniwrap.dll文件。
 * 
 * @author 测试仔刘毅
 */

import java.io.File;
import watij.runtime.ie.IE;
import org.apache.commons.io.FileUtils;

public class IEWebTestByWatiJ {
	
	protected static IE ie;
	
	/**
	 * start a new watij ie test.
	 * 
	 * @throws	RuntimeException
	 */
	
	protected void testCunstruction(){
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
	 * @throws	RuntimeException
	 */
	protected void testTermination(){		
		try{
			if (ie != null && ie.exists()){
				ie.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());			
		}
	}
}