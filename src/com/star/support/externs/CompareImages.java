package com.star.support.externs;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class CompareImages {

	private StarFileHandler localFileObject;
	private WebElement remoteImageObject;
	private WebDriver driver;

	public CompareImages(File localFile, WebElement remoteImage, WebDriver driver) throws Exception {
		this.localFileObject = new StarFileHandler(localFile);
		this.remoteImageObject = remoteImage;
		this.driver = driver;
	}

	public CompareImages(String localFile, WebElement remoteImage, WebDriver driver) throws Exception {
		this.localFileObject = new StarFileHandler(localFile);
		this.remoteImageObject = remoteImage;
		this.driver = driver;
	}

	public void changeLocalFileTo(File localfile) throws Exception {
		this.localFileObject = new StarFileHandler(localfile);
	}

	public void changeLocalFileTo(String localfile) throws Exception {
		this.localFileObject = new StarFileHandler(localfile);
	}

	public void changeRemoteImageTo(WebElement remoteImage) {
		this.remoteImageObject = remoteImage;
	}

	public File getLocalImage() throws Exception {
		return this.localFileObject.getFile();
	}

	public WebElement getRemoteImageWebElement() {
		return this.remoteImageObject;
	}

	private String generateMessageDigest(File fileToHash) throws Exception {
		MessageDigest MD5Digest = MessageDigest.getInstance("MD5");
		MD5Digest.reset();
		byte[] bufferSize = new byte[1024];
		InputStream theFileInputStream = new FileInputStream(fileToHash);
		int dataReadFromFile = 0;
		while (dataReadFromFile != -1) {
			dataReadFromFile = theFileInputStream.read(bufferSize);
			if (dataReadFromFile > 0) {
				MD5Digest.update(bufferSize, 0, dataReadFromFile);
			}
		}
		theFileInputStream.close();
		BigInteger MD5Hash = new BigInteger(1, MD5Digest.digest());
		return MD5Hash.toString(16);
	}

	public Boolean compareImages() throws Exception {
		FileDownloader downloadRemoteImage = new FileDownloader(driver);
		StarFileHandler downloadedImage = new StarFileHandler(
				downloadRemoteImage.imageDownloader(this.remoteImageObject));
		if (generateMessageDigest(this.localFileObject.getFile()).equals(
				generateMessageDigest(downloadedImage.getFile()))) {
			return true;
		}
		return false;
	}

}