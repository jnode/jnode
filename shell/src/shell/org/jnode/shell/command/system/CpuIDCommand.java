/*
 * $Id$
 */
package org.jnode.shell.command.system;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.vm.CpuID;
import org.jnode.vm.Unsafe;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CpuIDCommand {

	public static Help.Info HELP_INFO = new Help.Info("cpuid", "Show the identification of the current CPU");

	public static void main(String[] args) throws Exception {
		new CpuIDCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err) {

		final CpuID id = Unsafe.getCurrentProcessor().getCPUID();
		out.println(id);
	}

}
