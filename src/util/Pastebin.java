package util;

import java.net.MalformedURLException;
import java.net.URL;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

import fileManagement.IniFileReader;

public class Pastebin {
	public static String unlistedPaste(String _title, String _contents) {
		String return_link = "";
		try {
			String[] credentials = { IniFileReader.getPastebinUsername(), IniFileReader.getPastebinPassword() };
			String developerKey = IniFileReader.getPastebinKey();
			
			String title = _title; // insert your own title
			String contents = _contents; // insert your own paste contents
			int visibility = PastebinPaste.VISIBILITY_UNLISTED; // makes paste unlisted
			
			PastebinAccount account = new PastebinAccount(developerKey, credentials[0], credentials[1]);
			// fetches an user session id
			account.login();
			
			// create paste
			PastebinPaste paste = new PastebinPaste(account);
			paste.setContents(contents);
			paste.setPasteTitle(title);
			paste.setVisibility(visibility);
			paste.setPasteExpireDate(PasteExpireDate.ONE_DAY);
			
			// push paste
			PastebinLink link = paste.paste();
			return_link = link.getLink().toString();
		} catch (LoginException | PasteException e) {
			return_link = "Creating paste failed!";
		}
		return return_link;
	}
	
	public static String readPublicPasteLink(String _link) {
		String content = "";
		try {
			//read the developerKey into account
			String developerKey = IniFileReader.getPastebinKey();			
			PastebinAccount account = new PastebinAccount(developerKey);
			
			//convert String URL and fetch the content of the link
			URL url = new URL(_link);
			PastebinPaste paste = new PastebinPaste(account);
			PastebinLink link = new PastebinLink(paste, url);
			link.fetchContent();
			content = link.getPaste().getContents();
		} catch (MalformedURLException e) {
			content = "Reading paste failed!";
		}
		return content;
	}
}
