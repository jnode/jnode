/*
 * $Id$
 */

package org.jnode.shell.help;


/**
 * @author qades
 */
public class ClassNameArgument extends Argument {

	public ClassNameArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public ClassNameArgument(String name, String description) {
		super(name, description);
	}

	// here the specific command line completion would be implemented

	public Class getClass(ParsedArguments cmdLine) throws ClassNotFoundException {
		return Class.forName(getValue(cmdLine));
	}
}
