/**
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 */
public class JavaCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the class to excute");
	static final Argument ARG_ARGS = new Argument("arg", "the argument(s) to pass to the class", Argument.MULTI);

	public static Help.Info HELP_INFO = new Help.Info(
		"java",
		"Execute the main method of the given java class",
		new Parameter[]{
			new Parameter(ARG_CLASS, Parameter.MANDATORY),
			new Parameter(ARG_ARGS, Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new JavaCommand().execute(HELP_INFO.parse(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(
		ParsedArguments cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
		throws Exception {

		Class cls = ARG_CLASS.getClass(cmdLine);
		Method mainMethod = cls.getMethod("main", new Class[] { String[].class });

		String[] clsArgs = ARG_ARGS.getValues(cmdLine);
		if (clsArgs == null) {
			clsArgs = new String[0];
		}

		try {
			mainMethod.invoke(null, new Object[] { clsArgs });
		} catch (InvocationTargetException ex) {
			ex.getTargetException().printStackTrace(err);
		}
	}

}
