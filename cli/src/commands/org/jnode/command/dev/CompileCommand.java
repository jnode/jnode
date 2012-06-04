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
import org.jnode.vm.LoadCompileService;
import org.jnode.vm.classmgr.VmType;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class CompileCommand extends AbstractCommand {
    
    private static final String help_class = "the class file to compile";
    private static final String help_level = "the optimization level";
    private static final String help_test = "use the test version of the compiler";
    private static final String help_super = "compile a Java class (bytecodes) to native code";
    private static final String err_no_test = "No test compilers are currently registered";
    private static final String err_opt_test = "The highest (test) optimization level is %d%n";
    private static final String err_opt = "The highest optimization level is %d%n";
    private static final String err_no_class = "Class '%s' not found%n";
    private static final String fmt_out = "Compiling %d methods took %dms%n";
    
    private final int maxTestLevel = LoadCompileService.getHighestOptimizationLevel(true);
    private final int maxNontestLevel = LoadCompileService.getHighestOptimizationLevel(false);
    private final int maxLevel = Math.max(maxTestLevel, maxNontestLevel);

    private final ClassNameArgument argClass;
    private final IntegerArgument argLevel;
    private final FlagArgument argTest;

    public CompileCommand() {
        super(help_super);
        argClass = new ClassNameArgument("className", Argument.MANDATORY, help_class);
        argLevel = new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, help_level);
        argTest  = new FlagArgument("test", Argument.OPTIONAL, help_test);
        registerArguments(argClass, argLevel, argTest);
    }

    public static void main(String[] args) throws Exception {
        new CompileCommand().execute(args);
    }
    
    @Override
    public void execute() throws Exception {
        final String className = argClass.getValue();
        final int level = argLevel.isSet() ? argLevel.getValue() : 0;
        final boolean test = argTest.isSet();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (test) {
            if (maxTestLevel == -1) {
                err.println(err_no_test);
                exit(1);
            } else if (maxTestLevel < level) {
                err.format(err_opt_test, maxTestLevel);
                exit(1);
            }
        } else if (maxNontestLevel < level) {
            err.format(err_opt, maxNontestLevel);
            exit(1);
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Class<?> cls;
        try {
            cls = cl.loadClass(className);
            final VmType<?> type = VmType.fromClass((Class<?>) cls);
            final long start = System.currentTimeMillis();
            final int count = type.compileRuntime(level, test);
            final long end = System.currentTimeMillis();
            out.format(fmt_out, count, (end - start));
        } catch (ClassNotFoundException ex) {
            err.format(err_no_class, className);
            exit(1);
        }
    }
}
