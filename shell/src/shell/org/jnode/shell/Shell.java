/*
 * $Id$
 */
package org.jnode.shell;

import org.jnode.shell.alias.AliasManager;

/**
 * @author epr
 */
public interface Shell {

	/**
	 * Gets the alias manager of this shell
	 */
	public AliasManager getAliasManager();

	/** 
	 * Gets the CommandHistory object associated with this shell.
	 */
	public CommandHistory getCommandHistory();

        /**
	 * Prints a list of choices for command line completion.
	 */
	public void list(String[] items);

}
