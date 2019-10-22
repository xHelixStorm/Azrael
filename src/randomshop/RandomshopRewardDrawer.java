package randomshop;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.Weapons;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RandomshopRewardDrawer {
	private final static Logger logger = LoggerFactory.getLogger(RandomshopRewardDrawer.class);
	
	public static void drawReward(GuildMessageReceivedEvent e, Weapons weapon, long currency, Guilds guild_settings) {
		try {
			var theme_id = guild_settings.getThemeID();
			BufferedImage rewardOverlay = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Randomshop/"+weapon.getOverlayName()+".png"));
			BufferedImage drawWeapon = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Weapons/"+weapon.getDescription()+".png"));
			
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
			
			e.getChannel().sendMessage("Congratulations "+e.getMember().getEffectiveName()+", you have received **"+weapon.getDescription()+" "+weapon.getStatDescription()+"**. Remaining balance: **"+currency+"**").queue();
		} catch(IOException e1) {
			logger.error("An error occurred while printing the random shop price", e1);
		}
	}
}
