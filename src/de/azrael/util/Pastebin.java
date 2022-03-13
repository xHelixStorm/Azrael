package de.azrael.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.jpastebin.exceptions.PasteException;
import org.jpastebin.pastebin.PasteExpireDate;
import org.jpastebin.pastebin.PastebinLink;
import org.jpastebin.pastebin.PastebinPaste;
import org.jpastebin.pastebin.account.PastebinAccount;
import org.jpastebin.pastebin.exceptions.LoginException;
import org.jpastebin.pastebin.exceptions.ParseException;

public class Pastebin {
	private final static String PASTEBIN_API_KEY = "PASTEBIN_API_KEY";
	private final static String PASTEBIN_USER = "PASTEBIN_USER";
	private final static String PASTEBIN_PASS = "PASTEBIN_PASS";
	
	public static String unlistedPaste(String _title, String _contents) throws LoginException, PasteException, IllegalStateException {
		final String key = System.getProperty(PASTEBIN_API_KEY);
		final String user = System.getProperty(PASTEBIN_USER);
		final String pass = System.getProperty(PASTEBIN_PASS);
		
		String title = _title; // insert your own title
		String contents = _contents; // insert your own paste contents
		int visibility = PastebinPaste.VISIBILITY_UNLISTED; // makes paste unlisted
		
		PastebinAccount account = new PastebinAccount(key, user, pass);
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
		
		PastebinAccount account = new PastebinAccount(System.getProperty(PASTEBIN_API_KEY));
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
	
	public static String unlistedPermanentPaste(String _title, String _contents) throws LoginException, PasteException, IllegalStateException, RuntimeException {
		final String key = System.getProperty(PASTEBIN_API_KEY);
		final String user = System.getProperty(PASTEBIN_USER);
		final String pass = System.getProperty(PASTEBIN_PASS);
		
		String title = _title; // insert your own title
		String contents = _contents; // insert your own paste contents
		int visibility = PastebinPaste.VISIBILITY_UNLISTED; // makes paste unlisted
		
		PastebinAccount account = new PastebinAccount(key, user, pass);
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
	
	public static String readPasteLink(String _link) throws MalformedURLException, RuntimeException, LoginException, ParseException {
		final String key = System.getProperty(PASTEBIN_API_KEY);
		final String user = System.getProperty(PASTEBIN_USER);
		final String pass = System.getProperty(PASTEBIN_PASS);
		
		PastebinAccount account = null;
		if(user.length() > 0 && pass.length() > 0) {
			//private pastes
			account = new PastebinAccount(key, user, pass);
			account.login();
			
			PastebinLink[] pastes = account.getPastes(1000);
			for(final var paste : pastes) {
				if(paste.getLink().toString().equals(_link)) {
					paste.fetchContent();
					return paste.getPaste().getContents();
				}
			}
		}
		else {
			//public and unlisted pastes
			account = new PastebinAccount(key);
		}
		
		//convert String URL and fetch the content of the link
		URL url = new URL(_link);
		PastebinPaste paste = new PastebinPaste(account);
		PastebinLink link = new PastebinLink(paste, url);
		link.fetchContent();
		return link.getPaste().getContents();
	}
}
