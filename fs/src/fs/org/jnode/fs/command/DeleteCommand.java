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
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 */
public class DeleteCommand {

	static final FileArgument ARG_DIR = new FileArgument("file/dir", "delete the file or directory");
	public static Help.Info HELP_INFO =
		new Help.Info(
			"file/dir",
			"the file or directory to delete",
			new Parameter[] { new Parameter(ARG_DIR, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final File entry = ARG_DIR.getFile(cmdLine);
		if (entry.exists()) {
			entry.delete();
		} else {
			System.err.println(entry + " does not exist");
		}
	}

}
