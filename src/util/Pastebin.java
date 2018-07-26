package util;

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
			e.printStackTrace();
			return_link = "Creating paste failed!";
		}
		return return_link;
	}
}
