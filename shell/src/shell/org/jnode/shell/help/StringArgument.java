/*
 * $Id$
 */

package org.jnode.shell.help;

import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public class StringArgument extends Argument {

	public StringArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public StringArgument(String name, String description) {
		super(name, description);
	}

	public String complete(String partial) {
		String result = CommandLine.escape(partial, true);	// force quote
		return result.substring(0, result.length() - 1);	// remove ending quote
	}
	
}
