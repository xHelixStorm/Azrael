package docs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;

public class GoogleDocuments {
	private static final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);
	
	/**
	 * Retrieve credentials
	 * @param httpTransport Trusted GoogleNetHttpTransport
	 * @param guild_id load json from location
	 * @return Credential
	 * @throws Exception File error / other errors
	 */
	
	public static Credential getCredentials(final NetHttpTransport httpTransport, final long guild_id) throws Exception {
		//Load client secrets
		InputStream in = new FileInputStream(new File("files/Guilds/"+guild_id+"/credentials.json"));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jacksonFactory, new InputStreamReader(in));
		
		//Build flow and trigger user authorization request
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jacksonFactory, clientSecrets, SCOPES)
			.setDataStoreFactory(new FileDataStoreFactory(new File("./files/"+guild_id+"/Tokens")))
			.setAccessType("offline")
			.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	/**
	 * Retrieve document
	 * @param guild_id load json from location
	 * @return Document
	 * @throws Exception
	 */
	
	public static Document getDocument(final long guild_id) throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		Docs service = new Docs.Builder(httpTransport, jacksonFactory, getCredentials(httpTransport, guild_id))
			.setApplicationName("Azrael")
			.build();
		
		return service.documents().get("1s3Y9upzwKY95Rwgo6691AOcA-ZYYbH1mb4o7MBWa72I").execute();
	}
}
