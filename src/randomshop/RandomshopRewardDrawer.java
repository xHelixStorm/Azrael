package randomshop;

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
import rankingSystem.Weapons;

public class RandomshopRewardDrawer {
	public static void drawReward(MessageReceivedEvent e, Weapons weapon, long currency) {
		try {
			BufferedImage rewardOverlay = ImageIO.read(new File("./files/RankingSystem/Inventory/"+weapon.getOverlayName()+".png"));
			BufferedImage drawWeapon = ImageIO.read(new File("./files/RankingSystem/Inventory/weapons/"+weapon.getDescription()+".png"));
			
			int overlayW = rewardOverlay.getWidth();
			int overlayH = rewardOverlay.getHeight();
			
			int rewardX = (overlayW/2)-(drawWeapon.getWidth()/2);
			int rewardY = (overlayH/2)-(drawWeapon.getHeight()/2);
			
			BufferedImage overlay = new BufferedImage(overlayW, overlayH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(rewardOverlay, 0, 0, null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(drawWeapon, rewardX, rewardY, null);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_reward_"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_reward_"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file1, "reward.png", null).complete();
			file1.delete();
			
			e.getTextChannel().sendMessage("Congratulations "+e.getMember().getEffectiveName()+", you have received **"+weapon.getDescription()+" "+weapon.getStatDescription()+"**. Remaining balance: **"+currency+"**").queue();
		} catch(IOException e1) {
			Logger logger = LoggerFactory.getLogger(RandomshopRewardDrawer.class);
			logger.error("An error occurred while printing the random shop price", e1);
		}
	}
}
