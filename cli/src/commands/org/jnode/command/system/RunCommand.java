/*
 * $Id$
 *
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
 
package org.jnode.command.system;

import java.io.File;
import java.io.PrintWriter;

import javax.naming.NameNotFoundException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Load and execute a command file.
 *
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class RunCommand extends AbstractCommand {
    
    private static final String help_file = "The command script file name";
    private static final String help_args = "Arguments passed to the script";
    private static final String help_super = "Run a script";
    private static final String err_null = "Shell is null";
    
    private final FileArgument argFile;
    private final StringArgument argArgs;
    
    public RunCommand() {
        super(help_super);
        argFile =  new FileArgument("file", Argument.MANDATORY | Argument.EXISTING, help_file);
        argArgs = new StringArgument("args", Argument.OPTIONAL | Argument.MULTIPLE, help_args);
        registerArguments(argFile, argArgs);
    }

    public static void main(String[] args) throws Exception {
        new RunCommand().execute(args);
    }
    
    @Override
    public void execute() throws Exception {
        final PrintWriter err = getError().getPrintWriter();
        final File file = argFile.getValue();

        Shell shell = null;
        try {
            shell = ShellUtils.getShellManager().getCurrentShell();
        } catch (NameNotFoundException e) {
            e.printStackTrace(err);
            exit(2);
        }

        if (shell == null) {
            err.println(err_null);
            exit(2);
        }
        
        String[] args = argArgs.getValues();

        int rc = shell.runCommandFile(file, null, args);
        if (rc != 0) {
            exit(rc);
        }
    }
}
