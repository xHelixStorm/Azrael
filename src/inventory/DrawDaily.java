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

import constructors.Guilds;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class DrawDaily {
	private final static Logger logger = LoggerFactory.getLogger(DrawDaily.class);
	
	public static void draw(GuildMessageReceivedEvent _e, String _reward, Guilds guild_settings) {
		try {
			var theme_id = guild_settings.getThemeID();
			BufferedImage daily = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Dailies/daily_blank.png"));
			BufferedImage reward = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Dailies/"+_reward+".png"));
			
			final int rewardX = guild_settings.getDailyRewardX();
			final int rewardY = guild_settings.getDailyRewardY();
			final int generalTextFontSize = guild_settings.getDailyTextFontSize();
			final int descriptionMode = guild_settings.getDailyDescriptionMode();
			final int descriptionX = guild_settings.getDailyDescriptionX();
			final int descriptionY = guild_settings.getDailyDescriptionY();
			final int descriptionStartX = guild_settings.getDailyDescriptionStartX();
			final int fieldSizeX = guild_settings.getDailyFieldSizeX();
			
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
		} catch(IOException ioe) {
			_e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(_e.getMember(), Translation.EMBED_TITLE_ERROR)+_reward).setDescription(STATIC.getTranslation(_e.getMember(), Translation.DAILY_ERROR_1)).build()).queue();
			logger.error("Error on drawing the daily reward in guild {}", _e.getGuild().getId(), ioe);
		}
		File file1 = new File(IniFileReader.getTempDirectory()+"daily_gu"+_e.getGuild().getId()+"us"+_e.getMember().getUser().getId()+".png");
		_e.getChannel().sendFile(file1, "daily.png").complete();
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
