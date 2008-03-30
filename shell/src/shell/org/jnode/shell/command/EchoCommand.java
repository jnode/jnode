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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.StringArgument;

/**
 * @author epr
 */

public class EchoCommand extends AbstractCommand {

	public static Help.Info HELP_INFO = new Help.Info(
		"echo", "Print the given text",
		new Parameter[]{
			new Parameter(new StringArgument("arg", "the text to print", Argument.MULTI), Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args) throws Exception {
		new EchoCommand().execute(args);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
	throws Exception {
		Iterator<String> it = commandLine.iterator();
		int i = 0;
		while (it.hasNext()) {
			if (i > 0) {
				out.print(' ');
			}
			out.print(it.next());
			i++;
		}
		out.println();
	}
}
