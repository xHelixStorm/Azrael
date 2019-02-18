package commandsContainer;

import java.awt.Color;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import rankingSystem.Weapon_Abbvs;

public class RandomshopExecution {
	Logger logger = LoggerFactory.getLogger(RandomshopExecution.class);
	
	public static void runHelp(MessageReceivedEvent e, List<Weapon_Abbvs> abbreviations) {
		EmbedBuilder message = new EmbedBuilder().setColor(Color.BLUE).setTitle("Randomshop exclusives");
		if(abbreviations.size() == 0) {
			e.getTextChannel().sendMessage(message.setDescription("The randomshop is currently not available. Please retry later!").build()).queue();
		}
		else {
			
		}
	}
}
