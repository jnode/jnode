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

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class ExecCommand extends AbstractCommand {
    
    private final ClassNameArgument ARG_CLASSNAME =
        new ClassNameArgument("className", Argument.MANDATORY,
                "The fully qualified name of the class to be 'exec'd");
    private final StringArgument ARG_ARG =
        new StringArgument("arg", Argument.OPTIONAL | Argument.MULTIPLE,
                "Arguments to be passed to the class");

	public ExecCommand() {
	    super("Execute the given class in a new process");
	    registerArguments(ARG_CLASSNAME, ARG_ARG);
	}

	public static void main(String[] args) throws Exception {
		new ExecCommand().execute(args);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err)
		throws Exception {
        String className = ARG_CLASSNAME.getValue();
        String[] args = ARG_ARG.getValues();
	    String[] execArgs = new String[args.length + 1];
	    execArgs[0] = className;
	    System.arraycopy(args, 0, execArgs, 1, args.length);
		Runtime.getRuntime().exec(execArgs);
	}

}
