/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.fs.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * Delete a file or a empty directory
 * 
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 */
public class DeleteCommand implements Command {

    static final FileArgument ARG_DIR = new FileArgument("file/dir",
            "delete the file or directory");

    public static Help.Info HELP_INFO = new Help.Info("del",
            "delete a file or directory", new Parameter[] { new Parameter(
                    ARG_DIR, Parameter.MANDATORY) });

    public static void main(String[] args) throws Exception {
        new DeleteCommand().execute(new CommandLine(args), System.in,
                System.out, System.err);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
        File entry = ARG_DIR.getFile(cmdLine);
        boolean deleteOk = false;

        if (!entry.exists()) {
            err.println(entry + " does not exist");
        }
        
        // Lookup the Filesystem service
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        
        // for this time, delete only empty directory (wait implementation of -r
        // option)
        if (entry.isDirectory() && !fss.isMount(entry.getAbsolutePath())) {
            final File[] subFiles = entry.listFiles();
            for (File f : subFiles) {
                final String name = f.getName();
                if (!name.equals(".") && !name.equals("..")) {
                    err.println("Directory is not empty");
                    return;
                }
            }
        }

        deleteOk = entry.delete();

        if (!deleteOk) {
            err.println(entry + " does not deleted");
        }
    }

}
