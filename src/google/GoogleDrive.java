package google;

import java.io.IOException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

public class GoogleDrive {
	
	/**
	 * Retrieve Drive client service
	 * @param guild_id to retrieve the credentials from
	 * @return Drive service
	 * @throws Exception Any error along the way
	 */
	
	public static Drive getDriveClientService() throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Drive.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, "drive"))
			.setApplicationName("Azrael")
			.build();
	}
	
	/**
	 * Transfer ownership of a file to a user over email and drive
	 * @param service Drive service
	 * @param file_id Id name of the file
	 * @param email Email address to pass on the ownership
	 * @throws IOException
	 */
	
	public static void transferOwnerOfFile(final Drive service, String file_id, String email) throws IOException {
		Permission userPermission = new Permission().setType("user").setRole("owner").setEmailAddress(email);
		service.permissions().create(file_id, userPermission).setFields("id").setTransferOwnership(true).execute();
	}
}
