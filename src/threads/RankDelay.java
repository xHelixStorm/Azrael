package threads;

import java.io.File;

public class RankDelay implements Runnable{
	File file;
	
	public RankDelay(String _file){
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
