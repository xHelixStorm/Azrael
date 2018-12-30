package threads;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayDelete implements Runnable{
	File file;
	long delay;
	boolean write;
	
	public DelayDelete(String _file, long _delay){
		file = new File(_file);
		delay = _delay;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			Logger logger = LoggerFactory.getLogger(DelayDelete.class);
			logger.error("Thread sleep of DelayDelete couldn't be completed", e);
		}
		if(file.exists()) {
			file.delete();
		}
	}
}
