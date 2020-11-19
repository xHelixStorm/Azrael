package google;

import java.io.IOException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;

public class GoogleYoutube {
	
	/**
	 * Retrieve YouTube client service
	 * @return YouTube service
	 * @throws Exception Any error along the way
	 */
	
	public static YouTube getService() throws Exception {
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new YouTube.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, "youtube"))
			.setApplicationName("Azrael")
			.build();
	}
	
	/**
	 * Search for YouTube Video(s)
	 * @param service YouTube client service
	 * @param query search request
	 * @param maxResults limit of videos to retrieve
	 * @return query results
	 * @throws IOException
	 */
	
	public static SearchListResponse searchYouTubeVideo(final YouTube service, final String query, final long maxResults) throws IOException {
		//Search YouTube Video
		YouTube.Search.List request = service.search().list("snippet");
		return request.setMaxResults(maxResults)
			.setQ(query)
			.setType("video")
			.execute();
	}
}
