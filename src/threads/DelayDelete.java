package threads;

import java.io.File;

import fileManagement.FileSetting;

public class DelayDelete implements Runnable{
	File file;
	long delay;
	boolean write;
	
	public DelayDelete(String _file, long _delay, boolean _write){
		file = new File(_file);
		delay = _delay;
		write = _write;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(file.exists()) {
			if(write == false) {
				file.delete();
			}
			else {
				FileSetting.createFile(file.getPath(), "expired");
			}
		}
	}
}
