/*
 * $Id$
 */
package org.jnode.shell;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public class ShellUtils {

	/**
	 * Get the current shell manager
	 * @return The current shell manager
	 * @throws NameNotFoundException
	 */
	public static ShellManager getShellManager() 
	throws NameNotFoundException {
		return (ShellManager)InitialNaming.lookup(ShellManager.NAME);
	}
}
