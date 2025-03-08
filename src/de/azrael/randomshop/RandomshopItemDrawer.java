package de.azrael.randomshop;

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

import de.azrael.constructors.Guilds;
import de.azrael.constructors.Weapons;
import de.azrael.enums.Directory;
import de.azrael.enums.Translation;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

public class RandomshopItemDrawer {
	private final static Logger logger = LoggerFactory.getLogger(RandomshopItemDrawer.class);
	
	public static void drawItems(Member member, TextChannel channel, List<Weapons> weapons, int current_page, int last_page, Guilds guild_settings) {
		String traceWeapon = null;
		try {
			BufferedImage randomshop = ImageIO.read(new File(Directory.RANDOMSHOP.getPath()+"randomshop_blank.png"));
			
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
				BufferedImage currentWeapon = ImageIO.read(new File(Directory.WEAPONS.getPath()+weapon.getDescription()+".png"));
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
			ImageIO.write(overlay, "png", new File(Directory.TEMP.getPath()+"randomshop_items_gu"+member.getGuild().getId()+"us"+member.getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(Directory.TEMP.getPath()+"randomshop_items_gu"+member.getGuild().getId()+"us"+member.getUser().getId()+".png");
			channel.sendFiles(FileUpload.fromData(file1, "randomshop.png")).queue(m -> {
				file1.delete();
			});
		} catch (IOException e1) {
			if(member.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(member.getGuild(), channel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
				channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(member, Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(member, Translation.RANDOMSHOP_ERR)).build()).queue();
			else
				channel.sendMessage(STATIC.getTranslation(member, Translation.RANDOMSHOP_ERR)).queue();
			logger.error("Randomshop Items couldn't be drawn. Error for item {} in guild {}", traceWeapon, member.getGuild().getId(), e1);
		}
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
}
