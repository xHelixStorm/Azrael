package inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import util.STATIC;

public class DrawDaily {
	private final static Logger logger = LoggerFactory.getLogger(DrawDaily.class);
	
	public static void draw(GuildMessageReceivedEvent e, String obtained, Guilds guild_settings) {
		try {
			BufferedImage daily = ImageIO.read(new File("./files/RankingSystem/Dailies/daily_blank.png"));
			BufferedImage reward = ImageIO.read(new File("./files/RankingSystem/Dailies/"+obtained+".png"));
			
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
				g.drawString(obtained, descriptionX, descriptionY);
			else if(descriptionMode == 1)
				g.drawString(obtained, descriptionStartX+getCenteredString(obtained, fieldSizeX, g), descriptionY);
			else if(descriptionMode == 2)
				g.drawString(obtained, getRightString(obtained, descriptionX, g),  descriptionY);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"daily_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			final File file1 = new File(IniFileReader.getTempDirectory()+"daily_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getChannel().sendFile(file1, "daily.png").queue(message -> {
				file1.delete();
			});
		} catch(IOException ioe) {
			if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.DAILY_ERROR_1)+obtained).build()).queue();
			else
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.DAILY_ERROR_1)+obtained).queue();
			logger.error("Daily reward couldn't be printed for user {} and reward {} in guild {}", e.getMember().getUser().getId(), obtained, e.getGuild().getId(), ioe);
		}
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
