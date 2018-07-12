package rankingSystem;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import util.STATIC;

public class RankingMethods extends ListenerAdapter{
	public static void getRankUp(MessageReceivedEvent e , int _level, int _level_skin, int _icon_skin, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try {
			BufferedImage rankUp = ImageIO.read(new File("./pictures/RankingSystem/S4League/levelup"+_level_skin+"_blank.png"));
			BufferedImage rank = ImageIO.read(new File("./pictures/RankingSystem/S4League/Rank/level_"+_icon_skin+"_"+_level+".jpg"));
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int level = _level;
			int level1;
			int level2;
			String levelS1 = null;
			String levelS2 = null;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			if(level > 9){
				level1 = level / 10;
				level2 = level % 10;
				sb.append(level1);
				levelS1 = sb.toString();
				sb2.append(level2);
				levelS2 = sb2.toString();
			}
			else{
				levelS1 = "0";
				sb.append(level);
				levelS2 = sb.toString();
			}
			
			if(characterCounter > 10){name = name.substring(0, 10);}
			
			int rankUpW = rankUp.getWidth();
			int rankUpH = rankUp.getHeight();
			BufferedImage overlay = new BufferedImage(rankUpW, rankUpH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(rankUp, 0, 0, null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(rank, _rankx, _ranky, _rank_width, _rank_height, null);
			Color color = new Color(_color_r, _color_g, _color_b);
			g.setColor(color);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(new Font("Nexa Bold", Font.BOLD, 32));
			g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, 103, g), 65);
			g.setFont(new Font("Nexa Bold", Font.BOLD, 23));
			g.drawString(name, 137, 68);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/lvup_"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/lvup_"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file1, "level_up.png", null).complete();
			file1.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void getRank(MessageReceivedEvent e, int _experience, int _level, int _rank_skin, int _icon_skin, int _bar_color, boolean _additional_text, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try{
			STATIC.allowCertificates();
			
			BufferedImage rank = ImageIO.read(new File("./pictures/RankingSystem/S4League/rank"+_rank_skin+"_blank.png"));
			BufferedImage experienceBar = ImageIO.read(new File("./pictures/RankingSystem/S4League/ExperienceBar/exp"+_bar_color+"_"+_experience+".png"));
			BufferedImage level = ImageIO.read(new File("./pictures/RankingSystem/S4League/Rank/level_"+_icon_skin+"_"+_level+".jpg"));
			String avatar = "";
			try{
				avatar = e.getMember().getUser().getAvatarUrl();
			} catch(NullPointerException npe){
				avatar = e.getMember().getUser().getDefaultAvatarUrl();
			}
			final String urlStr = avatar;
			final URL url = new URL(urlStr);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int levelT = _experience;
			
			if(characterCounter > 10){name = name.substring(0, 10);}
			
			int rankW = rank.getWidth();
			int rankH = rank.getHeight();
			BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(rank, 0, 0, null);
			g.drawImage(experienceBar, 38, 64, null);
			g.drawImage(level, _rankx, _ranky, _rank_width, _rank_height, null);
			g.drawImage(avatarPicture, 19, 19, 40, 40, null);
			Color color = new Color(_color_r, _color_g, _color_b);
			g.setColor(color);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, 12));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(_additional_text == true){
				g.drawString("Exp:", 17, 73);
				g.drawString(levelT+"%", 239, 73);
			}
			g.drawString("Rank:  #"+_level, 118, 57);
			g.setFont(new Font("Nexa Bold", Font.BOLD, 23));
			g.drawString(name, 117, 38);
			
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/rank_"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file2 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/rank_"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file2, "rank.png", null).complete();
			file2.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void getProfile(MessageReceivedEvent e, int _experiencePercentage, int _level, float _currentExperience, float _rankUpExperience, long _experience, long _currency, int _rank, int _profile_skin, int _icon_skin, int _bar_color, boolean _additional_text, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try{
			STATIC.allowCertificates();
			
			BufferedImage profile = ImageIO.read(new File("./pictures/RankingSystem/S4League/profile"+_profile_skin+"_blank.png"));
			BufferedImage experienceBar = ImageIO.read(new File("./pictures/RankingSystem/S4League/ExperienceBar/exp"+_bar_color+"_"+_experiencePercentage+".png"));
			BufferedImage level = ImageIO.read(new File("./pictures/RankingSystem/S4League/Rank/level_"+_icon_skin+"_"+_level+".jpg"));
			String avatar = "";
			try{
				avatar = e.getMember().getUser().getAvatarUrl();
			} catch(NullPointerException npe){
				avatar = e.getMember().getUser().getDefaultAvatarUrl();
			}
			final String urlStr = avatar;
			final URL url = new URL(urlStr);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int levelT = _experiencePercentage;
			int currentExperience = (int) _currentExperience;
			int rankUpExperience = (int) _rankUpExperience;
			long experience = _experience;
			long currency = _currency;
			int rank = _rank;
			
			if(characterCounter > 10){name = name.substring(0, 10);}
			
			int profileW = profile.getWidth();
			int profileH = profile.getHeight();
			BufferedImage overlay = new BufferedImage(profileW, profileH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(profile, 0, 0, null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(experienceBar, 42, 100, null);
			g.drawImage(level, _rankx, _ranky, _rank_width, _rank_height, null);
			g.drawImage(avatarPicture, 20, 20, 55, 55, null);
			Color color = new Color(_color_r, _color_g, _color_b);
			g.setColor(color);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, 13));
			if(_additional_text == true){
				g.drawString("Exp:", 15, 109);
				g.drawString(levelT+"%", 243, 109);
			}
			g.drawString(""+_level, getRightString(""+_level, 136, g), 176);
			g.drawString(""+experience, getRightString(""+experience, 136, g), 153);
			g.drawString(currentExperience+"/"+rankUpExperience, getRightString(""+currentExperience, 142, g), 123);
			g.drawString(""+currency, getRightString(""+_currency, 263, g), 153);
			g.drawString(""+rank, getRightString(""+_rank, 263, g), 176);
			g.setFont(new Font("Nexa Bold", Font.BOLD, 24));
			g.drawString(name, 126, 56);
			
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"AutoDelFiles/profile_"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file3 = new File(IniFileReader.getTempDirectory()+"AutoDelFiles/profile_"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file3, "profile.png", null).complete();
			file3.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static int getRightString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - fm.stringWidth(s);
	    return x;
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - (fm.stringWidth(s)/2);
	    return x;
	}
}
