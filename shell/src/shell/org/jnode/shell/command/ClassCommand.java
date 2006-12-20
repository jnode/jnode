/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command;

import java.io.PrintStream;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ClassNameArgument;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassType;
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
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class type = cl.loadClass(className);
		showClass(type, System.out);
	}
	
	private static void showClass(Class<?> type, PrintStream out) {
        final VmType<?> vmType = type.getVmClass();
		out.println("Name             : " + type.getName());
		//out.println("Is abstract      : " + type.isAbstract());
		out.println("Is array         : " + type.isArray());
		out.println("Is primitive     : " + type.isPrimitive());
        out.println("Shared statics   : " + vmType.isSharedStatics());
        out.println("Is initialized   : " + vmType.isInitialized());
		out.println("Protection domain: " + type.getProtectionDomain());
        
        if (vmType instanceof VmClassType) {
            out.println("#Instances       : " + ((VmClassType<?>)vmType).getInstanceCount());
        }
        if (vmType instanceof VmArrayClass) {
            out.println("Total length     : " + ((VmArrayClass<?>)vmType).getTotalLength());            
            out.println("Maximum length   : " + ((VmArrayClass<?>)vmType).getMaximumLength());            
        }
	}
}
