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

import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class TestCommand extends AbstractCommand {

    private final ClassNameArgument ARG_CLASS = new ClassNameArgument(
            "className", Argument.MANDATORY, "the class representing the testcase");

    public TestCommand() {
        super("Run a JUnit testcase");
        registerArguments(ARG_CLASS);
    }

    public static void main(String[] args) throws Exception {
        new TestCommand().execute(args);
    }

    /**
     * Execute this command
     * 
     * @throws ClassNotFoundException 
     */
    public void execute(CommandLine cmdLine, InputStream in,
            PrintStream out, PrintStream err) {
        try {
            Class<?> clazz = ARG_CLASS.getValueAsClass();
            TestResult res = new TestRunner().doRun(new TestSuite(clazz));
            if (!res.wasSuccessful()) {
                exit(1);
            }
        } catch (ClassNotFoundException ex) {
            err.println("Class not found: " + ex.getMessage());
            exit(2);
        }
    }
}
