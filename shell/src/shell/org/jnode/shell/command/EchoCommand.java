/**
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
