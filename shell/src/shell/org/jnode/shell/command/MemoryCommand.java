/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MemoryCommand {

	public static Help.Info HELP_INFO = new Help.Info("memory", "View the current memory status");

	public static void main(String[] args) throws Exception {
		new MemoryCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		final Runtime rt = Runtime.getRuntime();
		out.println("Total memory " + rt.totalMemory());
		out.println("Free memory  " + rt.freeMemory());
	}
}
