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
import java.io.PrintWriter;

import javax.naming.NameNotFoundException;

import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * Delete a file or a empty directory
 *
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 * @author Levente S\u00e1ntha
 * @author Martin Husted Hartvig (hagar at jnode.org)
 * @author crawley@jnode.org
 */
public class DeleteCommand extends AbstractCommand {

    private final FileArgument ARG_PATHS = new FileArgument(
            "paths", Argument.MANDATORY | Argument.MULTIPLE | Argument.EXISTING, 
            "the files or directories to be deleted");
    private final FlagArgument FLAG_RECURSIVE = new FlagArgument(
            "recursive", Argument.OPTIONAL, 
            "if set, any directories are deleted recursively");
    
    private FileSystemService fss;
    private boolean recursive;
    private PrintWriter err;
    

    public DeleteCommand() {
        super("delete files or directories");
        registerArguments(ARG_PATHS, FLAG_RECURSIVE);
    }

    public static void main(String[] args) throws Exception {
        new DeleteCommand().execute(args);
    }

    public void execute() throws NameNotFoundException {
        // Lookup the Filesystem service
        fss = InitialNaming.lookup(FileSystemService.NAME);
        recursive = FLAG_RECURSIVE.isSet();
        File[] paths = ARG_PATHS.getValues();
        this.err = getError().getPrintWriter();
        boolean ok = true;
        for (File file : paths) {
            ok &= deleteFile(file);
        }
        if (!ok) {
            exit(1);
        }
    }

    private boolean deleteFile(File file) {
        if (!file.exists()) {
            err.println(file + " does not exist");
            return false;
        }
        boolean deleteOk = true;

        // FIXME the following doesn't handle mounted filesystems correctly (I think).
        // Recursive delete should not recurse >>into<< a mounted filesystem, but should
        // give an error message and then refuse to delete the parent directory because
        // it cannot be emptied.
        if (file.isDirectory() && !fss.isMount(file.getAbsolutePath())) {
            for (File f : file.listFiles()) {
                final String name = f.getName();

                if (!name.equals(".") && !name.equals("..")) {
                    if (!recursive) {
                        err.println("Directory is not empty " + file);
                        deleteOk = false;
                        break;
                    } else {
                        deleteOk &= deleteFile(f);
                    }
                }
            }
        }

        if (deleteOk) {
            // FIXME ... this is going to attempt to delete "directories" that are 
            // mounted filesystems.  Is this right?  What will it do?
            // FIXME ... this does not report the reason that the delete failed.
            // How should we do that?
            deleteOk = file.delete();
            if (!deleteOk) {
                err.println(file + " was not deleted");
            }
        }
        return deleteOk;
    }
}
