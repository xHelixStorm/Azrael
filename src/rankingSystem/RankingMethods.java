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

import constructors.Rank;
import fileManagement.GuildIni;
import fileManagement.IniFileReader;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RankingMethods extends ListenerAdapter{
	private final static Logger logger = LoggerFactory.getLogger(RankingMethods.class);
	
	public static void getRankUp(GuildMessageReceivedEvent e , int theme_id, Rank user_details) {		
		try {
			BufferedImage rankUp = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/levelup"+user_details.getRankingLevel()+"_blank.png"));
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+".png"));
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int level = user_details.getLevel();
			int level1;
			int level2;
			String levelS1 = null;
			String levelS2 = null;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			int[] lev = GuildIni.getWholeLevel(e.getGuild().getIdLong());
			final var nameLengthLimit = lev[0];
			final var generalTextFontSize = lev[1];
			final var nameTextFontSize = lev[2];
			
			if(level > 9) {
				level1 = level / 10;
				level2 = level % 10;
				sb.append(level1);
				levelS1 = sb.toString();
				sb2.append(level2);
				levelS2 = sb2.toString();
			}
			else {
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
			g.drawImage(rank, user_details.getRankXLevel(), user_details.getRankYLevel(), user_details.getRankWidthLevel(), user_details.getRankHeightLevel(), null);
			Color color = new Color(user_details.getColorRLevel(), user_details.getColorGLevel(), user_details.getColorBLevel());
			g.setColor(color);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(new Font("Nexa Bold", Font.BOLD, generalTextFontSize));
			g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, user_details.getLevelXLevel(), g), user_details.getLevelYLevel());
			g.setFont(new Font("Nexa Bold", Font.BOLD, nameTextFontSize));
			g.drawString(name, user_details.getNameXLevel(), user_details.getNameYLevel());
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"lvup_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file1 = new File(IniFileReader.getTempDirectory()+"lvup_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getChannel().sendFile(file1, "level_up.png").complete();
			file1.delete();
		} catch (IOException e1) {
			logger.error("RankUp couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getRank(GuildMessageReceivedEvent e, String _name, String _avatar, int _experience, int _rank, int theme_id, Rank user_details) {		
		try{
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/rank"+user_details.getRankingRank()+"_blank.png"));
			BufferedImage experienceBar;
			if(_experience != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+user_details.getBarColorRank()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experience, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+".png"));
			
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
			g.drawImage(experienceBar, user_details.getBarXRank(), user_details.getBarYRank(), null);
			level = blurImage(level);
			g.drawImage(level, user_details.getRankXRank(), user_details.getRankYRank(), user_details.getRankWidthRank(), user_details.getRankHeightRank(), null);
			g.drawImage(avatarPicture, user_details.getAvatarXRank(), user_details.getAvatarYRank(), user_details.getAvatarWidthRank(), user_details.getAvatarHeightRank(), null);
			Color color = new Color(user_details.getColorRRank(), user_details.getColorGRank(), user_details.getColorBRank());
			g.setColor(color);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, generalTextFontSize));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(user_details.getAdditionalExpTextRank() == true) {
				g.drawString("Exp:", 14, 73);
			}
			if(user_details.getAdditionalPercentTextRank() == true) {
				g.drawString(levelT+"%", 246, 73);
			}
			var rankString = insertDots(_rank);
			g.drawString("Rank:  #"+rankString, 118, 57);
			g.setFont(new Font("Nexa Bold", Font.BOLD, nameTextFontSize));
			g.drawString(name, user_details.getNameXRank(), user_details.getNameYRank());
			
			ImageIO.write(overlay, "png", new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png"));
			g.dispose();
			
			File file2 = new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+".png");
			e.getChannel().sendFile(file2, "rank.png").complete();
			file2.delete();
		} catch (IOException e1) {
			logger.error("Rank couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getProfile(GuildMessageReceivedEvent e, String _name, String _avatar, int _experiencePercentage, int _rank, int currentExperience, int rankUpExperience, int theme_id, Rank user_details) {		
		try{
			BufferedImage profile = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/profile"+user_details.getRankingProfile()+"_blank.png"));
			BufferedImage experienceBar;
			if(_experiencePercentage != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+user_details.getBarColorProfile()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experiencePercentage, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+".png"));
			
			final URL url = new URL(_avatar);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = _name;
			int characterCounter = name.length();
			int levelT = _experiencePercentage;
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
			g.drawImage(experienceBar, user_details.getBarXProfile(), user_details.getBarYProfile(), null);
			level = blurImage(level);
			g.drawImage(level, user_details.getRankXProfile(), user_details.getRankYProfile(), user_details.getRankWidthProfile(), user_details.getRankHeightProfile(), null);
			g.drawImage(avatarPicture, user_details.getAvatarXProfile(), user_details.getAvatarYProfile(), user_details.getAvatarWidthProfile(), user_details.getAvatarHeightProfile(), null);
			Color color = new Color(user_details.getColorRProfile(), user_details.getColorGProfile(), user_details.getColorBProfile());
			g.setColor(color);
			g.setFont(new Font("Nexa Bold", Font.PLAIN, generalTextFontSize));
			if(user_details.getAdditionalExpTextProfile() == true) {
				g.drawString("Exp:", 15, 109);
			}
			if(user_details.getAdditionalPercentTextProfile() == true) {
				g.drawString(levelT+"%", 243, 109);
			}
			
			if(descriptionMode == 0) {
				g.drawString(""+user_details.getLevel(), 136, 176);
				var experienceString = insertDots(user_details.getExperience());
				g.drawString(""+experienceString, 136, 153);
				var currentExperienceString = insertDots(user_details.getCurrentExperience());
				var rankUpExperienceString = insertDots(user_details.getRankUpExperience());
				g.drawString(currentExperienceString+"/"+rankUpExperienceString, 142, 123);
				var currencyString = insertDots(user_details.getCurrency());
				g.drawString(currencyString, 263, 153);
				var rankString = insertDots(rank);
				g.drawString(rankString, 263, 176);
			}
			else if(descriptionMode == 1) {
				g.drawString(""+user_details.getLevel(), getCenteredString(""+user_details.getLevel(), 136, g), 176);
				var experienceString = insertDots(user_details.getExperience());
				g.drawString(""+experienceString, getCenteredString(""+experienceString, 136, g), 153);
				var currencyString = insertDots(user_details.getCurrency());
				g.drawString(currencyString, getCenteredString(currencyString, 263, g), 153);
				var rankString = insertDots(rank);
				g.drawString(rankString, getCenteredString(rankString, 263, g), 176);
			}
			else if(descriptionMode == 2) {
				g.drawString(""+user_details.getLevel(), getRightString(""+user_details.getLevel(), 136, g), 176);
				var experienceString = insertDots(user_details.getExperience());
				g.drawString(""+experienceString, getRightString(""+experienceString, 136, g), 153);
				var currencyString = insertDots(user_details.getCurrency());
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
			e.getChannel().sendFile(file3, "profile.png").complete();
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
