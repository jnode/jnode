/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command;

import java.lang.reflect.Field;

import javax.naming.NameNotFoundException;

import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.AliasArgument;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class HelpCommand {

    static final AliasArgument ARG_COMMAND = new AliasArgument("command", Help.getLocalizedHelp("help.arg.command"));
	static final Parameter PARAM_COMMAND = new Parameter(ARG_COMMAND, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"help",
			Help.getLocalizedHelp("help.desc"),
			new Parameter[] { PARAM_COMMAND });

	public static void main(String[] args)
	throws NameNotFoundException,
            HelpException {
		Help.Info info = HELP_INFO; // defaults to print own help

                ParsedArguments cmdLine = HELP_INFO.parse(args);
		if (PARAM_COMMAND.isSet(cmdLine))
			try {
				String cmd = ARG_COMMAND.getValue(cmdLine);
				Class clazz = null;
				try {
					final Shell shell = ShellUtils.getShellManager().getCurrentShell();
					clazz = shell.getAliasManager().getAliasClass(cmd);
				} catch (NoSuchAliasException ex) {
					clazz = Class.forName(cmd);
				}
				Field clInfo = clazz.getField(Help.INFO_FIELD_NAME);
				info = (Help.Info)clInfo.get(null); // static access
			} catch (ClassNotFoundException ex) {
				System.err.println("Class not found");
			} catch (NoSuchFieldException ex) {
				System.err.println("Class does not provide requested information");
			} catch (ClassCastException ex) {
				System.err.println("Embedded information is in wrong format");
			} catch (IllegalAccessException ex) {
				System.err.println("Embedded information is not public");
			} catch (SecurityException ex) {
				System.err.println("Access to class restricted");
			}
		info.help();
	}

}
