package google;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;

/**
 * Utils for google docs and spreadsheets API
 * @author xHelixStorm
 *
 */

@SuppressWarnings("deprecation")
public class GoogleUtils {
	private static final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPESDOCS = Collections.singletonList(DocsScopes.DOCUMENTS);
	private static final List<String> SCOPESSHEETS = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final List<String> SCOPESDRIVE = Collections.singletonList(DriveScopes.DRIVE);
	
	/**
	 * Retrieve credentials
	 * @param httpTransport Trusted GoogleNetHttpTransport
	 * @return Credential
	 * @throws Exception File error / other errors
	 */
	
	@SuppressWarnings({ "preview" })
	public static Credential getCredentials(final NetHttpTransport httpTransport, final String type) throws Exception {
		//use the fitting scopes
		List<String> scopes;
		switch(type) {
			case "docs" -> scopes = SCOPESDOCS;
			case "sheets" -> scopes = SCOPESSHEETS;
			case "drive" -> scopes = SCOPESDRIVE;
			default -> scopes = null;
		}
		
		return GoogleCredential.fromStream(new FileInputStream(new File("files/Google/credentials.json"))).createScoped(scopes);
	}
	
	/**
	 * Retrieve jackson factory
	 * @return jacksonFactory
	 */
	
	public static JacksonFactory getJacksonFactory() {
		return jacksonFactory;
	}
	
	/**
	 * Build a url depending on the api id
	 * @param file_id
	 * @param api_id
	 * @return
	 */
	
	@SuppressWarnings("preview")
	public static String buildFileURL(String file_id, int api_id) {
		return switch(api_id) {
			case 1 -> "https://docs.google.com/document/d/"+file_id;
			case 2 -> "https://docs.google.com/spreadsheets/d/"+file_id;
			default -> "";
		};
	}
}
