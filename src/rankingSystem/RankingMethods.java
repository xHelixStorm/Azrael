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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constructors.Rank;
import fileManagement.IniFileReader;
import gif.GifDecoder;
import gif.GifSequenceWriter;
import gif.GifDecoder.GifImage;
import gif.GifOptimizer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RankingMethods extends ListenerAdapter {
	//Class for drawing level ups, ranks and profiles basing on the user settings
	private final static Logger logger = LoggerFactory.getLogger(RankingMethods.class);
	
	public static void getRankUp(GuildMessageReceivedEvent e , int theme_id, Rank user_details) {		
		try {
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+"."+user_details.getFileTypeIcon()));
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int level = user_details.getLevel();
			int level1;
			int level2;
			String levelS1 = null;
			String levelS2 = null;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
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
			
			if(characterCounter > user_details.getNameLengthLimit_Level() && user_details.getNameLengthLimit_Level() != 0)
				name = name.substring(0, user_details.getNameLengthLimit_Level());
			
			if(!user_details.getFileTypeLevel().equals("gif")) {
				BufferedImage rankUp = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getLevelDescription()+"."+user_details.getFileTypeLevel()));
				int rankUpW = rankUp.getWidth();
				int rankUpH = rankUp.getHeight();
				BufferedImage overlay = new BufferedImage(rankUpW, rankUpH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(rankUp, 0, 0, null);
				if(user_details.getRankXLevel() > 0 || user_details.getRankYLevel() > 0) {
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g.drawImage(rank, user_details.getRankXLevel(), user_details.getRankYLevel(), user_details.getRankWidthLevel(), user_details.getRankHeightLevel(), null);
				}
				Color color = new Color(user_details.getColorRLevel(), user_details.getColorGLevel(), user_details.getColorBLevel());
				g.setColor(color);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if(user_details.getLevelXLevel() > 0 || user_details.getLevelYLevel() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getTextFontSize_Level()));
					g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, user_details.getLevelXLevel(), g), user_details.getLevelYLevel());
				}
				if(user_details.getNameXLevel() > 0 || user_details.getNameYLevel() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Level()));
					g.drawString(name, user_details.getNameXLevel(), user_details.getNameYLevel());
				}
				ImageIO.write(overlay, user_details.getFileTypeLevel(), new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeLevel()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getLevelDescription()+"."+user_details.getFileTypeLevel()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeLevel())), gif.getFrame(0).getType(), 20, true);
				int rankUpW = gif.getWidth();
				int rankUpH = gif.getHeight();
				for(var i = 0; i < frames; i++) {
					BufferedImage rankUp = gif.getFrame(i);
					BufferedImage overlay = new BufferedImage(rankUpW, rankUpH, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = overlay.createGraphics();
					g.drawImage(rankUp, 0, 0, null);
					if(user_details.getRankXLevel() > 0 || user_details.getRankYLevel() > 0) {
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						g.drawImage(rank, user_details.getRankXLevel(), user_details.getRankYLevel(), user_details.getRankWidthLevel(), user_details.getRankHeightLevel(), null);
					}
					Color color = new Color(user_details.getColorRLevel(), user_details.getColorGLevel(), user_details.getColorBLevel());
					g.setColor(color);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if(user_details.getLevelXLevel() > 0 || user_details.getLevelYLevel() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getTextFontSize_Level()));
						g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, user_details.getLevelXLevel(), g), user_details.getLevelYLevel());
					}
					if(user_details.getNameXLevel() > 0 || user_details.getNameYLevel() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Level()));
						g.drawString(name, user_details.getNameXLevel(), user_details.getNameYLevel());
					}
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeLevel());
			if(file1.getTotalSpace() > 8000000) {
				e.getChannel().sendMessage("Request is being processed. Please wait...").queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("level", "level_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"level_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile());
							e.getChannel().sendFile(file2, "level_up."+user_details.getFileTypeLevel()).complete();
							file2.delete();
						}
					} catch(IOException | DataFormatException e1) {
						logger.error("Compression error for file {}", user_details.getLevelDescription()+user_details.getFileTypeLevel(), e1);
						file1.delete();
						message.delete().queue();
						e.getChannel().sendMessage(new EmbedBuilder().setTitle("Error!").setDescription("File couldn't be compressed! Command aborted!").build()).queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "level_up."+user_details.getFileTypeLevel()).complete();
				file1.delete();
			}
		} catch (IOException e1) {
			logger.error("RankUp couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getRank(GuildMessageReceivedEvent e, String _name, String _avatar, int _experience, int _rank, int theme_id, Rank user_details) {		
		try{
			BufferedImage experienceBar;
			if(_experience != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+user_details.getBarColorRank()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experience, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+"."+user_details.getFileTypeIcon()));
			
			final URL url = new URL(_avatar);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = _name;
			int characterCounter = name.length();
			int levelT = _experience;
			
			if(characterCounter > user_details.getNameLengthLimit_Rank() && user_details.getNameLengthLimit_Rank() != 0)
				name = name.substring(0, user_details.getNameLengthLimit_Rank());
			
			if(!user_details.getFileTypeRank().equals("gif")) {
				BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getRankDescription()+"."+user_details.getFileTypeRank()));
				int rankW = rank.getWidth();
				int rankH = rank.getHeight();
				BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(rank, 0, 0, null);
				g.drawImage(experienceBar, user_details.getBarXRank(), user_details.getBarYRank(), null);
				if(user_details.getRankXRank() > 0 || user_details.getRankYRank() > 0)
					g.drawImage(blurImage(level), user_details.getRankXRank(), user_details.getRankYRank(), user_details.getRankWidthRank(), user_details.getRankHeightRank(), null);
				if(user_details.getAvatarXRank() > 0 || user_details.getAvatarYRank() > 0)
					g.drawImage(avatarPicture, user_details.getAvatarXRank(), user_details.getAvatarYRank(), user_details.getAvatarWidthRank(), user_details.getAvatarHeightRank(), null);
				Color color = new Color(user_details.getColorRRank(), user_details.getColorGRank(), user_details.getColorBRank());
				g.setColor(color);
				g.setFont(new Font("Nexa Bold", Font.PLAIN, user_details.getTextFontSize_Rank()));
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if(user_details.getExpTextXRank() > 0 || user_details.getExpTextYProfile() > 0)
					g.drawString("Exp:", user_details.getExpTextXRank(), user_details.getExpTextYRank());
				if(user_details.getPercentTextXRank() > 0 || user_details.getPercentTextYRank() > 0)
					g.drawString(levelT+"%", user_details.getPercentTextXRank(), user_details.getPercentTextYRank());
				var rankString = insertDots(_rank);
				if(user_details.getPlacementXRank() > 0 || user_details.getPlacementYRank() > 0)
					g.drawString("Rank:  #"+rankString, user_details.getPlacementXRank(), user_details.getPlacementYRank());
				if(user_details.getNameXRank() > 0 || user_details.getNameYRank() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Rank()));
					g.drawString(name, user_details.getNameXRank(), user_details.getNameYRank());
				}
				
				ImageIO.write(overlay, user_details.getFileTypeRank(), new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeRank()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getRankDescription()+"."+user_details.getFileTypeRank()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeRank())), gif.getFrame(0).getType(), 20, true);
				int rankW = gif.getWidth();
				int rankH = gif.getHeight();
				for(var i = 0; i < frames; i++) {
					BufferedImage rank = gif.getFrame(i);
					BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = overlay.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(rank, 0, 0, null);
					g.drawImage(experienceBar, user_details.getBarXRank(), user_details.getBarYRank(), null);
					if(user_details.getRankXRank() > 0 || user_details.getRankYRank() > 0)
						g.drawImage(blurImage(level), user_details.getRankXRank(), user_details.getRankYRank(), user_details.getRankWidthRank(), user_details.getRankHeightRank(), null);
					if(user_details.getAvatarXRank() > 0 || user_details.getAvatarYRank() > 0)
						g.drawImage(avatarPicture, user_details.getAvatarXRank(), user_details.getAvatarYRank(), user_details.getAvatarWidthRank(), user_details.getAvatarHeightRank(), null);
					Color color = new Color(user_details.getColorRRank(), user_details.getColorGRank(), user_details.getColorBRank());
					g.setColor(color);
					g.setFont(new Font("Nexa Bold", Font.PLAIN, user_details.getTextFontSize_Rank()));
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if(user_details.getExpTextXRank() > 0 || user_details.getExpTextYProfile() > 0)
						g.drawString("Exp:", user_details.getExpTextXRank(), user_details.getExpTextYRank());
					if(user_details.getPercentTextXRank() > 0 || user_details.getPercentTextYRank() > 0)
						g.drawString(levelT+"%", user_details.getPercentTextXRank(), user_details.getPercentTextYRank());
					var rankString = insertDots(_rank);
					if(user_details.getPlacementXRank() > 0 || user_details.getPlacementYRank() > 0)
						g.drawString("Rank:  #"+rankString, user_details.getPlacementXRank(), user_details.getPlacementYRank());
					if(user_details.getNameXRank() > 0 || user_details.getNameYRank() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Rank()));
						g.drawString(name, user_details.getNameXRank(), user_details.getNameYRank());
					}
					
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeRank());
			if(file1.getTotalSpace() > 8000000) {
				e.getChannel().sendMessage("Request is being processed. Please wait...").queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("rank", "rank_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"rank_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile());
							e.getChannel().sendFile(file2, "rank."+user_details.getFileTypeLevel()).complete();
							file2.delete();
						}
					} catch(IOException | DataFormatException e1) {
						logger.error("Compression error for file {}", user_details.getRankDescription()+user_details.getFileTypeRank(), e1);
						file1.delete();
						message.delete().queue();
						e.getChannel().sendMessage(new EmbedBuilder().setTitle("Error!").setDescription("File couldn't be compressed! Command aborted!").build()).queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "rank."+user_details.getFileTypeRank()).complete();
				file1.delete();
			}
		} catch (IOException e1) {
			logger.error("Rank couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getProfile(GuildMessageReceivedEvent e, String _name, String _avatar, int _experiencePercentage, int _rank, int currentExperience, int rankUpExperience, int theme_id, Rank user_details) {		
		try{
			BufferedImage experienceBar;
			if(_experiencePercentage != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+user_details.getBarColorProfile()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, (experienceBar.getWidth()*_experiencePercentage)/100, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+user_details.getLevel()+"."+user_details.getFileTypeIcon()));
			
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
			
			if(characterCounter > user_details.getNameLengthLimit_Profile() && user_details.getNameLengthLimit_Profile() != 0)
				name = name.substring(0, user_details.getNameLengthLimit_Profile());
			
			if(!user_details.getFileTypeProfile().equals("gif")) {
				BufferedImage profile = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getProfileDescription()+"."+user_details.getFileTypeProfile()));
				int profileW = profile.getWidth();
				int profileH = profile.getHeight();
				BufferedImage overlay = new BufferedImage(profileW, profileH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(profile, 0, 0, null);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(experienceBar, user_details.getBarXProfile(), user_details.getBarYProfile(), null);
				if(user_details.getRankXProfile() > 0 || user_details.getRankYProfile() > 0)
					g.drawImage(blurImage(level), user_details.getRankXProfile(), user_details.getRankYProfile(), user_details.getRankWidthProfile(), user_details.getRankHeightProfile(), null);
				if(user_details.getAvatarXProfile() > 0 || user_details.getAvatarYProfile() > 0)
					g.drawImage(avatarPicture, user_details.getAvatarXProfile(), user_details.getAvatarYProfile(), user_details.getAvatarWidthProfile(), user_details.getAvatarHeightProfile(), null);
				Color color = new Color(user_details.getColorRProfile(), user_details.getColorGProfile(), user_details.getColorBProfile());
				g.setColor(color);
				g.setFont(new Font("Nexa Bold", Font.PLAIN, user_details.getTextFontSize_Profile()));
				if(user_details.getExpTextXProfile() > 0 || user_details.getExpTextYProfile() > 0) {
					g.drawString("Exp:", user_details.getExpTextXProfile(), user_details.getExpTextYProfile());
				}
				if(user_details.getPercentTextXProfile() > 0 || user_details.getPercentTextYProfile() > 0) {
					g.drawString(levelT+"%", user_details.getPercentTextXProfile(), user_details.getPercentTextYProfile());
				}
				
				if(user_details.getDescriptionMode_Profile() == 0) {
					if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
						g.drawString(""+user_details.getLevel(), user_details.getLevelXProfile(), user_details.getLevelYProfile());
					if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0)
						g.drawString(insertDots(user_details.getExperience()), user_details.getExperienceXProfile(), user_details.getExperienceYProfile());
					if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0)
						g.drawString(insertDots(user_details.getCurrency()), user_details.getCurrencyXProfile(), user_details.getCurrencyYProfile());
					if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0)
						g.drawString(insertDots(rank), user_details.getPlacementXProfile(), user_details.getPlacementYProfile());
				}
				else if(user_details.getDescriptionMode_Profile() == 1) {
					if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
						g.drawString(""+user_details.getLevel(), getCenteredString(""+user_details.getLevel(), user_details.getLevelXProfile(), g), user_details.getLevelYProfile());
					if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0) {
						var experienceString = insertDots(user_details.getExperience());
						g.drawString(experienceString, getCenteredString(experienceString, user_details.getExperienceXProfile(), g), user_details.getExperienceYProfile());
					}
					if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0) {
						var currencyString = insertDots(user_details.getCurrency());
						g.drawString(currencyString, getCenteredString(currencyString, user_details.getCurrencyXProfile(), g), user_details.getCurrencyYProfile());
					}
					if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0) {
						var rankString = insertDots(rank);
						g.drawString(rankString, getCenteredString(rankString, user_details.getPlacementXProfile(), g), user_details.getPlacementYProfile());
					}
				}
				else if(user_details.getDescriptionMode_Profile() == 2) {
					if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
						g.drawString(""+user_details.getLevel(), getRightString(""+user_details.getLevel(), user_details.getLevelXProfile(), g), user_details.getLevelYProfile());
					if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0) {
						var experienceString = insertDots(user_details.getExperience());
						g.drawString(""+experienceString, getRightString(""+experienceString, user_details.getExperienceXProfile(), g), user_details.getExperienceYProfile());
					}
					if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0) {
						var currencyString = insertDots(user_details.getCurrency());
						g.drawString(currencyString, getRightString(currencyString, user_details.getCurrencyXProfile(), g), user_details.getCurrencyYProfile());
					}
					if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0) {
						var rankString = insertDots(rank);
						g.drawString(rankString, getRightString(rankString, user_details.getPlacementXProfile(), g), user_details.getPlacementYProfile());
					}
				}
				if(user_details.getExpReachXProfile() > 0 || user_details.getExpReachYProfile() > 0) {
					var currentExperienceString = insertDots(currentExperience);
					g.drawString(currentExperienceString+"/"+insertDots(rankUpExperience), getRightString(""+currentExperienceString, user_details.getExpReachXProfile(), g), user_details.getExpReachYProfile());
				}
				if(user_details.getNameXProfile() > 0 || user_details.getNameYProfile() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Profile()));
					g.drawString(name, user_details.getNameXProfile(), user_details.getNameYProfile());
				}
				
				ImageIO.write(overlay, user_details.getFileTypeProfile(), new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+user_details.getProfileDescription()+"."+user_details.getFileTypeProfile()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile())), gif.getFrame(0).getType(), 20, true);
				int profileW = gif.getWidth();
				int profileH = gif.getHeight();
				for(var i = 0; i < frames; i++) {
					BufferedImage profile = gif.getFrame(i);
					BufferedImage overlay = new BufferedImage(profileW, profileH, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = overlay.createGraphics();
					g.drawImage(profile, 0, 0, null);
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.drawImage(experienceBar, user_details.getBarXProfile(), user_details.getBarYProfile(), null);
					if(user_details.getRankXProfile() > 0 || user_details.getRankYProfile() > 0)
						g.drawImage(blurImage(level), user_details.getRankXProfile(), user_details.getRankYProfile(), user_details.getRankWidthProfile(), user_details.getRankHeightProfile(), null);
					if(user_details.getAvatarXProfile() > 0 || user_details.getAvatarYProfile() > 0)
						g.drawImage(avatarPicture, user_details.getAvatarXProfile(), user_details.getAvatarYProfile(), user_details.getAvatarWidthProfile(), user_details.getAvatarHeightProfile(), null);
					Color color = new Color(user_details.getColorRProfile(), user_details.getColorGProfile(), user_details.getColorBProfile());
					g.setColor(color);
					g.setFont(new Font("Nexa Bold", Font.PLAIN, user_details.getTextFontSize_Profile()));
					if(user_details.getExpTextXProfile() > 0 || user_details.getExpTextYProfile() > 0) {
						g.drawString("Exp:", user_details.getExpTextXProfile(), user_details.getExpTextYProfile());
					}
					if(user_details.getPercentTextXProfile() > 0 || user_details.getPercentTextYProfile() > 0) {
						g.drawString(levelT+"%", user_details.getPercentTextXProfile(), user_details.getPercentTextYProfile());
					}
					
					if(user_details.getDescriptionMode_Profile() == 0) {
						if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
							g.drawString(""+user_details.getLevel(), user_details.getLevelXProfile(), user_details.getLevelYProfile());
						if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0)
							g.drawString(insertDots(user_details.getExperience()), user_details.getExperienceXProfile(), user_details.getExperienceYProfile());
						if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0)
							g.drawString(insertDots(user_details.getCurrency()), user_details.getCurrencyXProfile(), user_details.getCurrencyYProfile());
						if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0)
							g.drawString(insertDots(rank), user_details.getPlacementXProfile(), user_details.getPlacementYProfile());
					}
					else if(user_details.getDescriptionMode_Profile() == 1) {
						if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
							g.drawString(""+user_details.getLevel(), getCenteredString(""+user_details.getLevel(), user_details.getLevelXProfile(), g), user_details.getLevelYProfile());
						if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0) {
							var experienceString = insertDots(user_details.getExperience());
							g.drawString(experienceString, getCenteredString(experienceString, user_details.getExperienceXProfile(), g), user_details.getExperienceYProfile());
						}
						if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0) {
							var currencyString = insertDots(user_details.getCurrency());
							g.drawString(currencyString, getCenteredString(currencyString, user_details.getCurrencyXProfile(), g), user_details.getCurrencyYProfile());
						}
						if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0) {
							var rankString = insertDots(rank);
							g.drawString(rankString, getCenteredString(rankString, user_details.getPlacementXProfile(), g), user_details.getPlacementYProfile());
						}
					}
					else if(user_details.getDescriptionMode_Profile() == 2) {
						if(user_details.getLevelXProfile() > 0 || user_details.getLevelYProfile() > 0)
							g.drawString(""+user_details.getLevel(), getRightString(""+user_details.getLevel(), user_details.getLevelXProfile(), g), user_details.getLevelYProfile());
						if(user_details.getExperienceXProfile() > 0 || user_details.getExperienceYProfile() > 0) {
							var experienceString = insertDots(user_details.getExperience());
							g.drawString(""+experienceString, getRightString(""+experienceString, user_details.getExperienceXProfile(), g), user_details.getExperienceYProfile());
						}
						if(user_details.getCurrencyXProfile() > 0 || user_details.getCurrencyYProfile() > 0) {
							var currencyString = insertDots(user_details.getCurrency());
							g.drawString(currencyString, getRightString(currencyString, user_details.getCurrencyXProfile(), g), user_details.getCurrencyYProfile());
						}
						if(user_details.getPlacementXProfile() > 0 || user_details.getPlacementYProfile() > 0) {
							var rankString = insertDots(rank);
							g.drawString(rankString, getRightString(rankString, user_details.getPlacementXProfile(), g), user_details.getPlacementYProfile());
						}
					}
					if(user_details.getExpReachXProfile() > 0 || user_details.getExpReachYProfile() > 0) {
						var currentExperienceString = insertDots(currentExperience);
						g.drawString(currentExperienceString+"/"+insertDots(rankUpExperience), getRightString(""+currentExperienceString, user_details.getExpReachXProfile(), g), user_details.getExpReachYProfile());
					}
					if(user_details.getNameXProfile() > 0 || user_details.getNameYProfile() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, user_details.getNameFontSize_Profile()));
						g.drawString(name, user_details.getNameXProfile(), user_details.getNameYProfile());
					}
					
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile());
			if(file1.getTotalSpace() > 8000000) {
				e.getChannel().sendMessage("Request is being processed. Please wait...").queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("profile", "profile_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"profile_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+user_details.getFileTypeProfile());
							e.getChannel().sendFile(file2, "profile."+user_details.getFileTypeLevel()).complete();
							file2.delete();
						}
					} catch(IOException | DataFormatException e1) {
						logger.error("Compression error for file {}", user_details.getProfileDescription()+user_details.getFileTypeProfile(), e1);
						file1.delete();
						message.delete().queue();
						e.getChannel().sendMessage(new EmbedBuilder().setTitle("Error!").setDescription("File couldn't be compressed! Command aborted!").build()).queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "profile."+user_details.getFileTypeProfile()).complete();
				file1.delete();
			}
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
