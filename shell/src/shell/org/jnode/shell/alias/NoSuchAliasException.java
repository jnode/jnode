/*
 * $Id$
 */
package org.jnode.shell.alias;

import org.jnode.shell.ShellException;

/**
 * @author epr
 */
public class NoSuchAliasException extends ShellException {

	/**
	 * 
	 */
	public NoSuchAliasException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchAliasException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoSuchAliasException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public NoSuchAliasException(String s) {
		super(s);
	}
}
