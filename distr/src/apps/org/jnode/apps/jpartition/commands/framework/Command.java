package org.jnode.apps.jpartition.commands.framework;

public interface Command {
    public void execute(CommandProcessor processor) throws CommandException;

    public CommandStatus getStatus();
}
