package util;

import java.util.List;

import core.Channels;

public class STATIC {
	
	private static final String VERSION = "5.8.243";
	
	public static String getVersion() {
		return VERSION;
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
