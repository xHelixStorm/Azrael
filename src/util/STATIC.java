package util;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import constructors.Channels;

public class STATIC {
	
	private static final String VERSION = "5.11.273";
	private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();
	private static final CopyOnWriteArrayList<Timer> timers = new CopyOnWriteArrayList<Timer>();
	
	public static String getVersion() {
		return VERSION;
	}
	
	public static void addThread(Thread thread, final String name) {
		if(threads.parallelStream().filter(f -> f.getName().equals(name)).findAny().orElse(null) != null)
			return;
		
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
	
	public static void addTimer(Timer timer) {
		if(timers.parallelStream().filter(f -> f.equals(timer)).findAny().orElse(null) != null)
			return;
		
		timers.add(timer);
	}
	
	public static void killAllTimers() {
		for(Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
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
	
	public static int getLevel(String category) {
		switch(category) {
			case "adm": return 100;
			case "mod": return 20;
			case "com": return 1;
			case "bot": return 10;
			case "mut": return 0;
			case "rea": return 1;
			default : 	return 0;
		}
	}
}
