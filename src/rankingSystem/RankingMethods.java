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

import constructors.Ranking;
import enums.Translation;
import fileManagement.IniFileReader;
import gif.GifDecoder;
import gif.GifSequenceWriter;
import gif.GifDecoder.GifImage;
import gif.GifOptimizer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sql.RankingSystem;
import util.STATIC;

public class RankingMethods extends ListenerAdapter {
	//Class for drawing level ups, ranks and profiles basing on the user settings
	private final static Logger logger = LoggerFactory.getLogger(RankingMethods.class);
	
	public static void getRankUp(GuildMessageReceivedEvent e , int theme_id, Ranking user_details, int rank_icon) {
		final var skinIcon = RankingSystem.SQLgetRankingIcons(user_details.getRankingIcon(), theme_id);
		final var skin = RankingSystem.SQLgetRankingLevel(user_details.getRankingLevel(), theme_id);
		if(skinIcon == null || skin == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_ERR)).build()).queue();
			logger.error("RankUp couldn't be drawn for guild {}. Skin not found!", e.getGuild().getIdLong());
			return;
		}
		
		try {
			BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+rank_icon+"."+skinIcon.getFileType()));
			String name = e.getMember().getEffectiveName();
			int characterCounter = name.length();
			int level = (user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel());
			int level1;
			int level2;
			String levelS1 = null;
			String levelS2 = null;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			if(level > 9 && level < 100) {
				level1 = level / 10;
				level2 = level % 10;
				sb.append(level1);
				levelS1 = sb.toString();
				sb2.append(level2);
				levelS2 = sb2.toString();
			}
			else if(level > 99) {
				levelS1 = "";
				levelS2 = ""+level;
			}
			else {
				levelS1 = "0";
				sb.append(level);
				levelS2 = sb.toString();
			}
			
			if(characterCounter > skin.getNameLengthLimit() && skin.getNameLengthLimit() != 0)
				name = name.substring(0, skin.getNameLengthLimit());
			
			if(!skin.getFileType().equals("gif")) {
				BufferedImage rankUp = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				int rankUpW = rankUp.getWidth();
				int rankUpH = rankUp.getHeight();
				BufferedImage overlay = new BufferedImage(rankUpW, rankUpH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(rankUp, 0, 0, null);
				if(skin.getIconX() > 0 || skin.getIconY() > 0) {
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g.drawImage(rank, skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
				}
				Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
				g.setColor(color);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if(skin.getLevelX() > 0 || skin.getLevelY() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getTextFontSize()));
					g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, skin.getLevelX(), g), skin.getLevelY());
				}
				if(skin.getNameX() > 0 || skin.getNameY() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
					g.drawString(name, skin.getNameX(), skin.getNameY());
				}
				ImageIO.write(overlay, skin.getFileType(), new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType())), gif.getFrame(0).getType(), 20, true);
				int rankUpW = gif.getWidth();
				int rankUpH = gif.getHeight();
				for(var i = 0; i < frames; i++) {
					BufferedImage rankUp = gif.getFrame(i);
					BufferedImage overlay = new BufferedImage(rankUpW, rankUpH, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = overlay.createGraphics();
					g.drawImage(rankUp, 0, 0, null);
					if(skin.getIconX() > 0 || skin.getIconY() > 0) {
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						g.drawImage(rank, skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
					}
					Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
					g.setColor(color);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if(skin.getLevelX() > 0 || skin.getLevelY() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getTextFontSize()));
						g.drawString(levelS1+""+levelS2, getCenteredString(levelS1+""+levelS2, skin.getLevelX(), g), skin.getLevelY());
					}
					if(skin.getNameX() > 0 || skin.getNameY() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
						g.drawString(name, skin.getNameX(), skin.getNameY());
					}
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"level_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
			if(file1.length() > 8000000 && e.getGuild().getBoostCount() < 15) {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESSION)).queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("level", "level_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"level_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
							e.getChannel().sendFile(file2, "level_up."+skin.getFileType()).queue(complete -> {
								file2.delete();
							}, error -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_SEND_ERR)).build()).queue();
								file2.delete();
							});;
						}
					} catch(IOException | DataFormatException e1) {
						logger.error("Compression error for file {}", skin.getSkinDescription()+skin.getFileType(), e1);
						file1.delete();
						message.delete().queue();
						e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESS_ERR)).build()).queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "level_up."+skin.getFileType()).complete();
				file1.delete();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.LEVEL_ERR)).build()).queue();
			logger.error("RankUp couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getRank(GuildMessageReceivedEvent e, String _name, String _avatar, int _experience, int _rank, int theme_id, Ranking user_details) {
		final var skinIcon = RankingSystem.SQLgetRankingIcons(user_details.getRankingIcon(), theme_id);
		final var skin = RankingSystem.SQLgetRankingRank(user_details.getRankingRank(), theme_id);
		if(skinIcon == null || skin == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANK_ERR)).build()).queue();
			logger.error("Rank couldn't be drawn for guild {}. Skin not found!", e.getGuild().getIdLong());
			return;
		}
		
		try{
			BufferedImage experienceBar;
			if(_experience != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+skin.getBarColor()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, 2*_experience, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			int rankIcon = RankingSystem.SQLgetLevels(e.getGuild().getIdLong(), theme_id).parallelStream().filter(f -> f.getLevel() == user_details.getLevel()).findAny().orElse(null).getRankIcon();
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+rankIcon+"."+skinIcon.getFileType()));
			
			final URL url = new URL(_avatar);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty(
			    "User-Agent",
			    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			final BufferedImage avatarPicture = ImageIO.read(connection.getInputStream());
			
			String name = _name;
			int characterCounter = name.length();
			int levelT = _experience;
			
			if(characterCounter > skin.getNameLengthLimit() && skin.getNameLengthLimit() != 0)
				name = name.substring(0, skin.getNameLengthLimit());
			
			if(!skin.getFileType().equals("gif")) {
				BufferedImage rank = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				int rankW = rank.getWidth();
				int rankH = rank.getHeight();
				BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(rank, 0, 0, null);
				if(skin.getBarX() > 0 || skin.getBarY() > 0)
					g.drawImage(experienceBar, skin.getBarX(), skin.getBarY(), null);
				if(skin.getIconX() > 0 || skin.getIconY() > 0)
					g.drawImage(blurImage(level), skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
				if(skin.getAvatarX() > 0 || skin.getAvatarY() > 0)
					g.drawImage(avatarPicture, skin.getAvatarX(), skin.getAvatarY(), skin.getAvatarWidth(), skin.getAvatarHeight(), null);
				Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
				g.setColor(color);
				g.setFont(new Font("Nexa Bold", Font.PLAIN, skin.getTextFontSize()));
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if(skin.getExpTextX() > 0 || skin.getExpTextY() > 0)
					g.drawString("Exp:", skin.getExpTextX(), skin.getExpTextY());
				if(skin.getPercentTextX() > 0 || skin.getPercentTextY() > 0)
					g.drawString(levelT+"%", skin.getPercentTextX(), skin.getPercentTextY());
				var rankString = insertDots(_rank);
				if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0)
					g.drawString("Rank:  #"+rankString, skin.getPlacementX(), skin.getPlacementY());
				if(skin.getNameX() > 0 || skin.getNameY() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
					g.drawString(name, skin.getNameX(), skin.getNameY());
				}
				
				ImageIO.write(overlay, skin.getFileType(), new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType())), gif.getFrame(0).getType(), 20, true);
				int rankW = gif.getWidth();
				int rankH = gif.getHeight();
				for(var i = 0; i < frames; i++) {
					BufferedImage rank = gif.getFrame(i);
					BufferedImage overlay = new BufferedImage(rankW, rankH, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = overlay.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(rank, 0, 0, null);
					if(skin.getBarX() > 0 || skin.getBarY() > 0)
						g.drawImage(experienceBar, skin.getBarX(), skin.getBarY(), null);
					if(skin.getIconX() > 0 || skin.getIconY() > 0)
						g.drawImage(blurImage(level), skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
					if(skin.getAvatarX() > 0 || skin.getAvatarY() > 0)
						g.drawImage(avatarPicture, skin.getAvatarX(), skin.getAvatarY(), skin.getAvatarWidth(), skin.getAvatarHeight(), null);
					Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
					g.setColor(color);
					g.setFont(new Font("Nexa Bold", Font.PLAIN, skin.getTextFontSize()));
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if(skin.getExpTextX() > 0 || skin.getExpTextY() > 0)
						g.drawString("Exp:", skin.getExpTextX(), skin.getExpTextY());
					if(skin.getPercentTextX() > 0 || skin.getPercentTextY() > 0)
						g.drawString(levelT+"%", skin.getPercentTextX(), skin.getPercentTextY());
					var rankString = insertDots(_rank);
					if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0)
						g.drawString("Rank:  #"+rankString, skin.getPlacementX(), skin.getPlacementY());
					if(skin.getNameX() > 0 || skin.getNameY() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
						g.drawString(name, skin.getNameX(), skin.getNameY());
					}
					
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"rank_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
			if(file1.length() > 8000000 && e.getGuild().getBoostCount() < 15) {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESSION)).queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("rank", "rank_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"rank_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
							e.getChannel().sendFile(file2, "rank."+skin.getFileType()).queue(complete -> {
								file2.delete();
							}, error -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_SEND_ERR)).build()).queue();
								file2.delete();
							});
						}
					} catch(IOException | DataFormatException e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESS_ERR)).build()).queue();
						logger.error("Compression error for file {}", skin.getSkinDescription()+skin.getFileType(), e1);
						file1.delete();
						message.delete().queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "rank."+skin.getFileType()).complete();
				file1.delete();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.RANK_ERR)).build()).queue();
			logger.error("Rank couldn't be drawn for guild {}", e.getGuild().getIdLong(), e1);
		}
	}
	
	public static void getProfile(GuildMessageReceivedEvent e, String _name, String _avatar, int _experiencePercentage, int _rank, int currentExperience, int rankUpExperience, int theme_id, Ranking user_details) {
		final var skinIcon = RankingSystem.SQLgetRankingIcons(user_details.getRankingIcon(), theme_id);
		final var skin = RankingSystem.SQLgetRankingProfile(user_details.getRankingProfile(), theme_id);
		if(skinIcon == null || skin == null) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PROFILE_ERR)).build()).queue();
			logger.error("Profile couldn't be drawn for guild {}. Skin not found!", e.getGuild().getIdLong());
			return;
		}
		
		try{
			BufferedImage experienceBar;
			if(_experiencePercentage != 0) {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+skin.getBarColor()+"_"+100+".png"));
				experienceBar = experienceBar.getSubimage(0, 0, (experienceBar.getWidth()*_experiencePercentage)/100, experienceBar.getHeight());
			}
			else {
				experienceBar = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/ExperienceBar/exp"+0+"_"+0+".png"));
			}
			int rankIcon = RankingSystem.SQLgetLevels(e.getGuild().getIdLong(), theme_id).parallelStream().filter(f -> f.getLevel() == user_details.getLevel()).findAny().orElse(null).getRankIcon();
			BufferedImage level = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Rank/level_"+user_details.getRankingIcon()+"_"+rankIcon+"."+skinIcon.getFileType()));
			
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
			
			if(characterCounter > skin.getNameLengthLimit() && skin.getNameLengthLimit() != 0)
				name = name.substring(0, skin.getNameLengthLimit());
			
			if(!skin.getFileType().equals("gif")) {
				BufferedImage profile = ImageIO.read(new File("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				int profileW = profile.getWidth();
				int profileH = profile.getHeight();
				BufferedImage overlay = new BufferedImage(profileW, profileH, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = overlay.createGraphics();
				g.drawImage(profile, 0, 0, null);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if(skin.getBarX() > 0 || skin.getBarY() > 0)
					g.drawImage(experienceBar, skin.getBarX(), skin.getBarY(), null);
				if(skin.getIconX() > 0 || skin.getIconY() > 0)
					g.drawImage(blurImage(level), skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
				if(skin.getAvatarX() > 0 || skin.getAvatarY() > 0)
					g.drawImage(avatarPicture, skin.getAvatarX(), skin.getAvatarY(), skin.getAvatarWidth(), skin.getAvatarHeight(), null);
				Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
				g.setColor(color);
				g.setFont(new Font("Nexa Bold", Font.PLAIN, skin.getTextFontSize()));
				if(skin.getExpTextX() > 0 || skin.getExpTextY() > 0) {
					g.drawString("Exp:", skin.getExpTextX(), skin.getExpTextY());
				}
				if(skin.getPercentTextX() > 0 || skin.getPercentTextY() > 0) {
					g.drawString(levelT+"%", skin.getPercentTextX(), skin.getPercentTextY());
				}
				
				if(skin.getDescriptionMode() == 0) {
					if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
						g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), skin.getLevelY());
					if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0)
						g.drawString(insertDots(user_details.getExperience()), skin.getExperienceX(), skin.getExperienceY());
					if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0)
						g.drawString(insertDots(user_details.getCurrency()), skin.getCurrencyX(), skin.getCurrencyY());
					if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0)
						g.drawString(insertDots(rank), skin.getPlacementX(), skin.getPlacementY());
				}
				else if(skin.getDescriptionMode() == 1) {
					if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
						g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), getCenteredString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), g), skin.getLevelY());
					if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0) {
						var experienceString = insertDots(user_details.getExperience());
						g.drawString(experienceString, getCenteredString(experienceString, skin.getExperienceX(), g), skin.getExperienceY());
					}
					if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0) {
						var currencyString = insertDots(user_details.getCurrency());
						g.drawString(currencyString, getCenteredString(currencyString, skin.getCurrencyX(), g), skin.getCurrencyY());
					}
					if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0) {
						var rankString = insertDots(rank);
						g.drawString(rankString, getCenteredString(rankString, skin.getPlacementX(), g), skin.getPlacementY());
					}
				}
				else if(skin.getDescriptionMode() == 2) {
					if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
						g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), getRightString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), g), skin.getLevelY());
					if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0) {
						var experienceString = insertDots(user_details.getExperience());
						g.drawString(""+experienceString, getRightString(""+experienceString, skin.getExperienceX(), g), skin.getExperienceY());
					}
					if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0) {
						var currencyString = insertDots(user_details.getCurrency());
						g.drawString(currencyString, getRightString(currencyString, skin.getCurrencyX(), g), skin.getCurrencyY());
					}
					if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0) {
						var rankString = insertDots(rank);
						g.drawString(rankString, getRightString(rankString, skin.getPlacementX(), g), skin.getPlacementY());
					}
				}
				if(skin.getExpReachX() > 0 || skin.getExpReachY() > 0) {
					var currentExperienceString = insertDots(currentExperience);
					g.drawString(currentExperienceString+"/"+insertDots(rankUpExperience), getRightString(""+currentExperienceString, skin.getExpReachX(), g), skin.getExpReachY());
				}
				if(skin.getNameX() > 0 || skin.getNameY() > 0) {
					g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
					g.drawString(name, skin.getNameX(), skin.getNameY());
				}
				
				ImageIO.write(overlay, skin.getFileType(), new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType()));
				g.dispose();
			}
			else {
				final GifImage gif = GifDecoder.read(new FileInputStream("./files/RankingSystem/"+theme_id+"/Skins/"+skin.getSkinDescription()+"."+skin.getFileType()));
				var frames = gif.getFrameCount();
				GifSequenceWriter writer = new GifSequenceWriter(new FileImageOutputStream(new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType())), gif.getFrame(0).getType(), 20, true);
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
					if(skin.getBarX() > 0 || skin.getBarY() > 0)
						g.drawImage(experienceBar, skin.getBarX(), skin.getBarY(), null);
					if(skin.getIconX() > 0 || skin.getIconY() > 0)
						g.drawImage(blurImage(level), skin.getIconX(), skin.getIconY(), skin.getIconWidth(), skin.getIconHeight(), null);
					if(skin.getAvatarX() > 0 || skin.getAvatarY() > 0)
						g.drawImage(avatarPicture, skin.getAvatarX(), skin.getAvatarY(), skin.getAvatarWidth(), skin.getAvatarHeight(), null);
					Color color = new Color(skin.getColorR(), skin.getColorG(), skin.getColorB());
					g.setColor(color);
					g.setFont(new Font("Nexa Bold", Font.PLAIN, skin.getTextFontSize()));
					if(skin.getExpTextX() > 0 || skin.getExpTextY() > 0) {
						g.drawString("Exp:", skin.getExpTextX(), skin.getExpTextY());
					}
					if(skin.getPercentTextX() > 0 || skin.getPercentTextY() > 0) {
						g.drawString(levelT+"%", skin.getPercentTextX(), skin.getPercentTextY());
					}
					
					if(skin.getDescriptionMode() == 0) {
						if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
							g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), skin.getLevelY());
						if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0)
							g.drawString(insertDots(user_details.getExperience()), skin.getExperienceX(), skin.getExperienceY());
						if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0)
							g.drawString(insertDots(user_details.getCurrency()), skin.getCurrencyX(), skin.getCurrencyY());
						if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0)
							g.drawString(insertDots(rank), skin.getPlacementX(), skin.getPlacementY());
					}
					else if(skin.getDescriptionMode() == 1) {
						if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
							g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), getCenteredString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), g), skin.getLevelY());
						if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0) {
							var experienceString = insertDots(user_details.getExperience());
							g.drawString(experienceString, getCenteredString(experienceString, skin.getExperienceX(), g), skin.getExperienceY());
						}
						if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0) {
							var currencyString = insertDots(user_details.getCurrency());
							g.drawString(currencyString, getCenteredString(currencyString, skin.getCurrencyX(), g), skin.getCurrencyY());
						}
						if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0) {
							var rankString = insertDots(rank);
							g.drawString(rankString, getCenteredString(rankString, skin.getPlacementX(), g), skin.getPlacementY());
						}
					}
					else if(skin.getDescriptionMode() == 2) {
						if(skin.getLevelX() > 0 || skin.getLevelY() > 0)
							g.drawString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), getRightString(""+(user_details.getDisplayLevel() > 0 ? user_details.getDisplayLevel() : user_details.getLevel()), skin.getLevelX(), g), skin.getLevelY());
						if(skin.getExperienceX() > 0 || skin.getExperienceY() > 0) {
							var experienceString = insertDots(user_details.getExperience());
							g.drawString(""+experienceString, getRightString(""+experienceString, skin.getExperienceX(), g), skin.getExperienceY());
						}
						if(skin.getCurrencyX() > 0 || skin.getCurrencyY() > 0) {
							var currencyString = insertDots(user_details.getCurrency());
							g.drawString(currencyString, getRightString(currencyString, skin.getCurrencyX(), g), skin.getCurrencyY());
						}
						if(skin.getPlacementX() > 0 || skin.getPlacementY() > 0) {
							var rankString = insertDots(rank);
							g.drawString(rankString, getRightString(rankString, skin.getPlacementX(), g), skin.getPlacementY());
						}
					}
					if(skin.getExpReachX() > 0 || skin.getExpReachY() > 0) {
						var currentExperienceString = insertDots(currentExperience);
						g.drawString(currentExperienceString+"/"+insertDots(rankUpExperience), getRightString(""+currentExperienceString, skin.getExpReachX(), g), skin.getExpReachY());
					}
					if(skin.getNameX() > 0 || skin.getNameY() > 0) {
						g.setFont(new Font("Nexa Bold", Font.BOLD, skin.getNameFontSize()));
						g.drawString(name, skin.getNameX(), skin.getNameY());
					}
					
					writer.writeToSequence(overlay);
					g.dispose();
				}
				writer.close();
			}
			final File file1 = new File(IniFileReader.getTempDirectory()+"profile_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
			if(file1.length() > 8000000 && e.getGuild().getBoostCount() < 15) {
				e.getChannel().sendMessage(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESSION)).queue(message -> {
					try {
						String [] args = {file1.getCanonicalPath(), file1.getCanonicalPath().replace("profile", "profile_compressed")};
						final var output = GifOptimizer.startGifOptimization(args);
						if(output == null) {
							file1.delete();
							message.delete().queue();
							final File file2 = new File(IniFileReader.getTempDirectory()+"profile_compressed_gu"+e.getGuild().getId()+"us"+e.getMember().getUser().getId()+"."+skin.getFileType());
							e.getChannel().sendFile(file2, "profile."+skin.getFileType()).queue(complete -> {
								file2.delete();
							}, error -> {
								e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_SEND_ERR)).build()).queue();
								file2.delete();
							});;
						}
					} catch(IOException | DataFormatException e1) {
						e.getChannel().sendMessage(new EmbedBuilder().setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.GIF_COMPRESS_ERR)).build()).queue();
						logger.error("Compression error for file {}", skin.getSkinDescription()+skin.getFileType(), e1);
						file1.delete();
						message.delete().queue();
					}
				});
			}
			else {
				e.getChannel().sendFile(file1, "profile."+skin.getFileType()).complete();
				file1.delete();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle(STATIC.getTranslation(e.getMember(), Translation.EMBED_TITLE_ERROR)).setDescription(STATIC.getTranslation(e.getMember(), Translation.PROFILE_ERR)).build()).queue();
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
