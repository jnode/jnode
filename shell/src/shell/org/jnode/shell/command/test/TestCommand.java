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
 
package org.jnode.shell.command.test;

import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ClassNameArgument;

/**
 * @author epr
 */
public class TestCommand {

    static final ClassNameArgument ARG_CLASS = new ClassNameArgument(
            "classname", "the class representing the testcase");

    // static final Argument ARG_ARGS = new Argument("arg", "the argument(s) to
    // pass to the testcase", Argument.MULTI);

    public static Help.Info HELP_INFO = new Help.Info("test",
            "Run a JUnit testcase", new Parameter[] { new Parameter(ARG_CLASS,
                    Parameter.MANDATORY)
            // new Parameter(ARG_ARGS, Parameter.OPTIONAL)
            });

    public static void main(String[] args) throws Exception {
        new TestCommand().execute(HELP_INFO.parse(args), System.in, System.out,
                System.err);
    }

    /**
     * Execute this command
     */
    public void execute(ParsedArguments cmdLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {

        Class clazz = ARG_CLASS.getClass(cmdLine);

        JNodeTestRunner.run(clazz);
    }

    private static class JNodeTestRunner extends TestRunner {

        public static void run(Class testClass) {
            JNodeTestRunner runner = new JNodeTestRunner();
            TestSuite suite = new TestSuite(testClass);
            runner.doRun(suite);
        }
    }

}
