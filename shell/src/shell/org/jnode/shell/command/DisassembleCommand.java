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

import java.io.OutputStreamWriter;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ClassNameArgument;
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.argument.StringArgument;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Levente S\u00e1ntha
 */
public class DisassembleCommand {

	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("className", "the class to disassemble");
    static final StringArgument ARG_METHOD = new StringArgument("methodName", "the method to disassemble");
	static final IntegerArgument ARG_LEVEL = new IntegerArgument("level", "the optimization level");
	static final IntegerArgument ARG_TEST = new IntegerArgument("test", "If 1, the test compilers are used");
	static final Parameter PARAM_LEVEL = new Parameter(ARG_LEVEL, Parameter.OPTIONAL);
	static final Parameter PARAM_TEST = new Parameter(ARG_TEST, Parameter.OPTIONAL);
	
	public static Help.Info HELP_INFO = new Help.Info("disasm", "Disassemble a Java class or method",
            new Parameter[] {
                new Parameter(ARG_CLASS, Parameter.MANDATORY),
                new Parameter(ARG_METHOD, Parameter.OPTIONAL),
                PARAM_LEVEL, PARAM_TEST});

	public static void main(String[] args) throws Exception {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);

		final String className = ARG_CLASS.getValue(cmdLine);
        final String methodName = ARG_METHOD.getValue(cmdLine);
		final int level = PARAM_LEVEL.isSet(cmdLine) ? ARG_LEVEL.getInteger(cmdLine) : 0;
		final boolean test = PARAM_TEST.isSet(cmdLine) ? (ARG_TEST.getInteger(cmdLine) != 0) : false;
		
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class<?> cls = cl.loadClass(className);
		final VmType<?> type = cls.getVmClass();
		final long start = System.currentTimeMillis();
		final int count = type.disassemble(methodName, level, test, new OutputStreamWriter(System.out));
		final long end = System.currentTimeMillis();
		System.out.println("Disassembling " + count + " methods took " + (end - start) + "ms");
	}
}
