package fileManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;

public class FileSetting {
	private static final Logger logger = LoggerFactory.getLogger(FileSetting.class);
	private static PrintWriter pw;
	
	public static void createFile(String name, String content){
		try {
			pw = new PrintWriter(name, "UTF-8");
			pw.print(content);
			pw.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error("Exception on file creation: {}", name, e);
		}
	}
	
	public static void appendFile(String name, String content){
		try
		{
		    FileWriter fw = new FileWriter(name, true);
		    fw.write(content);
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static String readFile(String name) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(name));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null){
					sb.append(line+"\n");
					line = br.readLine();
				}
				String content = sb.toString();
				return content;
			} catch (IOException e) {
				logger.error("Error on read line of readFile: {}", name, e);
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("File {} couldn't be closed after reading", name, e);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File {} to read couldn't be found", name, e);
			return "";
		}
		return null;
	}
	
	public static ArrayList<String> readFileIntoArray(String _name) {
		ArrayList<String> content = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(_name));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null){
					content.add(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				return content;
			} catch (IOException e) {
				logger.error("Error on read line of readFileIntoArray: {}", _name, e);
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("File {} couldn't be closed after reading into an array", _name, e);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File {} to read couldn't be found", _name, e);
		}
		return null;
	}
	
	public static String [] readFileIntoFixedArray(String _name) {
		ArrayList<String> content = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(_name));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null){
					content.add(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				
				String [] contentReturn = new String[content.size()];
				for(int index = 0; index < content.size(); index++) {
					contentReturn[index] = content.get(index);
				}
				return contentReturn;
			} catch (IOException e) {
				logger.error("Error on read line of readFileIntoFixedArray: {}", _name, e);
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("File {} couldn't be closed after reading into a fixed array", _name, e);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File {} to read couldn't be found", _name, e);
		}
		return null;
	}
	
	public static void deleteFile(String name) {
		File file = new File(name);
		file.delete();
	}
	
	public static void createTemp(ReadyEvent e) {
		(new File(IniFileReader.getTempDirectory())).mkdirs();
	}
	
	public static void createGuildDirectory(Guild guild) {
		(new File("files/Guilds")).mkdir();
		if(new File("files/Guilds/"+guild.getId()).mkdir()) {
			if(!new File("files/Guilds/"+guild.getId()+"/reactionmessage.txt").exists())
				createFile("files/Guilds/"+guild.getId()+"/reactionmessage.txt", "");
			if(!new File("files/Guilds/"+guild.getId()+"/helpmessage.txt").exists())
				createFile("files/Guilds/"+guild.getId()+"/helpmessage.txt", "");
		}
	}
}
