package com.star.logging.selenium;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LoggingUtils {
	public static BufferedWriter createWriter(String resultFileNameAndPath, String resultEncoding,
			boolean replaceExistingFile) throws RuntimeException {
		BufferedWriter loggingWriter = null;
		try {
			File resultFile = new File(resultFileNameAndPath);
			if ((replaceExistingFile) && (resultFile.exists())) {
				resultFile.delete();
			}
			boolean newFileCreated = resultFile.createNewFile();
			if (!newFileCreated) {
				throw new RuntimeException("Failed to create new file: '" + resultFileNameAndPath
						+ "'. Does this file already exist?");
			}

			if (!resultFile.canWrite()) {
				throw new RuntimeException("Cannot write to file: '" + resultFileNameAndPath + "'");
			}

			loggingWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					resultFileNameAndPath), resultEncoding));
		} catch (IOException ioExc) {
			throw new RuntimeException(
					"ERROR while creating file: '" + resultFileNameAndPath + "'", ioExc);
		}
		return loggingWriter;
	}

	public static BufferedWriter createWriter(String resultFileNameAndPath, String resultEncoding)
			throws RuntimeException {
		return createWriter(resultFileNameAndPath, resultEncoding, false);
	}

	public static String timeStampForFileName() {
		return timeStampForFileName("yyyy-MM-dd_HH-mm");
	}

	public static String timeStampForFileName(String simpleDateFormat) {
		Date currentDateTime = new Date(System.currentTimeMillis());
		SimpleDateFormat humanReadableFormat = new SimpleDateFormat(simpleDateFormat);
		return humanReadableFormat.format(currentDateTime);
	}

	static String[] getCorrectedArgsArray(LoggingBean loggingBean, int presetNumArgs,
			String defaultValue) {
		String[] currentArgs;
		if ((null == loggingBean) || (null == loggingBean.getArgs()))
			currentArgs = new String[0];
		else {
			currentArgs = loggingBean.getArgs();
		}
		String[] newArgs = new String[presetNumArgs];
		for (int i = 0; i < currentArgs.length; i++) {
			newArgs[i] = currentArgs[i];
		}
		for (int i = currentArgs.length; i < presetNumArgs; i++) {
			newArgs[i] = defaultValue;
		}
		return newArgs;
	}
}