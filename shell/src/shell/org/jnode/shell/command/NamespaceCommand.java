/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.*;

/**
 * @author epr
 */
public class NamespaceCommand {

        public static Help.Info HELP_INFO = new Help.Info(
		"namespace",
		"Print the contents of the system namespace"
	);

	public static void main(String[] args)
	throws Exception {
		new NamespaceCommand().execute(new CommandLine(args), System.in, System.out, System.err);
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
			
		Set names = InitialNaming.nameSet();
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			String name = (String)i.next();
			out.println(name);
		}
	}

}
