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

import de.azrael.util.STATIC;
import de.azrael.webserver.HandlerGET;
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
			ServerSocket connect = new ServerSocket(WebserviceUtils.getPort(), 100);
			connect.setSoTimeout(0);
			while(true) {
				//create a socket and lock on to it until a request has been received
				Socket socket = connect.accept();
				logger.info("Request on socket from {} ({})", socket.getInetAddress().getHostName(), socket.getInetAddress().getHostAddress());
				
				//separate received request to a new thread and then accept new requests
				new Thread(() -> {
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
								logger.info("Socket request method {} with message:\n{}", method, payload.toString());
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
							else if(method.equals("GET")) {
								final String fullEndpoint = parse.nextToken();
								boolean tokenVerified = false;
								while(in.ready()) {
									final String header = in.readLine();
									if(header.startsWith("Token:")) {
										String [] token = header.split(" ");
										if(token.length == 2 && STATIC.getToken().equals(token[1])) {
											tokenVerified = true;
											break;
										}
									}
								}
								logger.info("Socket request method {} with endpoint: {}", method, fullEndpoint);
								if(tokenVerified) {
									final String [] splitEndpoint = fullEndpoint.split("\\?");
									final String endpoint = splitEndpoint[0];
									String [] queryParams = null;
									if(splitEndpoint.length == 2) {
										queryParams = splitEndpoint[1].split("&");
									}
									HandlerGET.handleRequest(e, out, endpoint.substring(1), queryParams);
								}
								else {
									WebserviceUtils.return401(out, "Invalid Token", true);
								}
							}
							else {
								StringBuilder payload = new StringBuilder();
								while(in.ready()) {
									payload.append((char)in.read());
								}
								logger.info("Socket request method {} with message: {}", method, payload.toString());
								WebserviceUtils.return405(out, "Method not allowed", false);
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
						try { in.close(); } catch (IOException e) { e.printStackTrace(); }
						try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
						out.close();
					}
				}).start();
				
			}
		} catch (IOException e) {
			logger.error("Webservice error", e);
		} finally {
			if(WebserviceUtils.getPort() != 0) {
				//immediately restart webservice
				logger.info("Restarting Azrael webservice!");
				new Thread(this).start();
			}
		}
	}
}
