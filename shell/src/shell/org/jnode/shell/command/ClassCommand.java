/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.PrintStream;

import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ClassCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("class", "the class file to view");

	public static Help.Info HELP_INFO = new Help.Info("class", "View a Java class", new Parameter[] { new Parameter(ARG_CLASS, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		String className = ARG_CLASS.getValue(cmdLine);
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class type = cl.loadClass(className);
		showClass(type, System.out);
	}
	
	private static void showClass(Class type, PrintStream out) {
		out.println("Name             : " + type.getName());
		//out.println("Is abstract      : " + type.isAbstract());
		out.println("Is array         : " + type.isArray());
		out.println("Is primitive     : " + type.isPrimitive());
		out.println("Protection domain: " + type.getProtectionDomain());
	}
}
