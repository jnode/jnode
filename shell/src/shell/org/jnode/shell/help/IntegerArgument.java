/*
 * $Id$
 */
package org.jnode.shell.help;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IntegerArgument extends Argument {
	
	public IntegerArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public IntegerArgument(String name, String description) {
		super(name, description);
	}

	public String complete(String partial) {
		return partial;
	}
	
	public int getInteger(ParsedArguments args) {
		return Integer.parseInt(this.getValue(args));
	}
}
