/*
 * $Id$
 */
package org.jnode.fs.command;

import java.io.File;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CdCommand {

	static final FileArgument ARG_DIR = new FileArgument("directory", "the directory to switch to");
	public static Help.Info HELP_INFO = new Help.Info("cd", "Go to the given directory", new Parameter[] { new Parameter(ARG_DIR, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final File dir = ARG_DIR.getFile(cmdLine);
		if (dir.exists() && dir.isDirectory()) {
			System.getProperties().setProperty("user.dir", dir.getAbsolutePath());
		} else {
			System.err.println(dir + " is not a valid directory");
		}
	}

}
