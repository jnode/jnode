/*
 * $Id$
 */
package org.jnode.shell.command;

import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmType;

/**
 * @author epr
 */
public class CompileCommand {

        static final ClassNameArgument ARG_CLASS = new ClassNameArgument("className", "the class file to compile");

	public static Help.Info HELP_INFO = new Help.Info(
		"compile",
		"Compile a Java class",
		new Parameter[]{
			new Parameter(ARG_CLASS, Parameter.MANDATORY)
		}
	);

	public static void main(String[] args)
	throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		String filename = ARG_CLASS.getValue(cmdLine);
		final VmType type = VmSystem.getSystemClassLoader().loadClass(filename, true);
		final long start = System.currentTimeMillis();
		final int count = type.compile();
		final long end = System.currentTimeMillis();
		System.out.println("Compiling " + count + " methods took " + (end-start) + "ms");
	}

}
