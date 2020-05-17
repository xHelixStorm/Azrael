package util;

import java.net.MalformedURLException;
import java.net.URL;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.jpaste.pastebin.account.PastebinAccount;
import org.jpaste.pastebin.exceptions.LoginException;

import fileManagement.GuildIni;
import fileManagement.IniFileReader;

public class Pastebin {
	public static String unlistedPaste(String _title, String _contents, long guild_id) throws LoginException, PasteException, IllegalStateException {
		String[] credentials = GuildIni.getPastebinCredentials(guild_id);
		
		String title = _title; // insert your own title
		String contents = _contents; // insert your own paste contents
		int visibility = PastebinPaste.VISIBILITY_UNLISTED; // makes paste unlisted
		
		PastebinAccount account = new PastebinAccount(IniFileReader.getPastebinDeveloperKey(), credentials[0], credentials[1]);
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
		return link.getLink().toString();
	}
	
	public static String GuestPaste(String _title, String _contents) throws LoginException, PasteException, IllegalStateException {
		String title = _title; // insert your own title
		String contents = _contents; // insert your own paste contents
		int visibility = PastebinPaste.VISIBILITY_PUBLIC; // makes paste unlisted
		
		PastebinAccount account = new PastebinAccount(IniFileReader.getPastebinDeveloperKey());
		// fetches an user session id
		account.login();
		
		// create paste
		PastebinPaste paste = new PastebinPaste(account);
		paste.setContents(contents);
		paste.setPasteTitle(title);
		paste.setVisibility(visibility);
		paste.setPasteExpireDate(PasteExpireDate.ONE_HOUR);
		
		// push paste
		PastebinLink link = paste.paste();
		return link.getLink().toString();
	}
	
	public static String unlistedPermanentPaste(String _title, String _contents, long guild_id) throws LoginException, PasteException, IllegalStateException, RuntimeException {
		String[] credentials = GuildIni.getPastebinCredentials(guild_id);
		
		String title = _title; // insert your own title
		String contents = _contents; // insert your own paste contents
		int visibility = PastebinPaste.VISIBILITY_UNLISTED; // makes paste unlisted
		
		PastebinAccount account = new PastebinAccount(IniFileReader.getPastebinDeveloperKey(), credentials[0], credentials[1]);
		// fetches an user session id
		account.login();
		
		// create paste
		PastebinPaste paste = new PastebinPaste(account);
		paste.setContents(contents);
		paste.setPasteTitle(title);
		paste.setVisibility(visibility);
		paste.setPasteExpireDate(PasteExpireDate.NEVER);
		
		// push paste
		PastebinLink link = paste.paste();
		return link.getLink().toString();
	}
	
	public static String readPublicPasteLink(String _link) throws MalformedURLException, RuntimeException {
		//read the developerKey into account
		String developerKey = IniFileReader.getPastebinDeveloperKey();
		PastebinAccount account = new PastebinAccount(developerKey);
		
		//convert String URL and fetch the content of the link
		URL url = new URL(_link);
		PastebinPaste paste = new PastebinPaste(account);
		PastebinLink link = new PastebinLink(paste, url);
		link.fetchContent();
		return link.getPaste().getContents();
	}
}
