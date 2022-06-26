package de.azrael.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Directory;

public class FileHandler {
	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
	
	public static boolean createFile(Directory directory, String name, String content) {
		final String fileName = directory.getPath()+name;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
			out.write(directory.isEncryptionEnabled() ? STATIC.encrypt(content) : content);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			logger.error("File couldn't be created: {}", fileName, e);
		}
		return false;
	}
	
	public static void appendFile(Directory directory, String name, String content) {
		final String fileName = directory.getPath()+name;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
			out.write(directory.isEncryptionEnabled() ? STATIC.encrypt(content) : content);
			out.flush();
			out.close();
		} catch(IOException ioe) {
		    logger.error("File couldn't be appended: {}", fileName, ioe);
		}
	}

	public static String readFile(Directory directory, String name) {
		final String fileName = directory.getPath()+name;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null) {
					if(directory.isEncryptionEnabled())
						sb.append(line);
					else
						sb.append(line+"\n");
					line = br.readLine();
				}
				return directory.isEncryptionEnabled() ? STATIC.decrypt(sb.toString()) : sb.toString();
			} catch (IOException e) {
				logger.error("Error on reading file: {}", fileName, e);
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Error on closing file after reading: {}", fileName, e);
				}
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error("File not found: {}", fileName, e);
			return "";
		}
		return null;
	}
	
	public static void deleteFile(Directory directory, String name) {
		File file = new File(directory.getPath()+name);
		if(file.exists())
			file.delete();
	}
	
	public static void createTemp() {
		(new File(System.getProperty("TEMP_DIRECTORY"))).mkdirs();
	}
}
