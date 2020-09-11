package randomshop;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Weapons;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import util.STATIC;

public class RandomshopItemDrawer {
	private final static Logger logger = LoggerFactory.getLogger(RandomshopItemDrawer.class);
	
	public static void drawItems(GuildMessageReceivedEvent e, GuildMessageReactionAddEvent e2, List<Weapons> weapons, int current_page, int last_page, Guilds guild_settings) {
		String traceWeapon = null;
		try {
			var theme_id = guild_settings.getThemeID();
			BufferedImage randomshop = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Randomshop/randomshop_blank.png"));
			
			final var startX = guild_settings.getRandomshopStartX();
			final var startY = guild_settings.getRandomshopStartY();
			final var pageX = guild_settings.getRandomshopPageX();
			final var pageY = guild_settings.getRandomshopPageY();
			final var generalFontSize = guild_settings.getRandomshopTextFontSize();
			final var sizeX = guild_settings.getRandomshopBoxSizeX();
			final var sizeY = guild_settings.getRandomshopBoxSizeY();
			final var itemSizeX = guild_settings.getRandomshopItemSizeX();
			final var itemSizeY = guild_settings.getRandomshopItemSizeY();
			final var moveX = guild_settings.getRandomshopNextBoxX();
			final var moveY = guild_settings.getRandomshopNextBoxY();
			final var rowLimit = guild_settings.getRandomshopRowLimit();
			
			int overlayW = randomshop.getWidth();
			int overlayH = randomshop.getHeight();
			
			BufferedImage overlay = new BufferedImage(overlayW, overlayH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(randomshop, 0, 0, null);
			var counter = 0;
			var currentX = startX;
			var currentY = startY;
			for(var weapon : weapons) {
				traceWeapon = weapon.getDescription();
				counter++;
				BufferedImage currentWeapon = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Weapons/"+weapon.getDescription()+".png"));
				g.drawImage(currentWeapon, currentX+(sizeX/2)-(itemSizeX/2), currentY+(sizeY/2)-(itemSizeY/2), (itemSizeX != 0 ? itemSizeX : currentWeapon.getWidth()), (itemSizeY != 0 ? itemSizeY : currentWeapon.getHeight()), null);
				if(counter % rowLimit != 0) {
					currentX += moveX;
				}
				else {
					currentX = startX;
					currentY += moveY;
				}
			}
			g.setFont(new Font("Nexa Bold", Font.BOLD, generalFontSize));
			g.drawString(current_page+" / "+ last_page, pageX+getCenteredString(current_page+" / "+ last_page, 0, g), pageY);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"randomshop_items_gu"+(e != null ? e.getGuild().getId() : e2.getGuild().getId())+"us"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId())+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"randomshop_items_gu"+(e != null ? e.getGuild().getId() : e2.getGuild().getId())+"us"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId())+".png");
			if(e != null)
				e.getChannel().sendFile(file1, "randomshop.png").complete();
			else
				e2.getChannel().sendFile(file1, "randomshop.png").complete();
			file1.delete();
		} catch (IOException e1) {
			if(e != null) {
				if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_ERR)).build()).queue();
				else
					e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_ERR)).queue();
			}
			else {
				if(e2.getGuild().getSelfMember().hasPermission(e2.getChannel(), Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e2.getGuild(), e2.getChannel(), EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
					e2.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e2.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e2.getMember(), Translation.RANDOMSHOP_ERR)).build()).queue();
				else
					e2.getChannel().sendMessage(STATIC.getTranslation(e2.getMember(), Translation.RANDOMSHOP_ERR)).queue();
			}
			logger.error("Randomshop Items couldn't be drawn. Error for item {}", traceWeapon, e1);
		}
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
}
