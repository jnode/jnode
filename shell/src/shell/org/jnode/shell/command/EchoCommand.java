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

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.*;

/**
 * @author epr
 */
public class EchoCommand {

	public static Help.Info HELP_INFO = new Help.Info(
		"echo",
		"Print the given text",
		new Parameter[]{
			new Parameter(new StringArgument("arg", "the text to print", Argument.MULTI), Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new EchoCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(
		CommandLine cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
	throws Exception {

		int i = 0;
		while (cmdLine.hasNext()) {
			if (i > 0) {
				out.print(' ');
			}
			out.print(cmdLine.next());
			i++;
		}
		out.println();
	}

}
