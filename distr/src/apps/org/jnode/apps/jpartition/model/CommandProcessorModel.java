package org.jnode.apps.jpartition.model;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.commands.framework.CommandProcessorListener;
import org.jnode.apps.jpartition.swingview.ErrorReporter;

public class CommandProcessorModel extends AbstractModel
				implements CommandProcessorListener
{
	private static final Logger log = Logger.getLogger(CommandProcessorModel.class);
		
	private final CommandProcessor commandProcessor;
	
	public CommandProcessorModel()
	{
		commandProcessor = new CommandProcessor();
		commandProcessor.addListener(this);
	}
	
	public void processCommands()
	{
		try
		{
			commandProcessor.process();
		}
		catch(Throwable t)
		{
			ErrorReporter.reportError(log, this, t);			
		}
		finally
		{
			propSupport.firePropertyChange("commandsProcessed", null, this);
		}		
	}
	
	public void commandAdded(CommandProcessor processor, Command command) {
		propSupport.firePropertyChange("commandAdded", null, command);
	}

	public void commandStarted(CommandProcessor processor, Command command) {
		propSupport.firePropertyChange("commandStarted", command, command);
	}

	public void commandFinished(CommandProcessor processor, Command command) {
		propSupport.firePropertyChange("commandFinished", command, command);
	}

	public void commandRemoved(CommandProcessor processor, Command command) {
		propSupport.firePropertyChange("commandRemoved", command, command);
	}
}
