package inventory;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

public class InventoryBuilder{
	public static void DrawInventory(MessageReceivedEvent _e, GuildMessageReactionAddEvent _e2, String _inventory_tab, String _sub_tab, ArrayList<InventoryContent> _items, int _current_page, int _max_page){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			try{
				BufferedImage blank_inventory = ImageIO.read(new File("./files/RankingSystem/Inventory/inventory_blank.png"));
				BufferedImage inventory_tab = ImageIO.read(new File("./files/RankingSystem/Inventory/inventory_"+_inventory_tab+"_"+_sub_tab+".png"));
				
				int [] inven = GuildIni.getWholeInventory((_e != null ? _e.getGuild().getIdLong() : _e2.getGuild().getIdLong()));
				final int startX = inven[0];
				final int startY = inven[1];
				final int tabX = inven[2];
				final int tabY = inven[3];
				final int pageFontSize = inven[4];
				final int pageX = inven[5];
				final int pageY = inven[6];
				final int generalTextFontSize = inven[7];
				final int boxSizeX = inven[8];
				final int boxSizeY = inven[9];
				final int descriptionY = inven[10];
				final int itemSizeX = inven[11];
				final int itemSizeY = inven[12];
				final int nextBoxX = inven[13];
				final int nextBoxY = inven[14];
				final int expiration_positionY = inven[15];
				final int rowLimit = inven[16];
				
				int inventory_Width = blank_inventory.getWidth();
				int inventory_Height = blank_inventory.getHeight();
				BufferedImage overlay = new BufferedImage(inventory_Width, inventory_Height, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(blank_inventory, 0, 0, null);
				g.drawImage(inventory_tab, tabX, tabY, null);
				g.setFont(new Font("Nexa Bold", Font.BOLD, pageFontSize));
				g.drawString(_current_page+"/"+_max_page, getRightString(""+_current_page, pageX, g), pageY);
				g.setFont(new Font("Nexa Bold", Font.BOLD, generalTextFontSize));
				
				int i = 0;
				var currentX = startX;
				var currentY = startY;
				for(InventoryContent inventory : _items){
					i++;
					BufferedImage item;
					if(inventory.getType() != null)
						item = ImageIO.read(new File("./files/RankingSystem/Inventory/items/"+inventory.getDescription()+".png"));
					else
						item = ImageIO.read(new File("./files/RankingSystem/Inventory/weapons/"+inventory.getWeaponDescription()+".png"));
					g.drawImage(item, currentX+boxSizeX-(item.getWidth()/2), currentY+boxSizeY-(item.getHeight()/2), (itemSizeX != 0 ? itemSizeX : item.getWidth()), (itemSizeY != 0 ? itemSizeY : item.getHeight()), null);
					g.drawString((inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+ " "+inventory.getStat()), currentX+getCenteredString(inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+ " "+inventory.getStat(), boxSizeX, g), currentY+boxSizeY+descriptionY);
					if(inventory.getType() == null || inventory.getType().equals("ite")){
						if(inventory.getStatus().equals("limit")){
							long time = inventory.getExpiration().getTime()-System.currentTimeMillis();
							long days = time/1000/60/60/24;
							long hours = time/1000/60/60%24;
							long minutes = time/1000/60-(days*24*60)-(hours*60);
							g.drawString(days+"D "+hours+"H "+minutes+"M", currentX+getCenteredString(days+"D "+hours+"H "+minutes+"M", boxSizeX, g), currentY+boxSizeY+expiration_positionY);
						}
						else{
							g.drawString(inventory.getNumber()+"x", currentX+getCenteredString(inventory.getNumber()+"x", boxSizeX, g), currentY+boxSizeY+expiration_positionY);
						}
					}
					if(i % rowLimit != 0) {
						currentX += nextBoxX;
					}
					else {
						currentX = startX;
						currentY += nextBoxY;
					}
				}
				
				if(_e != null)
					ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/"+_e.getMember().getUser().getId()+"_inventory.png"));
				else
					ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/"+_e2.getMember().getUser().getId()+"_inventory.png"));
			} catch(IOException ioe){
				Logger logger = LoggerFactory.getLogger(InventoryBuilder.class);
				logger.warn("Inventory tab not found", ioe);
			}
			
			if(_e != null) {
				File upload = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/"+_e.getMember().getUser().getId()+"_inventory.png");
				_e.getTextChannel().sendFile(upload, "inventory.png", null).complete();
				upload.delete();
			}
			else {
				File upload = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/"+_e2.getMember().getUser().getId()+"_inventory.png");
				_e2.getChannel().sendFile(upload, "inventory.png", null).complete();
				upload.delete();
			}
		});
		executor.shutdown();
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
	
	private static int getRightString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - fm.stringWidth(s);
	    return x;
	}
}
