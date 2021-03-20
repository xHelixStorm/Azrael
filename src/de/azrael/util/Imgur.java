package de.azrael.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.fileManagement.IniFileReader;

public class Imgur {
	private final static Logger logger = LoggerFactory.getLogger(Imgur.class);
	private final static String IMAGE_UPLOAD = "https://api.imgur.com/3/image";
	
	public static String uploadFile(File file) {
		final String clientID = IniFileReader.getImgurClientID();
		if(clientID != null && clientID.length() > 0) {
			ByteArrayOutputStream byteArray = null;
			InputStream is = null;
			OutputStreamWriter wr = null;
			BufferedReader rd = null;
			
			try {
				URL url = new URL(IMAGE_UPLOAD);
			    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			    
			    //create base64 image
			    byteArray = new ByteArrayOutputStream();
			    is = file.toURI().toURL().openStream();
			    byte [] byteChunk = new byte[4096];
			    int n;
			    
			    while((n = is.read(byteChunk)) > 0) {
			    	byteArray.write(byteChunk, 0, n);
			    }
			    
			    byte[] byteImage = byteArray.toByteArray();
			    String dataImage = Base64.getEncoder().encodeToString(byteImage);
			
				String data = URLEncoder.encode("image", "UTF-8") + "="
				+ URLEncoder.encode(dataImage, "UTF-8");
				
				conn.setDoOutput(true);
			    conn.setDoInput(true);
			    conn.setRequestMethod("POST");
			    conn.setRequestProperty("Authorization", "Client-ID " + clientID);
			    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			    conn.connect();
			    StringBuilder stb = new StringBuilder();
			    wr = new OutputStreamWriter(conn.getOutputStream());
			    wr.write(data);
			    wr.flush();
			    
			    // Get the response
			    if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 399) {
				    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				    String line;
				    while ((line = rd.readLine()) != null) {
				        stb.append(line).append("\n");
				    }
				    
				    //return url to uploaded image
				    JSONObject object = new JSONObject(stb.toString());
				    if(object.has("data")) {
				    	object = (JSONObject) object.get("data");
				    	if(object.has("gifv")) {
				    		return object.getString("gifv");
				    	}
				    	if(object.has("link")) {
				    		return object.getString("link");
				    	}
				    	else {
				    		logger.error("File couldn't be uploaded to Imgur. Imgur response:\n{}", stb.toString());
				    	}
				    }
				    else {
				    	logger.error("File couldn't be uploaded to Imgur. Imgur response:\n{}", stb.toString());
				    }
				    return null;
			    }
			    else {
			    	rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				    String line;
				    while ((line = rd.readLine()) != null) {
				        stb.append(line).append("\n");
				    }
				    logger.error("File couldn't be uploaded to Imgur. Imgur response:\n{}", stb.toString());
				    return null;
			    }
			    
			} catch (IOException e) {
				logger.error("File couldn't be uploaded to Imgur", e);
			} finally {
				try {
					byteArray.close();
					is.close();
					wr.close();
					rd.close();
				} catch (IOException e) {
					logger.error("Memory release error", e);
				}
			}
		}
		return null;
	}
}
