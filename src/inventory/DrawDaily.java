package inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DrawDaily {
	public static void draw(MessageReceivedEvent _e, String _reward, int theme_id) {
		try {
			BufferedImage daily = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Dailies/daily_blank.png"));
			BufferedImage reward = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"Dailies/"+_reward+".png"));
			
			int [] dail = GuildIni.getWholeDaily(_e.getGuild().getIdLong());
			final int rewardX = dail[0];
			final int rewardY = dail[1];
			final int generalTextFontSize = dail[2];
			final int descriptionMode = dail[3];
			final int descriptionX = dail[4];
			final int descriptionY = dail[5];
			final int descriptionStartX = dail[6];
			final int fieldSizeX = dail[7];
			
			int dailyW = daily.getWidth();
			int dailyH = daily.getHeight();
			BufferedImage overlay = new BufferedImage(dailyW, dailyH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(daily, 0, 0, null);
			g.drawImage(reward, rewardX-(reward.getWidth()/2), rewardY-(reward.getHeight()/2), null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, generalTextFontSize));
			if(descriptionMode == 0)
				g.drawString(_reward, descriptionX, descriptionY);
			else if(descriptionMode == 1)
				g.drawString(_reward, descriptionStartX+getCenteredString(_reward, fieldSizeX, g), descriptionY);
			else if(descriptionMode == 2)
				g.drawString(_reward, getRightString(_reward, descriptionX, g),  descriptionY);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"daily_gu"+_e.getGuild().getId()+"us"+_e.getMember().getUser().getId()+".png"));
			g.dispose();
		} catch(IOException ioe){
			Logger logger = LoggerFactory.getLogger(DrawDaily.class);
			logger.error("Error on daily reward drawing", ioe);
		}
		File file1 = new File(IniFileReader.getTempDirectory()+"daily_gu"+_e.getGuild().getId()+"us"+_e.getMember().getUser().getId()+".png");
		_e.getTextChannel().sendFile(file1, "daily.png").complete();
		file1.delete();
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = (w/2) - (fm.stringWidth(s)/2);
	    return x;
	}
	
	private static int getRightString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - fm.stringWidth(s);
	    return x;
	}
}
