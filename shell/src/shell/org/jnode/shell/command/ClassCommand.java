/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.PrintStream;

import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ClassCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("class", "the class file to view");

	public static Help.Info HELP_INFO = new Help.Info("class", "View a Java class", new Parameter[] { new Parameter(ARG_CLASS, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		String className = ARG_CLASS.getValue(cmdLine);
		final VmType type = VmSystem.getSystemClassLoader().loadClass(className, true);
		showClass(type, System.out);
	}
	
	private static void showClass(VmType type, PrintStream out) {
		out.println("Name        : " + type.getName());
		out.println("Is abstract : " + type.isAbstract());
		out.println("Is array    : " + type.isArray());
		out.println("Is primitive: " + type.isPrimitive());
		out.println("Is compiled : " + type.isCompiled());
	}
}
