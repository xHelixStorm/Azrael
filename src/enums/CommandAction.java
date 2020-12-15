package enums;

import java.util.HashMap;

public enum CommandAction {
	
	MESSAGE		(1, false), 
	YOUTUBE		(2, true),
	ROLEADD		(3, false),
	ROLEREMOVE	(4, false);
	
	/**
	 * Defined map to retrieve enum by numeric action type
	 */
	
	private final static HashMap<Integer, CommandAction> BY_ACTION = new HashMap<Integer, CommandAction>();
	
	/**
	 * Map setter
	 */
	
	static {
		for(CommandAction e : values()) {
			BY_ACTION.put(e.action, e);
		}
	}
	
	public final int action;
	public final boolean inputRequired;
	
	/**
	 * Enum constructor
	 * @param action numeric type of action
	 * @param inputRequired forces parameter input
	 */
	
	private CommandAction(int action, boolean inputRequired) {
		this.action = action;
		this.inputRequired = inputRequired;
	}
	
	/**
	 * Retrieve enum by action
	 * @param action numeric type of action
	 * @return enum
	 */
	
	public static CommandAction valueOfAction(final Integer action) {
		return BY_ACTION.get(action);
	}
}
