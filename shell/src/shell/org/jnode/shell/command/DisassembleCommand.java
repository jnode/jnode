/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.vm.LoadCompileService;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Levente S\u00e1ntha
 * @author clawley@jnode.org
 */
public class DisassembleCommand extends AbstractCommand {
    private final int maxTestLevel = 
        LoadCompileService.getHighestOptimizationLevel(true);
    private final int maxNontestLevel = 
        LoadCompileService.getHighestOptimizationLevel(false);
    private final int maxLevel = Math.max(maxTestLevel, maxNontestLevel);

    private final ClassNameArgument ARG_CLASS =
        new ClassNameArgument("className", Argument.MANDATORY, "the class to disassemble");
    private final StringArgument ARG_METHOD =
        new StringArgument("methodName", Argument.OPTIONAL, "the method to disassemble");
    private final IntegerArgument ARG_LEVEL =
        new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, "the optimization level");
    private final FlagArgument ARG_TEST = 
        new FlagArgument("test", Argument.OPTIONAL, "If set, the test compilers are used");

    public DisassembleCommand() {
        super("disassemble a Java class or method");
        registerArguments(ARG_CLASS, ARG_METHOD, ARG_LEVEL, ARG_TEST);
    }

    public static void main(String[] args) throws Exception {
        new DisassembleCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        final PrintWriter out = getOutput().getPrintWriter();
        final PrintWriter err = getError().getPrintWriter();
        final String className = ARG_CLASS.getValue();
        final String methodName = ARG_METHOD.isSet() ? ARG_METHOD.getValue() : null;
        final int level = ARG_LEVEL.isSet() ? ARG_LEVEL.getValue() : 0;
        final boolean test = ARG_TEST.isSet();

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> cls;
        try {
            cls = cl.loadClass(className);
        } catch (ClassNotFoundException ex) {
            err.println("Class '" + className + "' not found");
            exit(1);
            // not reached
            return;  
        }
        final VmType<?> type = VmType.fromClass((Class<?>) cls);
        if (test) {
            if (maxTestLevel == -1) {
                err.println("No test compilers are currently registered");
                exit(1);
            } else if (maxTestLevel < level) {
                err.println("The highest (test) optimization level is " + maxTestLevel);
                exit(1);
            }
        } else if (maxNontestLevel < level) {
            err.println("The highest optimization level is " + maxNontestLevel);
            exit(1);
        }
        final long start = System.currentTimeMillis();
        final int count = type.disassemble(methodName, level, test, out);
        final long end = System.currentTimeMillis();
        out.println("Disassembling " + count + " methods took " + (end - start) + "ms");
    }
}
