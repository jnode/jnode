/*
 * $Id$
 */

package org.jnode.shell.command;

import org.jnode.shell.help.*;

public class SetCommand {

	static final PropertyNameArgument ARG_KEY = new PropertyNameArgument("key", "the property name");
	static final StringArgument ARG_VALUE = new StringArgument("value", "the value to set the property to");

        public static final Help.Info HELP_INFO = new Help.Info(
		"set",
		"Sets a system property",
		new Parameter[]{
			new Parameter(ARG_KEY, Parameter.MANDATORY),
			new Parameter(ARG_VALUE, Parameter.MANDATORY),
		}
	);

	public static void main(String[] args) throws SyntaxErrorException {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

                String key = ARG_KEY.getValue(cmdLine);
		String value = ARG_VALUE.getValue(cmdLine);
		System.getProperties().setProperty(key, value);
	}

}
