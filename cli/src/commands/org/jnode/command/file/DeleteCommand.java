/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.file;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.command.util.IOUtils;
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
 * @author chris boertien
 * @see <a href="http://www.opengroup.org/onlinepubs/009695399/utilities/rm.html">POSIX "rm" command</a>
 */
public class DeleteCommand extends AbstractCommand {
    
    private static final String help_file = "the files or directories to be deleted";
    private static final String help_recurse = "if set, any directories are deleted recursively";
    private static final String help_force = "ignore non-existant files, never prompt";
    private static final String help_interact = "prompt before every delete";
    private static final String help_verbose = "give information on what is happening";
    private static final String help_onefs = "If recursively deleting, do not recurse into directories that reside" +
                                             "on a different file system";                               
    private static final String help_super = "Delete files or directories";
    private static final String str_not_exist = "File or Directory does not exist";
    private static final String str_is_dir = "Is a directory";
    private static final String str_is_dot = "Directory is '.' or '..'";
    private static final String str_is_mount = "Directory is a mount point";
    private static final String str_not_empty = "Directory is not empty";
    private static final String fmt_skip = "Skipping '%s' %s%n";
    private static final String fmt_removed_file = "Removed '%s'";
    private static final String fmt_removed_dir = "Removed directory '%s'";
    private static final String fmt_not_removed = "'%s' was not removed";
    private static final String fmt_ask_remove_file = "Remove regular file '%s'? [yes/no] ";
    private static final String fmt_ask_descend = "Descend into directory '%s'? [yes/no]";
    private static final String fmt_ask_remove_dir = "Remove directory '%s'? [yes/no]";

    private final FileArgument argPaths;
    private final FlagArgument flagRecurse;
    private final FlagArgument flagForce;
    private final FlagArgument flagInteract;
    private final FlagArgument flagVerbose;
    private final FlagArgument flagOneFS;
    
    private FileSystemService fss;
    private PrintWriter err;
    private PrintWriter out;
    private Reader in;
    private boolean recursive;
    private boolean force;
    private boolean interactive;
    private boolean verbose;
    private boolean onefs;

    public DeleteCommand() {
        super(help_super);
        argPaths     = new FileArgument("paths", Argument.MANDATORY | Argument.MULTIPLE | Argument.EXISTING, help_file);
        flagRecurse  = new FlagArgument("recursive", Argument.OPTIONAL, help_recurse);
        flagForce    = new FlagArgument("force", Argument.OPTIONAL, help_force);
        flagInteract = new FlagArgument("interactive", Argument.OPTIONAL, help_interact);
        flagVerbose  = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
        flagOneFS    = new FlagArgument("onefs", 0, help_onefs);
        registerArguments(argPaths, flagRecurse, flagForce, flagInteract, flagVerbose, flagOneFS);
    }

    public static void main(String[] args) throws Exception {
        new DeleteCommand().execute(args);
    }

    public void execute() throws NameNotFoundException {
        // Lookup the Filesystem service
        fss = InitialNaming.lookup(FileSystemService.NAME);
        
        recursive    = flagRecurse.isSet();
        force        = flagForce.isSet();
        interactive  = flagInteract.isSet();
        verbose      = flagVerbose.isSet();
        onefs        = flagOneFS.isSet();
        File[] paths = argPaths.getValues();
        
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
        // We have to be careful about how we handle race conditions, especially
        // considering the depth-first nature of recursive file deletion. If this
        // method gets called on a file, and the file does not exist, then we assume
        // some other process has beat us to it. The command line only allows existing
        // files to be input.
        
        boolean deleteOk;
        boolean prompt;
        
        if (!file.exists()) {
            // someone beat us to the delete() call, return gracefully.
            return skip(str_not_exist, file) || true;
        }
        
        // A note about 'interactive' mode. It is _not_ an error
        // if stdin is not a tty. It is possible to send responses
        // via a pipe. This is why we only check for a tty stdin
        // if interactive was not set.
        prompt = (!force && (interactive || (!isWriteable(file) && getInput().isTTY())));
        
        if (file.isDirectory()) {
            // This is written to match the POSIX behavior in rm Description Section 2
            if (!recursive) {
                return skip(str_is_dir, file);
            }
            if (file.getName().equals("..") || file.getName().equals(".")) {
                return skip(str_is_dot, file);
            }
            if (prompt) {
                if (!prompt(String.format(fmt_ask_descend, file))) {
                    return skip("", file);
                }
            }
            // According to the POSIX spec, there is no provision for recursive deletion
            // that spans into another file system. The GNU rm utility provides the ability
            // to stop recursive deleting from entering another file system. If that flag
            // is not set, then we will continue to delete files within that file system
            // but the deleting of the directory that is the mount will probably fail because
            // it is in use.
            if (!checkMount(file)) {
                return skip(str_is_mount, file);
            }
            deleteOk = true;
            for (File f : file.listFiles()) {
                String name = f.getName();
                if (!name.equals(".") && !name.equals("..")) {
                    deleteOk &= deleteFile(f);
                }
            }
            if (!deleteOk) {
                return skip(str_not_empty, file);
            }
            if (prompt) {
                if (!prompt(String.format(fmt_ask_remove_dir, file))) {
                    return skip("", file);
                }
            }
            return rmdir(file);
        } else {
            if (prompt) {
                if (!prompt(String.format(fmt_ask_remove_file, file))) {
                    return skip("", file);
                }
            }
            return unlink(file);
        }
    }
    
    private boolean prompt(String s) {
        return IOUtils.promptYesOrNo(in, out, s);
    }
    
    private boolean checkMount(File file) {
        // This is wrong, as the directory might have been given on the
        // command line, which means regardless of the value of onefs, we will continue.
        // The proper way would be to check that this directory resides on the same file
        // system as the directory given on the command line.
        if (onefs) {
            try {
                if (fss.isMount(file.getCanonicalPath())) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isWriteable(File file) {
        try {
            return file.canWrite();
        } catch (SecurityException e) {
            return false;
        }
    }
    
    private boolean rmdir(File file) {
        if (!file.delete()) {
            return error(fmt_not_removed, file);
        }
        if (verbose) {
            out.format(fmt_removed_dir, file);
        }
        return true;
    }
    
    private boolean unlink(File file) {
        if (!file.delete()) {
            return error(fmt_not_removed, file);
        }
        if (verbose) {
            out.format(fmt_removed_file, file);
        }
        return true;
    }
    
    private boolean skip(String msg, File file) {
        if (!force) {
            err.format(fmt_skip, file, msg);
        }
        return false;
    }
    
    private boolean error(String msg, Object... args) {
        if (verbose) {
            err.format(msg, args);
        }
        return false;
    }
}
