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
				
				int startX = 32;
				int startY = 198;
				
				int inventory_Width = blank_inventory.getWidth();
				int inventory_Height = blank_inventory.getHeight();
				BufferedImage overlay = new BufferedImage(inventory_Width, inventory_Height, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(blank_inventory, 0, 0, null);
				g.drawImage(inventory_tab, 13, 3, null);
				g.setFont(new Font("Nexa Bold", Font.BOLD, 40));
				g.drawString(_current_page+"/"+_max_page, getRightString(""+_current_page, 1220, g), 1810);
				g.setFont(new Font("Nexa Bold", Font.BOLD, 36));
				
				int i = 1;
				for(InventoryContent inventory : _items){
					int x = getDrawPositionX(i);
					int y = getDrawPositionY(i);
					BufferedImage item;
					if(inventory.getType() != null)
						item = ImageIO.read(new File("./files/RankingSystem/Inventory/items/"+inventory.getDescription()+".png"));
					else
						item = ImageIO.read(new File("./files/RankingSystem/Inventory/weapons/"+inventory.getWeaponDescription()+".png"));
					g.drawImage(item, startX+289-(item.getWidth()/2)+x, startY+243-(item.getHeight()/2)+y, null);
					g.drawString((inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+ " "+inventory.getStat()), startX+getCenteredString(inventory.getDescription() != null ? inventory.getDescription() : inventory.getWeaponDescription()+ " "+inventory.getStat(), 288, g)+x, startY+243+180+y);
					if(inventory.getType() == null || inventory.getType().equals("ite")){
						if(inventory.getStatus().equals("limit")){
							long time = inventory.getExpiration().getTime()-System.currentTimeMillis();
							long days = time/1000/60/60/24;
							long hours = time/1000/60/60%24;
							long minutes = time/1000/60-(days*24*60)-(hours*60);
							g.drawString(days+"D "+hours+"H "+minutes+"M", startX+getCenteredString(days+"D "+hours+"H "+minutes+"M", 288, g)+x, startY+243+230+y);
						}
						else{
							g.drawString(inventory.getNumber()+"x", startX+getCenteredString(inventory.getNumber()+"x", 288, g)+x, startY+243+230+y);
						}
					}
					i++;
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
	
	private static int getDrawPositionX(int i){
		int x = 0;
		
		switch(i){
			case 1:
			case 5:
			case 9:
				x = 0;
				break;
			case 2:
			case 6:
			case 10:
				x = 603;
				break;
			case 3:
			case 7:
			case 11:
				x = 603*2;
				break;
			case 4:
			case 8:
			case 12:
				x = 603*3;
		}
		return x;
	}
	
	private static int getDrawPositionY(int i){
		int y = 0;
		
		switch(i){
			case 1:
			case 2:
			case 3:
			case 4:
				y = 0;
				break;
			case 5:
			case 6:
			case 7:
			case 8:
				y = 496;
				break;
			case 9:
			case 10:
			case 11:
			case 12:
				y = 496*2;
				break;
			}
		return y;
	}
}
