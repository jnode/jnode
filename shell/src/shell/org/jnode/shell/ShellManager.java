/*
 * $Id$
 */
package org.jnode.shell;

/**
 * @author epr
 */
public interface ShellManager {

	/**
	 * Name used to bind this manager into the InitialNaming namespace.
	 * @see org.jnode.naming.InitialNaming
	 */
	public static final String NAME = "jnode/shellmanager";

	/**
	 * Gets the current shell 
	 */
	public Shell getCurrentShell();

	/**
	 * Register the new current shell
	 * @param currentShell
	 */
	public void registerShell(Shell currentShell);

}
