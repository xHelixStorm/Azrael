package inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Guilds;
import constructors.InventoryContent;
import enums.Translation;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import util.STATIC;

public class InventoryBuilder {
	private final static Logger logger = LoggerFactory.getLogger(InventoryBuilder.class);
	
	public static void DrawInventory(GuildMessageReceivedEvent e, GuildMessageReactionAddEvent e2, String _inventory_tab, String _sub_tab, ArrayList<InventoryContent> _items, int _current_page, int _max_page, Guilds guild_settings) {
		int theme_id = guild_settings.getThemeID();
		if(new File("./files/RankingSystem/"+theme_id+"/Inventory/inventory_blank.png").exists()) {
			try {
				BufferedImage blank_inventory = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Inventory/inventory_blank.png"));
				BufferedImage inventory_tab = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Inventory/inventory_"+_inventory_tab+"_"+_sub_tab+".png"));
				
				final int startX = guild_settings.getInventoryStartX();
				final int startY = guild_settings.getInventoryStartY();
				final int tabX = guild_settings.getInventoryTabX();
				final int tabY = guild_settings.getInventoryTabY();
				final int pageFontSize = guild_settings.getInventoryPageFontSize();
				final int pageX = guild_settings.getInventoryPageX();
				final int pageY = guild_settings.getInventoryPageY();
				final int generalTextFontSize = guild_settings.getInventoryTextFontSize();
				final int boxSizeX = guild_settings.getInventoryBoxSizeX();
				final int boxSizeY = guild_settings.getInventoryBoxSizeY();
				final int descriptionY = guild_settings.getInventoryDescriptionY();
				final int itemSizeX = guild_settings.getInventoryItemSizeX();
				final int itemSizeY = guild_settings.getInventoryItemSizeY();
				final int nextBoxX = guild_settings.getInventoryNextBoxX();
				final int nextBoxY = guild_settings.getInventoryNextBoxY();
				final int expiration_positionY = guild_settings.getInventoryExpirationPositionY();
				final int rowLimit = guild_settings.getInventoryRowLimit();
				
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
				for(InventoryContent inventory : _items) {
					i++;
					BufferedImage item;
					if(inventory.getType() != null && inventory.getType().equals("ite"))
						item = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Items/"+inventory.getDescription()+"."+inventory.getFileType()));
					else if(inventory.getType() != null)
						item = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+inventory.getDescription()+"."+inventory.getFileType()));
					else if(inventory.getSkillDescription() == null)
						item = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Weapons/"+inventory.getWeaponDescription()+".png"));
					else
						item = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skills/"+inventory.getSkillDescription()+".png"));
					g.drawImage(item, currentX+(boxSizeX/2)-(item.getWidth()/2), currentY+boxSizeY-(item.getHeight()/2), (itemSizeX != 0 ? itemSizeX : item.getWidth()), (itemSizeY != 0 ? itemSizeY : item.getHeight()), null);
					g.drawString((inventory.getDescription() != null ? inventory.getDescription() : (inventory.getWeaponDescription() != null ? inventory.getWeaponDescription()+" "+inventory.getStat() : inventory.getSkillDescription())), currentX+getCenteredString(inventory.getDescription() != null ? inventory.getDescription() : (inventory.getWeaponDescription() != null ? inventory.getWeaponDescription()+ " "+inventory.getStat() : inventory.getSkillDescription()), boxSizeX, g), currentY+boxSizeY+descriptionY);
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
				
				if(e != null)
					ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"inventory_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
				else
					ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"inventory_gu"+e2.getGuild().getId()+"us"+e2.getMember().getUser().getId()+".png"));
			} catch(IOException ioe) {
				if(e != null) {
					e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.INVENTORY_DRAW_ERR)).build()).queue();
				}
				else {
					e2.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e2.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e2.getMember(), Translation.INVENTORY_DRAW_ERR)).build()).queue();
				}
				logger.warn("Inventory couldn't be drawn. Last item {}", ioe);
			}
			
			if(e != null) {
				File upload = new File(IniFileReader.getTempDirectory()+"inventory_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
				e.getChannel().sendFile(upload, "inventory.png").complete();
				upload.delete();
			}
			else {
				File upload = new File(IniFileReader.getTempDirectory()+"inventory_gu"+e2.getGuild().getId()+"us"+e2.getMember().getUser().getId()+".png");
				e2.getChannel().sendFile(upload, "inventory.png").complete();
				upload.delete();
			}
		}
		else {
			StringBuilder out = new StringBuilder();
			for(InventoryContent inventory : _items) {
				if(inventory.getType() != null && inventory.getType().equals("ite"))
					out.append(inventory.getDescription());
				else if(inventory.getType() != null)
					out.append(inventory.getDescription());
				else if(inventory.getSkillDescription() == null)
					out.append(inventory.getWeaponDescription()+" "+inventory.getStat());
				else
					out.append(inventory.getSkillDescription());
				if(inventory.getType() == null || inventory.getType().equals("ite")) {
					if(inventory.getStatus().equals("limit")) {
						long time = inventory.getExpiration().getTime()-System.currentTimeMillis();
						long days = time/1000/60/60/24;
						long hours = time/1000/60/60%24;
						long minutes = time/1000/60-(days*24*60)-(hours*60);
						out.append(" "+days+"D "+hours+"H "+minutes+"M");
					}
				}
				out.append("\n");
			}
			e.getChannel().sendMessage("```\n"+out.toString()+"\n```");
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
