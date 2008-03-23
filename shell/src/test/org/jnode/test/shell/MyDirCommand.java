/*
 * $Id: DirCommand.java 3590 2007-11-17 19:28:05Z lsantha $
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
 
package org.jnode.test.shell;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 */
public class MyDirCommand extends AbstractCommand {
    static final FileArgument ARG_PATH = new FileArgument("path", "the path to list contents of");
    public static Help.Info HELP_INFO =
            new Help.Info(
                    "dir",
                    "List the entries of the given path",
                    new Parameter[]{new Parameter(ARG_PATH, Parameter.OPTIONAL)});

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
//        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);
//        String userDir = System.getProperty("user.dir");
//        File path = ARG_PATH.getFile(cmdLine);
    }
}
