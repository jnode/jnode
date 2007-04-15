package org.jnode.apps.jpartition.commands.framework;

public interface CommandProcessorListener {
	void commandAdded(CommandProcessor processor, Command command);
	void commandRemoved(CommandProcessor processor, Command command);
	void commandStarted(CommandProcessor processor, Command command);
	void commandFinished(CommandProcessor processor, Command command);
}
