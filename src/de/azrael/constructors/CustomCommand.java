package de.azrael.constructors;

import de.azrael.enums.CommandAction;

/**
 * Object class for custom commands
 * @author xHelixstorm
 *
 */

public class CustomCommand {
	private String command;
	private String output;
	private CommandAction action;
	private String description;
	private int level;
	private long targetChannel;
	private boolean enabled;
	
	/**
	 * Default constructor to instantiate object with command information
	 * @param command name of the command
	 * @param output printed message upon use
	 * @param action command type
	 * @param description short explaination will be displayed in 'help' with other commands
	 * @param level usage restriction for roles with lower level than the one of the command
	 * @param targetChannel to force the message output into a specific channel
	 * @param enabled command can be used only if it's enabled, else throw no output message
	 */
	
	public CustomCommand(String command, String output, int action, String description, int level, long targetChannel, boolean enabled) {
		this.command = command;
		this.output = output;
		this.action = CommandAction.valueOfAction(action);
		this.description = description;
		this.level = level;
		this.targetChannel = targetChannel;
		this.enabled = enabled;
	}
	
	/**
	 * retrieve the command name
	 * @return
	 */
	
	public String getCommand() {
		return command;
	}

	/**
	 * Retrieve the output message
	 * @return
	 */
	
	public String getOutput() {
		return output;
	}
	
	/**
	 * Retrieve the CommandAction enum for this command
	 * @return
	 */

	public CommandAction getAction() {
		return action;
	}
	
	/**
	 * Retrieve help description
	 * @return
	 */

	public String getDescription() {
		return description;
	}
	
	/**
	 * Retrieve restriction level
	 * @return
	 */

	public int getLevel() {
		return level;
	}
	
	/**
	 * Retrieve forced targeted channel
	 * @return
	 */

	public long getTargetChannel() {
		return targetChannel;
	}
	
	/**
	 * Verify that the command is enabled
	 * @return
	 */

	public boolean isEnabled() {
		return enabled;
	}
}
