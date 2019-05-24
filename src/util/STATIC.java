package util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import core.Channels;

public class STATIC {
	
	private static final String VERSION = "5.9.266";
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	
	public static String getVersion() {
		return VERSION;
	}
	
	public static void addThread(Thread thread, final String name) {
		thread.setName(name);
		threads.add(thread);
	}
	public static boolean killThread(final String name) {
		var thread = threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
		if(thread != null) {
			thread.interrupt();
			threads.remove(thread);
			return true;
		}
		else {
			return false;
		}
	}
	public static void removeThread(final Thread thread) {
		threads.remove(thread);
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
