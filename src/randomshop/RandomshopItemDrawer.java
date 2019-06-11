package randomshop;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Weapons;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

public class RandomshopItemDrawer {
	public static void drawItems(MessageReceivedEvent e, GuildMessageReactionAddEvent e2, List<Weapons> weapons, int current_page, int last_page) {
		try {
			BufferedImage randomshop = ImageIO.read(new File("./files/RankingSystem/Inventory/randomshop_blank.png"));
			
			int [] rand = GuildIni.getWholeRandomshopItems((e != null ? e.getGuild().getIdLong() : e2.getGuild().getIdLong()));
			final var startX = rand[0];
			final var startY = rand[1];
			final var pageX = rand[2];
			final var pageY = rand[3];
			final var generalFontSize = rand[4];
			final var sizeX = rand[5];
			final var sizeY = rand[6];
			final var itemSizeX = rand[7];
			final var itemSizeY = rand[8];
			final var moveX = rand[9];
			final var moveY = rand[10];
			final var rowLimit = rand[11];
			
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
				e.getTextChannel().sendFile(file1, "randomshop.png", null).complete();
			else
				e2.getChannel().sendFile(file1, "randomshop.png", null).complete();
			file1.delete();
		} catch (IOException e1) {
			Logger logger = LoggerFactory.getLogger(RandomshopItemDrawer.class);
			logger.error("Randomshop Items couldn't be drawn", e1);
		}
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
}
