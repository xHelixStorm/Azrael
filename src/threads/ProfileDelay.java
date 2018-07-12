package threads;

import java.io.File;

public class ProfileDelay implements Runnable{
	File file;
	
	public ProfileDelay(String _file){
		file = new File(_file);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		file.delete();
	}

}
