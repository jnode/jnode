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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * Touch a file
 * 
 * TODO if file exist change modified date
 * 
 * @author Yves Galante (yves.galante@jmob.net)
 * @author Andreas H\u00e4nel
 */
public class TouchCommand implements Command{

    static final FileArgument ARG_TOUCH = new FileArgument("file",
            "the file to touch");

    public static Help.Info HELP_INFO = new Help.Info("touch",
            "touch a file", new Parameter[] { new Parameter(ARG_TOUCH,
                    Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {
    	new TouchCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }
    
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    	ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
        File file = ARG_TOUCH.getFile(cmdLine);
         File parentFile = file.getParentFile();

        if (!file.exists()) {
            if (parentFile!=null && !parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    err.println("Parent dirs can't create");
                }
            }
            if (file.createNewFile()) {
                out.println("File created");
            } else {
                err.println("File can't create");
            }
        } else {
            out.println("File already exist");
        }
    }
}

