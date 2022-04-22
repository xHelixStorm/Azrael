package de.azrael.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.enums.Directory;
import net.dv8tion.jda.api.events.ReadyEvent;

public class FileHandler {
	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
	private static PrintWriter pw;
	
	public static void createFile(Directory directory, String name, String content) {
		final String fileName = directory.getPath()+name;
		try {
			pw = new PrintWriter(fileName, "UTF-8");
			pw.print(directory.isEncryptionEnabled() ? STATIC.encrypt(content) : content);
			pw.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error("File couldn't be created: {}", fileName, e);
		}
	}
	
	public static void appendFile(Directory directory, String name, String content) {
		final String fileName = directory.getPath()+name;
		try {
		    FileWriter fw = new FileWriter(fileName, true);
		    fw.write(directory.isEncryptionEnabled() ? STATIC.encrypt(content) : content);
		    fw.close();
		} catch(IOException ioe) {
		    logger.error("File couldn't be appended: {}", fileName, ioe);
		}
	}

	public static String readFile(Directory directory, String name) {
		final String fileName = directory.getPath()+name;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null) {
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
		} catch (FileNotFoundException e) {
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
	
	public static void createTemp(ReadyEvent e) {
		(new File(System.getProperty("TEMP_DIRECTORY"))).mkdirs();
	}
}
