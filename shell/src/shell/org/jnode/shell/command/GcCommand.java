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
public class GcCommand {

        public static Help.Info HELP_INFO = new Help.Info(
		"gc",
		"Start the garbage collector"
	);

	public static void main(String[] args)
	throws Exception {
		new GcCommand().execute(new CommandLine(args), System.in, System.out, System.err);
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
			
		final Runtime rt = Runtime.getRuntime();
		out.println("Memory size: " + rt.totalMemory());
		out.println("Free memory: " + rt.freeMemory());

		out.println("Starting gc...");
		
		long start = System.currentTimeMillis();
		rt.gc();
		long end = System.currentTimeMillis();
		
		out.println("Memory size: " + rt.totalMemory());
		out.println("Free memory: " + rt.freeMemory());
		out.println("Time taken : " + (end-start) + "ms");
	}

}
