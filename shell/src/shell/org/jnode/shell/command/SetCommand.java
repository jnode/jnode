/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
