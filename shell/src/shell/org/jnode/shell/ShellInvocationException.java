package org.jnode.shell;

public class ShellInvocationException extends ShellException {

	private static final long serialVersionUID = 1L;

	public ShellInvocationException(String s, Throwable cause) {
		super(s, cause);
	}

	public ShellInvocationException(String s) {
		super(s);
	}

	public ShellInvocationException(Throwable cause) {
		super(cause);
	}

}
