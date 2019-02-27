package randomshop;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import rankingSystem.Weapons;

public class RandomshopItemDrawer {
	public static void drawItems(MessageReceivedEvent e, GuildMessageReactionAddEvent e2, List<Weapons> weapons, int current_page, int last_page) {
		try {
			BufferedImage randomshop = ImageIO.read(new File("./files/RankingSystem/Inventory/randomshop_blank.png"));
			
			final var startX = 38;
			final var startY = 220;
			
			final var sizeX = 125;
			final var sizeY = 95;
			
			final var moveX = 147;
			final var moveY = 110;
			
			int overlayW = randomshop.getWidth();
			int overlayH = randomshop.getHeight();
			
			BufferedImage overlay = new BufferedImage(overlayW, overlayH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(randomshop, 0, 0, null);
			var counter = 0;
			var currentX = startX;
			var currentY = startY;
			for(var weapon : weapons) {
				counter++;
				BufferedImage currentWeapon = ImageIO.read(new File("./files/RankingSystem/Inventory/weapons/"+weapon.getDescription()+".png"));
				g.drawImage(currentWeapon, currentX+(sizeX/2)-40, currentY+(sizeY/2)-40, 80, 80, null);
				if(counter % 5 != 0) {
					currentX += moveX;
				}
				else {
					currentX = startX;
					currentY += moveY;
				}
			}
			g.setFont(new Font("Nexa Bold", Font.BOLD, 15));
			g.drawString(current_page+" / "+ last_page, 398+getCenteredString(current_page+" / "+ last_page, 0, g), 443);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_items_"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId())+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/randomshop_items_"+(e != null ? e.getMember().getUser().getId() : e2.getMember().getUser().getId())+".png");
			if(e != null)
				e.getTextChannel().sendFile(file1, "randomshop.png", null).complete();
			else
				e2.getChannel().sendFile(file1, "randomshop.png", null).complete();
			file1.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
}
