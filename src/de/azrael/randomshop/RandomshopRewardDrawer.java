package de.azrael.randomshop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.azrael.constructors.Guilds;
import de.azrael.constructors.Weapons;
import de.azrael.enums.Translation;
import de.azrael.fileManagement.IniFileReader;
import de.azrael.util.STATIC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RandomshopRewardDrawer {
	private final static Logger logger = LoggerFactory.getLogger(RandomshopRewardDrawer.class);
	
	public static void drawReward(GuildMessageReceivedEvent e, Weapons weapon, long currency, Guilds guild_settings, long time) {
		try {
			BufferedImage rewardOverlay = ImageIO.read(new File("./files/RankingSystem/Randomshop/"+weapon.getOverlayName()+".png"));
			BufferedImage drawWeapon = ImageIO.read(new File("./files/RankingSystem/Weapons/"+weapon.getDescription()+".png"));
			
			final int overlayW = rewardOverlay.getWidth();
			final int overlayH = rewardOverlay.getHeight();
			final int itemSizeX = guild_settings.getRandomshopRewardItemSizeX();
			final int itemSizeY = guild_settings.getRandomshopRewardItemSizeY();
			final int rewardX = (overlayW/2)-((itemSizeX != 0 ? itemSizeX : drawWeapon.getWidth())/2);
			final int rewardY = (overlayH/2)-((itemSizeY != 0 ? itemSizeY : drawWeapon.getHeight())/2);
			
			BufferedImage overlay = new BufferedImage(overlayW, overlayH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(rewardOverlay, 0, 0, null);
			g.drawImage(drawWeapon, rewardX, rewardY, (itemSizeX != 0 ? itemSizeX : drawWeapon.getWidth()), (itemSizeY != 0 ? itemSizeY : drawWeapon.getHeight()), null);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"randomshop_reward_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"randomshop_reward_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getChannel().sendFile(file1, "reward.png").complete();
			file1.delete();
			
			e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_REWARD).replaceFirst("\\{\\}", weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : "")+" "+(time > 0 ? TimeUnit.MILLISECONDS.toDays(time)+STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_DAYS) : STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_PERM))).replace("{}", ""+currency)).queue();
		} catch(IOException e1) {
			if(e.getGuild().getSelfMember().hasPermission(e.getChannel(), Permission.MESSAGE_EMBED_LINKS) || STATIC.setPermissions(e.getGuild(), e.getChannel(), EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
				e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_ERR_2)+weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : "")).build()).queue();
			else
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.RANDOMSHOP_ERR_2).replace("{}", weapon.getDescription()+(weapon.getStatDescription() != null ? " "+weapon.getStatDescription() : ""))).queue();
			logger.error("An error occurred while printing the random shop prize {} in guild {}", weapon.getDescription(), e.getGuild().getId(), e1);
		}
	}
}
