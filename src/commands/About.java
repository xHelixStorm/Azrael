package commands;

import fileManagement.IniFileReader;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sql.SqlConnect;
import util.STATIC;

public class About implements Command {

	@Override
	public boolean called(String[] args, MessageReceivedEvent e) {
		return false;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent e) {
		if(IniFileReader.getAboutCommand().equals("true")){
			long channel = e.getTextChannel().getIdLong();
			long guild_id = e.getGuild().getIdLong();
			SqlConnect.SQLgetChannelID(guild_id, "bot");
			long channel_id = SqlConnect.getChannelID();
			
			if(channel != channel_id && channel_id != 0){
				e.getTextChannel().sendMessage(e.getMember().getAsMention()+" I'm not allowed to execute commands in this channel, please write it again in <#"+channel_id+">").queue();
			}
			else{
				EmbedBuilder messageBuilder = new EmbedBuilder().setColor(0x00AE86).setThumbnail(e.getJDA().getSelfUser().getEffectiveAvatarUrl()).setTitle("About Page!");
				messageBuilder.setAuthor("Azrael", e.getJDA().getSelfUser().getEffectiveAvatarUrl());
				messageBuilder.setDescription("Here are all details about myself!");
				messageBuilder.addField("BOT VERSION", STATIC.getVersion_New(), true);
				messageBuilder.addField("DEVELOPER", "Java developer xHelixStorm", true);
				messageBuilder.addBlankField(false);
				messageBuilder.addField("Functionalities", "- Self designed ranking system.\n"
						+ "- Various bot settings for numerous moderation tools such as an automated mute system, bad-word filter, name filter and much more.\n"
						+ "- Entertainment commands to display your current level, ranking gain dailies, purchase from an integrated shop, take a look at your inventory or simply bring up cute pictures of pugs or cats.\n\n"
						+ "You can see all enabled commands under "+IniFileReader.getCommandPrefix()+"commands", false);
				messageBuilder.addBlankField(false);
				messageBuilder.addField("SOURCE CODE", "[Check the latest updates of Azrael on GitHub!](https://github.com/xHelixStorm/Azrael)", false);
				e.getTextChannel().sendMessage(messageBuilder.build()).queue();
			}
		}
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent e) {
		SqlConnect.clearAllVariables();
	}

	@Override
	public String help() {
		return null;
	}
	
	

}
