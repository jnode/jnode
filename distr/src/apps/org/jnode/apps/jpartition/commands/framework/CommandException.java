package org.jnode.apps.jpartition.commands.framework;

public class CommandException extends Exception {
	public CommandException(String s, Throwable cause) {
		super(s, cause);
	}

	public CommandException(String s) {
		super(s);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}

}
