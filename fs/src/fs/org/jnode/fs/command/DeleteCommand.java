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

import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

import javax.naming.NameNotFoundException;

/**
 * Delete a file or a empty directory
 * 
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 * @author Levente S\u00e1ntha
 */
public class DeleteCommand implements Command {

    static final FileArgument ARG_DIR = new FileArgument("file/dir",
            "delete the file or directory", true);

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
        File[] file_arr = ARG_DIR.getFiles(cmdLine);
        for(File file : file_arr)
            deleteFile(file, err);
    }

    private void deleteFile(File file, PrintStream err) throws NameNotFoundException {
        boolean deleteOk = false;

        if (!file.exists()) {
            err.println(file + " does not exist");
        }

        // Lookup the Filesystem service
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);

        // for this time, delete only empty directory (wait implementation of -r
        // option)
        if (file.isDirectory() && !fss.isMount(file.getAbsolutePath())) {
            final File[] subFiles = file.listFiles();
            for (File f : subFiles) {
                final String name = f.getName();
                if (!name.equals(".") && !name.equals("..")) {
                    err.println("Directory is not empty " + file);
                    break;
                }
            }
        }

        deleteOk = file.delete();

        if (!deleteOk) {
            err.println(file + " was not deleted");
        }
    }
}
