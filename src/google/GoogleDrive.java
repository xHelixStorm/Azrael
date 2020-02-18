package google;

import java.io.IOException;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

public class GoogleDrive {
	public static Drive getDriveClientService() throws Exception {
		//Build a new authorized API client service
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new Drive.Builder(httpTransport, GoogleUtils.getJacksonFactory(), GoogleUtils.getCredentials(httpTransport, "drive"))
			.setApplicationName("Azrael")
			.build();
	}
	
	public static void transferOwnerOfFile(final Drive service, String file_id) throws IOException {
		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
			@Override
			public void onSuccess(Permission arg0, HttpHeaders arg1) throws IOException {
				System.out.println();
			}

			@Override
			public void onFailure(GoogleJsonError arg0, HttpHeaders arg1) throws IOException {
				System.out.println();
			}
		};
		
		BatchRequest batch = service.batch();
		Permission userPermission = new Permission().setType("user").setRole("owner").setEmailAddress("YOUR_EMAIL");
		service.permissions().create(file_id, userPermission).setFields("id").setTransferOwnership(true).queue(batch, callback);
		
		batch.execute();
	}
}
