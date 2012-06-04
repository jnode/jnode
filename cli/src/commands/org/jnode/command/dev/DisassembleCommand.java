/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.command.dev;

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
    
    private static final String help_class = "the class to disassemble";
    private static final String help_method = "the method to disassemble";
    private static final String help_level = "the optimization level";
    private static final String help_test = "If set, the test compilers are used.";
    private static final String help_super = "Disassemble a Java class or method";
    private static final String err_no_class = "Class '%s' not found%n";
    private static final String err_no_test = "No test compilers are currently registered";
    private static final String err_test_opt = "The highest test optimization level is %d%n";
    private static final String err_opt = "The highest optimzation level is %d%n";
    private static final String fmt_out = "Disassembling %d methods took %dms%n";
    
    private final int maxTestLevel = LoadCompileService.getHighestOptimizationLevel(true);
    private final int maxNontestLevel = LoadCompileService.getHighestOptimizationLevel(false);
    private final int maxLevel = Math.max(maxTestLevel, maxNontestLevel);

    private final ClassNameArgument argClass;
    private final StringArgument argMethod;
    private final IntegerArgument argLevel;
    private final FlagArgument argTest;

    public DisassembleCommand() {
        super(help_super);
        argClass  = new ClassNameArgument("className", Argument.MANDATORY, help_class);
        argMethod = new StringArgument("methodName", Argument.OPTIONAL, help_method);
        argLevel  = new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, help_level);
        argTest   = new FlagArgument("test", Argument.OPTIONAL, help_test);
        registerArguments(argClass, argMethod, argLevel, argTest);
    }

    public static void main(String[] args) throws Exception {
        new DisassembleCommand().execute(args);
    }
    
    @Override
    public void execute() throws Exception {
        final PrintWriter out = getOutput().getPrintWriter();
        final PrintWriter err = getError().getPrintWriter();
        final String className = argClass.getValue();
        final String methodName = argMethod.isSet() ? argMethod.getValue() : null;
        final int level = argLevel.isSet() ? argLevel.getValue() : 0;
        final boolean test = argTest.isSet();

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> cls;
        try {
            cls = cl.loadClass(className);
        } catch (ClassNotFoundException ex) {
            err.format(err_no_class, className);
            exit(1);
            // not reached
            return;  
        }
        final VmType<?> type = VmType.fromClass((Class<?>) cls);
        if (test) {
            if (maxTestLevel == -1) {
                err.println(err_no_test);
                exit(1);
            } else if (maxTestLevel < level) {
                err.format(err_test_opt, maxTestLevel);
                exit(1);
            }
        } else if (maxNontestLevel < level) {
            err.format(err_opt, maxNontestLevel);
            exit(1);
        }
        final long start = System.currentTimeMillis();
        final int count = type.disassemble(methodName, level, test, out);
        final long end = System.currentTimeMillis();
        out.format(fmt_out, count, (end - start));
    }
}
