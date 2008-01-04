package org.jnode.apps.jpartition.commands.framework;

import org.apache.log4j.Logger;

abstract public class BaseCommand implements Command
{
	private static final Logger log = Logger.getLogger(BaseCommand.class);

	private CommandStatus status = CommandStatus.NOT_RUNNING;
	final private String name;

	protected BaseCommand(String name)
	{
		this.name = name;
	}

	final public void execute(CommandProcessor processor) throws CommandException
	{
		try
		{
			status = CommandStatus.RUNNING;
			processor.commandStarted(this);

			doExecute();
			status = CommandStatus.SUCCESS;
		}
		catch(CommandException e)
		{
			log.error("command failed", e);
			status = CommandStatus.FAILED;
			throw e;
		}
		catch(Throwable t)
		{
			log.error("command failed", t);
			status = CommandStatus.FAILED;
			throw new CommandException("command failed", t);
		}
	}

	final public CommandStatus getStatus()
	{
		return status;
	}

	abstract protected void doExecute() throws CommandException;

	@Override
	public String toString() {
		return status + " - " + name;
	}
}
