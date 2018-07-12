package threads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DeleteReport extends ListenerAdapter implements Runnable{
	private Path path;
	
	public DeleteReport(Path _path){
		path = _path;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
