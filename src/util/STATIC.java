package util;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import fileManagement.FileSetting;

public class STATIC {
	
	private static final String VERSION_OLD = FileSetting.readFile("./files/version.azr");
	private static final String VERSION_NEW = "12.0.1";
	
	public static void allowCertificates(){	
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager(){
				public java.security.cert.X509Certificate[] getAcceptedIssuers(){
					return null;
				}
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){
				}
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){
				}
			}
		};
		
		try{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}catch(Exception ex){
		}
	}
	
	public static String getVersion_Old(){
		return VERSION_OLD;
	}
	public static String getVersion_New(){
		return VERSION_NEW;
	}
}
