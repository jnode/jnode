/*
 * Created on Feb 26, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.assembler;

/**
 * @author epr
 */
public class UnresolvedObjectRefException extends Exception {

	/**
	 * 
	 */
	public UnresolvedObjectRefException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnresolvedObjectRefException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public UnresolvedObjectRefException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public UnresolvedObjectRefException(String s) {
		super(s);
	}
}
