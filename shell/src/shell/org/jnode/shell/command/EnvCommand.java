/*
 * Created on Mar 15, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Properties;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.*;

/**
 * @author epr
 */
public class EnvCommand {

        public static Help.Info HELP_INFO = new Help.Info(
		"env",
		"Print the system properties"
	);

	public static void main(String[] args)
	throws Exception {
		new EnvCommand().execute(new CommandLine(args), System.in, System.out, System.err);
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

		Properties ps = System.getProperties();
		for (Iterator i = ps.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			String value = ps.getProperty(key);

			out.print(key);
			out.print('=');
			out.println(value);
		}
	}

}
