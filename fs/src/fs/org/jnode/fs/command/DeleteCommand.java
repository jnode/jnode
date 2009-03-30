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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

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
    
    private static final String help_file = "the files or directories to be deleted";
    private static final String help_recurse = "if set, any directories are deleted recursively";
    private static final String help_force = "ignore non-existant files, never prompt";
    private static final String help_interact = "prompt before every delete";
    private static final String help_verbose = "give information on what is happening";

    private final FileArgument ArgPaths 
        = new FileArgument("paths", Argument.MANDATORY | Argument.MULTIPLE | Argument.EXISTING, help_file);
    private final FlagArgument FlagRecurse = new FlagArgument("recursive", Argument.OPTIONAL, help_recurse);
    private final FlagArgument FlagForce = new FlagArgument("force", Argument.OPTIONAL, help_force);
    private final FlagArgument FlagInteract = new FlagArgument("interactive", Argument.OPTIONAL, help_interact);
    private final FlagArgument FlagVerbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
    
    private FileSystemService fss;
    private boolean recursive;
    private boolean force;
    private boolean interactive;
    private boolean verbose;
    private PrintWriter err;
    private PrintWriter out;
    private Reader in;

    public DeleteCommand() {
        super("delete files or directories");
        registerArguments(ArgPaths, FlagRecurse, FlagForce, FlagInteract, FlagVerbose);
    }

    public static void main(String[] args) throws Exception {
        new DeleteCommand().execute(args);
    }

    public void execute() throws NameNotFoundException {
        // Lookup the Filesystem service
        fss = InitialNaming.lookup(FileSystemService.NAME);
        
        recursive    = FlagRecurse.isSet();
        force        = FlagForce.isSet();
        interactive  = FlagInteract.isSet();
        verbose      = FlagVerbose.isSet();
        File[] paths = ArgPaths.getValues();
        
        err = getError().getPrintWriter();
        out = getOutput().getPrintWriter();
        in = getInput().getReader();
        
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
            if (!force) {
                err.println(file + " does not exist");
            }
            return false;
        }
        if (file.isDirectory() && !recursive) {
            err.println("cannot remove " + file + ": Is a directory");
            return false;
        }
        if (file.isFile() && interactive && !prompt_yn("remove regular file " + file.getAbsolutePath() + "?")) {
            return false;
        }
        
        boolean deleteOk = true;

        // FIXME the following doesn't handle mounted filesystems correctly (I think).
        // Recursive delete should not recurse >>into<< a mounted filesystem, but should
        // give an error message and then refuse to delete the parent directory because
        // it cannot be emptied.
        if (file.isDirectory() && !fss.isMount(file.getAbsolutePath())) {
            if (interactive && !prompt_yn("descend into directory " + file.getAbsolutePath() + "?")) {
                return false;
            }
            for (File f : file.listFiles()) {
                final String name = f.getName();

                if (!name.equals(".") && !name.equals("..")) {
                    deleteOk &= deleteFile(f);
                }
            }
            if (deleteOk && interactive && !prompt_yn("remove directory " + file.getAbsolutePath() + "?")) {
                return false;
            }
        }
        
        if (deleteOk) {
            // FIXME ... this is going to attempt to delete "directories" that are 
            // mounted filesystems.  Is this right?  What will it do?
            // FIXME ... this does not report the reason that the delete failed.
            // How should we do that?
            if (verbose) {
                if (file.isFile()) out.println("removed " + file.getAbsolutePath());
                if (file.isDirectory()) out.println("removed directory " + file.getAbsolutePath());
            }
            deleteOk = file.delete();
            if (!deleteOk) {
                err.println(file + " was not deleted");
            }
        }
        return deleteOk;
    }
    
    private boolean prompt_yn(String s) {
        int choice;
        for (;;) {
            out.print(s + "  [y/n]");
            try {
                choice = in.read();
            } catch (IOException _) {
                choice = 0;
            }
            out.println();
            if (choice == 'y' || choice == 'n') break;
        }
        
        return choice == 'y';
    }
}
