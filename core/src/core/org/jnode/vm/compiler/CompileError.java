/*
 * Created on Feb 20, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.vm.compiler;

/**
 * @author epr
 */
public class CompileError extends LinkageError {

	/**
	 * 
	 */
	public CompileError() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CompileError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public CompileError(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public CompileError(String s) {
		super(s);
	}
}
