/**
 * $Id$
 */
package org.jnode.shell.command.test;

import java.io.InputStream;
import java.io.PrintStream;

import junit.textui.TestRunner;

import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 */
public class TestCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the class representing the testcase");
//	static final Argument ARG_ARGS = new Argument("arg", "the argument(s) to pass to the testcase", Argument.MULTI);

	public static Help.Info HELP_INFO = new Help.Info(
		"test",
		"Run a JUnit testcase",
		new Parameter[]{
			new Parameter(ARG_CLASS, Parameter.MANDATORY)
//			new Parameter(ARG_ARGS, Parameter.OPTIONAL)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new TestCommand().execute(HELP_INFO.parse(args), System.in, System.out, System.err);
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

                Class clazz = ARG_CLASS.getClass(cmdLine);

		TestRunner.run(clazz);
	}

}
