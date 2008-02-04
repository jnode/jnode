package org.jnode.apps.jpartition.commands.framework;

public class CommandException extends Exception {
	private static final long serialVersionUID = -8340890789850970389L;

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
