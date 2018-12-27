package threads;

import java.io.File;
import java.sql.Timestamp;

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
			System.err.print("["+new Timestamp(System.currentTimeMillis())+"] ");
			e.printStackTrace();
		}
		if(file.exists()) {
			file.delete();
		}
	}
}
