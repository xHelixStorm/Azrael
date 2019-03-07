package inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DrawDaily {
	public static void draw(MessageReceivedEvent _e, String _reward){
		try {
			BufferedImage daily = ImageIO.read(new File("./files/RankingSystem/Dailies/daily_blank.png"));
			BufferedImage reward = ImageIO.read(new File("./files/RankingSystem/Dailies/"+_reward+".png"));
			
			int startX = 29;
			int startY = 41;
			
			int dailyW = daily.getWidth();
			int dailyH = daily.getHeight();
			BufferedImage overlay = new BufferedImage(dailyW, dailyH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(daily, 0, 0, null);
			g.drawImage(reward, startX+181-(reward.getWidth()/2), startY+109-(reward.getHeight()/2), null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, 50));
			g.drawString(_reward, 440, 160);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/daily.png"));
			g.dispose();
		} catch(IOException ioe){
			Logger logger = LoggerFactory.getLogger(DrawDaily.class);
			logger.error("Error on daily reward drawing", ioe);
		}
		File file1 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/daily.png");
		_e.getTextChannel().sendFile(file1, "daily.png", null).complete();
		file1.delete();
	}
}
