/*
 * $Id$
 */
package org.jnode.fs.command;

import java.io.InputStream;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 */
public class CatCommand {

        static final FileArgument ARG_FILE = new FileArgument("file", "the file to print out");

	public static Help.Info HELP_INFO = new Help.Info(
		"cat",
		"Print the contents of the given file",
		new Parameter[]{
			new Parameter(ARG_FILE, Parameter.MANDATORY)
		}
	);

	public static void main(String[] args)
	throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		InputStream is = ARG_FILE.getInputStream(cmdLine);
		int len;
		final byte[] buf = new byte[1024];
		while ((len = is.read(buf)) > 0) {
			System.out.write(buf, 0, len);
		}
		System.out.flush();
		is.close();
	}

}
