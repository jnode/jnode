/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.*;

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
