package util;

import java.util.List;

import core.Channels;
import fileManagement.FileSetting;

public class STATIC {
	
	private static final String VERSION_OLD = FileSetting.readFile("./files/version.azr");
	private static final String VERSION_NEW = "12.0.1";
	
	public static String getVersion_Old() {
		return VERSION_OLD;
	}
	public static String getVersion_New() {
		return VERSION_NEW;
	}
	
	public static String getChannels(List<Channels> channels) {
		StringBuilder out = new StringBuilder();
		var first = true;
		var last = channels.size()-1;
		for(Channels channel : channels) {
			if(first)
				out.append("<#"+channel.getChannel_ID()+">");
			else if(channels.get(last).getChannel_ID() == channel.getChannel_ID())
				out.append(" or <#"+channel.getChannel_ID()+">");
			else
				out.append(", <#"+channel.getChannel_ID()+">");
		}
		return out.toString();
	}
}
