package de.azrael.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;

import de.azrael.util.STATIC;

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
	 * Search for YouTube channels
	 * @param service YouTube client service
	 * @param channel_id YouTube channel id
	 * @return query results
	 * @throws IOException
	 */
	
	public static ChannelListResponse searchYouTubeChannel(final YouTube service, final String channel_id) throws IOException {
		//Search YouTube Videos from a channel
		YouTube.Channels.List request = service.channels().list("snippet");
		return request.setId(channel_id).setMaxResults(1l).execute();
	}
	
	/**
	 * 
	 * @param name YouTube search criteria
	 * @return search result json array
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	
	public static JSONArray collectYouTubeVideos(String name) throws SocketTimeoutException, IOException {
		if(name.contains(" ")) {
			StringBuilder formattedName = new StringBuilder();
			for(String subName : name.split(" ")) {
				if(formattedName.length() > 0)
					formattedName.append("+");
				formattedName.append(subName);
			}
			name = formattedName.toString();
		}
		final BufferedReader bf = STATIC.retrieveWebPageCode("https://www.youtube.com/results?search_query="+name+"&h1=en&persist_h1=1");
		StringBuilder out = new StringBuilder(); 
		String line = "";
		while((line = bf.readLine()) != null) {
			out.append(line);
		}
		Document document = Jsoup.parse(out.toString());
		Matcher matcher = Pattern.compile("\\{\"responseContext\":.*\\};").matcher(document.body().toString());
		if(matcher.find()) {
			String result = matcher.group();
			result = result.replace("]};", "]}");
			JSONObject json = new JSONObject(result);
			json = json.getJSONObject("contents");
			json = json.getJSONObject("twoColumnSearchResultsRenderer");
			json = json.getJSONObject("primaryContents");
			json = json.getJSONObject("sectionListRenderer");
			json = json.getJSONArray("contents").getJSONObject(0);
			json = json.getJSONObject("itemSectionRenderer");
			return json.getJSONArray("contents");
		}
		return null;
	}
}
