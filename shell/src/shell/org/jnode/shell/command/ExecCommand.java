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

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.*;
import org.jnode.shell.help.argument.ClassNameArgument;

/**
 * @author epr
 */
public class ExecCommand {

	public static Help.Info HELP_INFO = new Help.Info(
		"exec",
		"Execute the given class in a new process",
		new Parameter[]{
			new Parameter(new ClassNameArgument("classname", "the class to execute"), Parameter.MANDATORY),
			new Parameter(new Argument("arg", "the argument(s) to pass to the class", Argument.MULTI), Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new ExecCommand().execute(new CommandLine(args), System.in, System.out, System.err);
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

		Runtime.getRuntime().exec(cmdLine.toStringArray());
	}

}
