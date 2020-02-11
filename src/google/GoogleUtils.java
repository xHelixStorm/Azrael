package google;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.DocsScopes;
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
	
	/**
	 * Retrieve credentials
	 * @param httpTransport Trusted GoogleNetHttpTransport
	 * @param guild_id load json from location
	 * @return Credential
	 * @throws Exception File error / other errors
	 */
	
	@SuppressWarnings({ "preview" })
	public static Credential getCredentials(final NetHttpTransport httpTransport, final long guild_id, final String type) throws Exception {
		//Load client secrets
		InputStream in = new FileInputStream(new File("files/Google/credentials.json"));
		
		//use the fitting scopes
		List<String> scopes;
		switch(type) {
			case "docs" -> scopes = SCOPESDOCS;
			case "sheets" -> scopes = SCOPESSHEETS;
			default -> scopes = null;
		}
		
		return GoogleCredential.fromStream(in).createScoped(scopes);
	}
	
	/**
	 * Retrieve jackson factory
	 * @return jacksonFactory
	 */
	
	public static JacksonFactory getJacksonFactory() {
		return jacksonFactory;
	}
}
