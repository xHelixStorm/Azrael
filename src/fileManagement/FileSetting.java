package fileManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class FileSetting {
private static PrintWriter pw;
	
	public static void createFile(String name, String content){
		try {
			pw = new PrintWriter(name, "UTF-8");
			pw.print(content);
			pw.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
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

	public static String readFile(String name){
		try {
			BufferedReader br = new BufferedReader(new FileReader(name));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while(line != null){
					sb.append(line);
					line = br.readLine();
				}
				String content = sb.toString();
				return content;
			} catch (IOException e) {
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
			return "";
		}
		return null;
	}
	
	public static ArrayList<String> readFileIntoArray(String _name){
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
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		}
		return null;
	}
	
	public static String [] readFileIntoFixedArray(String _name){
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
				System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
				e.printStackTrace();
			}finally {
				try {
					br.close();
				} catch (IOException e) {
					System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void deleteFile(String name){
		File file = new File(name);
		file.delete();
	}
	
	public static void createTemp(){
		boolean [] dir = new boolean[4];
		dir[0] = (new File(IniFileReader.getTempDirectory())).mkdirs();
		dir[1] = (new File(IniFileReader.getTempDirectory()+"Reports")).mkdirs();
		dir[2] = (new File(IniFileReader.getTempDirectory()+"CommandDelay")).mkdirs();
		dir[3] = (new File(IniFileReader.getTempDirectory()+"AutoDelFiles")).mkdirs();
	}
}
