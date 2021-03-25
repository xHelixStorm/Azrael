package de.azrael.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.webserver.HandlerPOST;
import de.azrael.webserver.WebserviceUtils;
import net.dv8tion.jda.api.events.ReadyEvent;

public class Webserver implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Webserver.class);
	
	private ReadyEvent e;
	
	public Webserver(ReadyEvent _e) {
		this.e = _e;
	}

	@Override
	public void run() {
		try {
			ServerSocket connect = new ServerSocket(WebserviceUtils.getPort());
			while(true) {
				//create a socket and lock on to it until a request has been received
				Socket socket = connect.accept();
				
				BufferedReader in = null;
				PrintWriter out = null;
				try {
					//get input data
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream());
					
					String input = in.readLine();
					if(input != null) {
						//retrieve the method
						StringTokenizer parse = new StringTokenizer(input);
						String method = parse.nextToken().toUpperCase();
						
						if(method.equals("POST")) {
							StringBuilder payload = new StringBuilder();
							while(in.ready()) {
								payload.append((char)in.read());
							}
							if(payload.toString().contains("Content-Type:") && payload.toString().contains("application/json")) {
								String passedJson = null;
								if(payload.toString().contains("{")) {
									int index = payload.toString().indexOf("{");
									if(index != -1)
										passedJson = payload.toString().substring(index);
								}
								if(passedJson != null) {
									JSONObject json = new JSONObject(passedJson);
									HandlerPOST.handleRequest(e, out, json);
								}
								else {
									WebserviceUtils.return400(out, "JSON format int content required", false);
								}
							}
							else {
								WebserviceUtils.return502(out, "Content Type application/json required.", false);
							}
						}
						else {
							WebserviceUtils.return501(out, "POST request method required", false);
						}
					}
				} catch(IOException e) {
					logger.error("Request error", e);
					WebserviceUtils.return500(out, "Unknown Error: "+e, false);
				} catch(JSONException e) {
					logger.error("Json error", e);
					WebserviceUtils.return502(out, "JSON Error: "+e, false);
				} catch(Exception e) {
					logger.error("Request error", e);
					WebserviceUtils.return500(out, "Unkown Error: "+e, false);
				} finally {
					in.close();
					out.close();
					socket.close();
				}
			}
		} catch (IOException e) {
			logger.error("Webservice error", e);
		} finally {
			if(WebserviceUtils.getPort() != 0) {
				//immediately restart webservice
				logger.debug("Restarting webservice!");
				new Thread(this).start();
			}
		}
	}
}