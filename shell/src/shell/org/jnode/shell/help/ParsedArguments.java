/*
 * $Id$
 */
package org.jnode.shell.help;

import java.util.Map;

/**
 * @author qades
 */
public class ParsedArguments {
	Map args;

	ParsedArguments(Map args) {
		this.args = args;
	}

	public int size() {
		return args.size();
	}

	String[] getValues(Argument arg) {
		return (String[])args.get(arg);
	}

	boolean isSet(Parameter param) {
		return args.containsKey(param);
	}
}