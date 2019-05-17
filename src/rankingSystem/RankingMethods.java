package rankingSystem;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RankingMethods extends ListenerAdapter{
	private final static Logger logger = LoggerFactory.getLogger(RankingMethods.class);
	
	public static void getRankUp(MessageReceivedEvent e , int _level, int _level_skin, int _icon_skin, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try {
			BufferedImage rankUp = ImageIO.read(new File("./files/RankingSystem/levelup"+_level_skin+"_blank.png"));
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/Rank/level_"+_icon_skin+"_"+_level+".png"));
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int level = _level;
			int level1;
			int level2;
			String levelS1 = null;
			String levelS2 = null;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			int[] lev = GuildIni.getWholeProfile(e.getGuild().getIdLong());
			final var nameLengthLimit = lev[0];
			final var generalTextFontSize = lev[1];
			final var nameTextFontSize = lev[2];
			
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
			
			if(characterCounter > nameLengthLimit && nameLengthLimit != 0)
				name = name.substring(0, nameLengthLimit);
			
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
			g.setFont(new Font("Nexa Bold", Font.BOLD, generalTextFontSize));
			g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, 103, g), 65);
			g.setFont(new Font("Nexa Bold", Font.BOLD, nameTextFontSize));
			g.drawString(name, 137, 68);
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"lvup_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"lvup_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file1, "level_up.png", null).complete();
			file1.delete();
		} catch (IOException e1) {
			logger.error("RankUp couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getRank(MessageReceivedEvent e, String _name, String _avatar, int _experience, int _level, int _rank, int _rank_skin, int _icon_skin, int _bar_color, boolean _additional_exp_text, boolean _additional_percent_text, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try{
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/rank"+_rank_skin+"_blank.png"));
			BufferedImage experienceBar;
			if(_experience != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/ExperienceBar/exp"+_bar_color+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experience, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/Rank/level_"+_icon_skin+"_"+_level+".png"));
			
			final URL url = new URL(_avatar);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = _name;
			int characterCounter = name.length();
			int levelT = _experience;
			
			int[] ran = GuildIni.getWholeProfile(e.getGuild().getIdLong());
			final var nameLengthLimit = ran[0];
			final var generalTextFontSize = ran[1];
			final var nameTextFontSize = ran[2];
			
			if(characterCounter > nameLengthLimit && nameLengthLimit != 0)
				name = name.substring(0, nameLengthLimit);
			
			int rankW = rank.getWidth();
			int rankH = rank.getHeight();
			BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(rank, 0, 0, null);
			g.drawImage(experienceBar, 38, 64, null);
			level = blurImage(level);
			g.drawImage(level, _rankx, _ranky, _rank_width, _rank_height, null);
			g.drawImage(avatarPicture, 19, 19, 40, 40, null);
			Color color = new Color(_color_r, _color_g, _color_b);
			g.setColor(color);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, generalTextFontSize));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(_additional_exp_text == true) {
				g.drawString("Exp:", 14, 73);
			}
			if(_additional_percent_text == true) {
				g.drawString(levelT+"%", 246, 73);
			}
			var rankString = insertDots(_rank);
			g.drawString("Rank:  #"+rankString, 118, 57);
			g.setFont(new Font("Nexa Bold", Font.BOLD, nameTextFontSize));
			g.drawString(name, 117, 38);
			
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file2 = new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file2, "rank.png", null).complete();
			file2.delete();
		} catch (IOException e1) {
			logger.error("Rank couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getProfile(MessageReceivedEvent e, String _name, String _avatar, int _experiencePercentage, int _level, float _currentExperience, float _rankUpExperience, long _experience, long _currency, int _rank, int _profile_skin, int _icon_skin, int _bar_color, boolean _additional_exp_text, boolean _additional_percent_text, int _color_r, int _color_g, int _color_b, int _rankx, int _ranky, int _rank_width, int _rank_height){		
		try{
			BufferedImage profile = ImageIO.read(new File("./files/RankingSystem/profile"+_profile_skin+"_blank.png"));
			BufferedImage experienceBar;
			if(_experiencePercentage != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/ExperienceBar/exp"+_bar_color+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experiencePercentage, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/Rank/level_"+_icon_skin+"_"+_level+".png"));
			
			final URL url = new URL(_avatar);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = _name;
			int characterCounter = name.length();
			int levelT = _experiencePercentage;
			int currentExperience = (int) _currentExperience;
			int rankUpExperience = (int) _rankUpExperience;
			long experience = _experience;
			long currency = _currency;
			int rank = _rank;
			
			int[] prof = GuildIni.getWholeProfile(e.getGuild().getIdLong());
			final var nameLengthLimit = prof[0];
			final var generalTextFontSize = prof[1];
			final var nameTextFontSize = prof[2];
			final var descriptionMode = prof[3];
			
			if(characterCounter > nameLengthLimit && nameLengthLimit != 0)
				name = name.substring(0, nameLengthLimit);
			
			int profileW = profile.getWidth();
			int profileH = profile.getHeight();
			BufferedImage overlay = new BufferedImage(profileW, profileH, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = overlay.createGraphics();
			g.drawImage(profile, 0, 0, null);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(experienceBar, 42, 100, null);
			level = blurImage(level);
			g.drawImage(level, _rankx, _ranky, _rank_width, _rank_height, null);
			g.drawImage(avatarPicture, 20, 20, 55, 55, null);
			Color color = new Color(_color_r, _color_g, _color_b);
			g.setColor(color);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, generalTextFontSize));
			if(_additional_exp_text == true) {
				g.drawString("Exp:", 15, 109);
			}
			if(_additional_percent_text == true) {
				g.drawString(levelT+"%", 243, 109);
			}
			
			if(descriptionMode == 0) {
				g.drawString(""+_level, 136, 176);
				var experienceString = insertDots(experience);
				g.drawString(""+experienceString, 136, 153);
				var currentExperienceString = insertDots(currentExperience);
				var rankUpExperienceString = insertDots(rankUpExperience);
				g.drawString(currentExperienceString+"/"+rankUpExperienceString, 142, 123);
				var currencyString = insertDots(currency);
				g.drawString(currencyString, 263, 153);
				var rankString = insertDots(rank);
				g.drawString(rankString, 263, 176);
			}
			else if(descriptionMode == 1) {
				g.drawString(""+_level, getCenteredString(""+_level, 136, g), 176);
				var experienceString = insertDots(experience);
				g.drawString(""+experienceString, getCenteredString(""+experienceString, 136, g), 153);
				var currencyString = insertDots(currency);
				g.drawString(currencyString, getCenteredString(currencyString, 263, g), 153);
				var rankString = insertDots(rank);
				g.drawString(rankString, getCenteredString(rankString, 263, g), 176);
			}
			else if(descriptionMode == 2) {
				g.drawString(""+_level, getRightString(""+_level, 136, g), 176);
				var experienceString = insertDots(experience);
				g.drawString(""+experienceString, getRightString(""+experienceString, 136, g), 153);
				var currencyString = insertDots(currency);
				g.drawString(currencyString, getRightString(currencyString, 263, g), 153);
				var rankString = insertDots(rank);
				g.drawString(rankString, getRightString(rankString, 263, g), 176);
			}
			var currentExperienceString = insertDots(currentExperience);
			var rankUpExperienceString = insertDots(rankUpExperience);
			g.drawString(currentExperienceString+"/"+rankUpExperienceString, getRightString(""+currentExperienceString, 142, g), 123);

			g.setFont(new Font("Nexa Bold", Font.BOLD, nameTextFontSize));
			g.drawString(name, 126, 56);
			
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file3 = new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getTextChannel().sendFile(file3, "profile.png", null).complete();
			file3.delete();
		} catch (IOException e1) {
			logger.error("Profile couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	private static int getRightString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = w - fm.stringWidth(s);
	    return x;
	}
	
	private static int getCenteredString(String s, int w, Graphics2D g) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = (w/2) - (fm.stringWidth(s)/2);
	    return x;
	}
	
	private static BufferedImage blurImage(BufferedImage image) {
		float ninth = 1.0f/9.0f;
		float[] blurKernel = {
				ninth, ninth, ninth,
				ninth, ninth, ninth,
				ninth, ninth, ninth
		};

		Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
		map.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		map.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		map.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		RenderingHints hints = new RenderingHints(map);
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
		return op.filter(image, null);
	}
	
	private static String insertDots(long value) {
		var number = ""+value;
		var stringLength = number.length();
		var line = 0;
		var newString = "";
		while(stringLength != 0) {
			var spot = stringLength % 3;
			if(spot != 0) {
				stringLength -= spot;
				newString += number.substring(line, spot)+(stringLength != 0 ? "." : "");
				line += spot;
			}
			else {
				stringLength -= 3;
				newString += number.substring(line, line+3)+(stringLength != 0 ? "." : "");
				line += 3;
			}
		}
		return newString;
	}
}
