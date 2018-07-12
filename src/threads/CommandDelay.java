package threads;

import java.io.File;

public class CommandDelay implements Runnable{
	File file;
	long delay;
	
	public CommandDelay(String _file, long _delay){
		file = new File(_file);
		delay = _delay;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		file.delete();
	}

}
