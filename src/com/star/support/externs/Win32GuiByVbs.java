package com.star.support.externs;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import com.star.support.externs.Win32GuiByVbs;
import com.star.logging.frame.LoggingManager;
import com.star.testdata.string.StringBufferUtils;

public class Win32GuiByVbs{
	
	private static final LoggingManager LOG = new LoggingManager(Win32GuiByVbs.class.getName());	
	private static final StringBufferUtils SBF = new StringBufferUtils();
	private static final String VBSRES = "vbsResult";
	
	private final String vbs_1 = "Set fObject = CreateObject(\"Scripting.FileSystemObject\") \n"
		  				 + "Set fileStream = fObject.CreateTextFile(\"{0}\", True) \n";
	private final String vbs_2 = "fileStream.Write({0}) \n"
						  + "fileStream.Close \n"
						  + "Set fileStream = nothing \n"
						  + "Set fObject = Nothing";

	/**
	 * execute  a specified vbs string and get its return value by temp file.
	 * 
	 * @param 	vbs vbs string to be executed
	 * @return	the vbs returned values: string
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public String getVbsResult(String vbs) {
		String vbsfileName = getEnvironment("TEMP") + "\\temp_" + SBF.getMilSecNow() + ".vbs";
		String tmpfileName = getEnvironment("TEMP") + "\\temp_" + SBF.getMilSecNow() + ".txt";
		
		if (!vbs.contains(VBSRES)) {
			LOG.error("vbs returned viriable name must be '" + VBSRES + "' !");
			throw new RuntimeException("vbs returned viriable name must be '" + VBSRES + "' !");
		}
		String vbsContent = MessageFormat.format(vbs_1, new Object[] { tmpfileName }) + vbs + "\n"
						  + MessageFormat.format(vbs_2, new Object[] { VBSRES });
		String resText = null;
		File file = null;
		BufferedReader fileReader = null;

		try {
			createVbsFile(vbsContent, vbsfileName);
			executeVbsFile(vbsfileName);

			file = new File(tmpfileName);
			fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			resText = fileReader.readLine().toString();
			fileReader.close();
			new File(vbsfileName).delete();
			file.delete();
		} catch (Exception ex) {
			LOG.error(ex);
			throw new RuntimeException(ex.getMessage());
		}
		return resText;
	}

	/**
	 * execute a vbs file.
	 * 
	 * @param	fileName whole name whitch vbs file to be executed
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	private void executeVbsFile(String vbsfileName){
		try {
			String[] vbsCmd  = new String[]{"wscript", vbsfileName};  
			Process process = Runtime.getRuntime().exec(vbsCmd);
			process.waitFor();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());
		}
	}

	/**
	 * create a temp vbs file to be executed.
	 * 
	 * @param	vbs string content to be written into file
	 * @param	vbsfileName whole name whitch vbs file to be saved
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	private void createVbsFile(String vbs, String vbsfileName){
		File file = new File(vbsfileName);
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(vbs);
			writer.flush();
			writer.close();
		}catch(Exception e){
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());			
		}
	}

	/**
	 * read regedit info by specified key.
	 * 
	 * @param	regKey whole path and keyname of regedit key
	 * @return	the regedit key value
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public String regRead(String regKey){
		String vbsStr = "Set WshObject = CreateObject(\"WScript.Shell\") \n"
						+ VBSRES 
						+ " = WshObject.RegRead(\"" + regKey + "\") \n"
						+ "Set WshObject = Nothing";		
		return getVbsResult(vbsStr);
	}

	/**
	 * get the ie version under current system.
	 * 
	 * @return	the internet explorer version string
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public String ieVersion(){
		String keyName = "HKLM\\Software\\Microsoft\\Internet Explorer\\version";
		String version = regRead(keyName);
		return version;
	}

	/**
	 * get system environment values.
	 * 
	 * @param	virName viriable name to get, such as "classpath", "JAVA_HOME"
	 * @return	the viriable value
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public String getEnvironment(String virName) {
		byte[] env = new byte[1000];
		try {
			Process process = Runtime.getRuntime().exec("cmd /c echo %" + virName + "% ");
			process.waitFor();
			InputStream iStream = process.getInputStream();
			iStream.read(env);
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("execute extern file failed:" + e.getMessage());		
		}
		return new String(env).trim();
	}

	/**
	 * kill win32 process using cmd.exe.
	 * 
	 * @param	process process name like 'iexplore' or 'iexplore.exe'
	 * @author  PAICDOM/LIUYI027
	 * @throws	RuntimeException
	 **/
	public void killWin32Process(String process){
		String cmd = "cmd /c taskkill /f /im " + process.replace(".exe", "") + ".exe";
		try {
			Runtime.getRuntime().exec(cmd).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}
}