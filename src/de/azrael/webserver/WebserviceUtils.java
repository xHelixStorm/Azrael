package de.azrael.webserver;

import java.io.PrintWriter;
import java.util.Date;

import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;

public class WebserviceUtils {
	private static int PORT = STATIC.getPort();
	
	public static int getPort() {
		if(PORT == 0)
			PORT = IniFileReader.getWebserverPort();
		return PORT;
		
	}
	
	public static PrintWriter return200(PrintWriter out, String message, boolean json, boolean overrideDefault) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 200 OK");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		if(!overrideDefault)
			out.println((json ? "{\"code\":200,\"message\":\""+message+"\"}" : message));
		else
			out.println(message);
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return201(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 201 Created");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":201,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return400(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 400 Bad Request");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":400,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return401(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 40^1 Unauthorized");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":401,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return404(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 404 Not Found");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":404,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return405(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 405 Method Not Allowed");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":405,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return500(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 500 Internal Server Error");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":500,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}

	public static PrintWriter return501(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 501 Not Implemented");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":501,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
	
	public static PrintWriter return502(PrintWriter out, String message, boolean json) {
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 502 Bad Gateway");
		out.println("Server: Azrael Bot by xHelixStorm");
		out.println("Date: " + new Date());
		out.println("Content-type: " + (json ? "application/json" : "text/plain"));
		out.println(); // blank line between headers and content, very important !
		out.println((json ? "{\"code\":502,\"message\":\""+message+"\"}" : message));
		out.flush(); // flush character output stream buffer
		return out;
	}
}
