package google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.Document;

public class GoogleDocuments {
	
	/**
	 * Retrieve documents service
	 * @return Documents service
	 * @throws Exception Any error along the way
	 */
	
	public static Docs getDocumentClientService() throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Docs.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, "docs"))
			.setApplicationName("Azrael")
			.build();
	}
	
	/**
	 * Retrieve a specific document
	 * @param service Document client service
	 * @param document_id ID of the document to retrieve
	 * @return Document Object
	 * @throws Exception Any error along the way
	 */
	
	public static Document getDocument(final Docs service, final String document_id) throws Exception {
		//get document
		return service.documents().get(document_id).execute();
	}
}
