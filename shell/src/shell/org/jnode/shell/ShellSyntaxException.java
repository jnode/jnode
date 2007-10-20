package org.jnode.shell;

public class ShellSyntaxException extends ShellException {

	private static final long serialVersionUID = 1L;

	public ShellSyntaxException(String s, Throwable cause) {
		super(s, cause);
	}

	public ShellSyntaxException(String s) {
		super(s);
	}

	public ShellSyntaxException(Throwable cause) {
		super(cause);
	}

}
