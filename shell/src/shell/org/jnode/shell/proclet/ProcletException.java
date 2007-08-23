package org.jnode.shell.proclet;

public class ProcletException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProcletException(String s) {
		super(s);
	}

	public ProcletException(String s, Throwable cause) {
		super(s, cause);
	}

	public ProcletException(Throwable cause) {
		super(cause);
	}

}
