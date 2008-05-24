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

import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

import javax.naming.NameNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Delete a file or a empty directory
 *
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 * @author Levente S\u00e1ntha
 * @author Martin Husted Hartvig (hagar at jnode.org)
 */
public class DeleteCommand extends AbstractCommand {

    private final FileArgument ARG_DIR;
    private final FlagArgument ARG_OPTION;


    public DeleteCommand() {
        super("delete files or directories");

        ARG_DIR = new FileArgument(
                "file/dir", Argument.MANDATORY, "the file or directory to be deleted");
        ARG_OPTION = new FlagArgument(
                "recursive", Argument.OPTIONAL, "if set, any directories are deleted recursively");
        
        registerArguments(ARG_DIR, ARG_OPTION);
    }

    public static void main(String[] args) throws Exception {
        new DeleteCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {

        boolean recursive = ARG_OPTION.isSet();

        File[] file_arr = ARG_DIR.getValues();

        boolean ok = true;
        for (File file : file_arr) {
            boolean tmp = deleteFile(file, err, recursive);
            ok &= tmp;
        }

        if (!ok) {
            exit(1);
        }
    }

    private boolean deleteFile(File file, PrintStream err, boolean recursive) throws NameNotFoundException {
        boolean deleteOk = true;
        try {
            if (!file.exists()) {
                err.println(file + " does not exist");
                return false;
            }

            // Lookup the Filesystem service
            final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);

            if (file.isDirectory() && !fss.isMount(file.getAbsolutePath())) {
                final File[] subFiles = file.listFiles();

                for (File f : subFiles) {
                    final String name = f.getName();

                    if (!name.equals(".") && !name.equals("..")) {
                        if (!recursive) {
                            err.println("Directory is not empty " + file);
                            deleteOk = false;
                            break;
                        }
                        else {
                            deleteFile(f, err, recursive);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to check file properties");
            e.printStackTrace();
            System.err.println("Trying to delete it directly");
        }

        try {
            if (deleteOk) {
                deleteOk = file.delete();
                if (!deleteOk) {
                    err.println(file + " was not deleted");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return deleteOk;
    }
}
