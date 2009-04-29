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
 
package org.jnode.command.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * File copy utility.  This utility copies one file to another file, or multiple files or directories
 * into an existing directory.  Files are copied byte-wise (not character-wise).  Recursive directory 
 * copy is supported.
 * 
 * @author crawley@jnode.org
 */
public class CpCommand extends AbstractCommand {

    private static final String help_source = "source files or directories";
    private static final String help_target = "target file or directory";
    private static final String help_force = "if set, force overwrite of existing files";
    private static final String help_interactive = "if set, ask before overwriting existing files";
    private static final String help_update = "if set, overwrite existing files older than their source";
    private static final String help_recurse = "if set, recursively copy source directories";
    private static final String help_verbose = "if set, output a line for each file copied";
    private static final String help_super = "Copy files and directories";
    private static final String err_no_source = "No source files or directories supplied";
    private static final String err_no_write = "Target directory is not writable";
    private static final String err_multi_dir = "Multi-file copy requires the target to be a directory";
    private static final String err_copy_dir_file = "Cannot copy a directory to a file";
    private static final String err_copy_dev = "Cannot copy to a device";
    private static final String fmt_verbose_copy = "File copied: %d, directories created: %d%n";
    private static final String err_mutex_flags = "The force, interactive and update flags are mutually exclusive";
    private static final String fmt_no_write = "directory '%s' is not writeable";
    private static final String fmt_dir_create = "Creating directory '%s'%n";
    private static final String fmt_dir_replace = "Replacing file '%s' with a directory%n";
    private static final String fmt_dir_skip = "not overwriting '%s' with a directory";
    private static final String fmt_is_dir = "'%s' is a directory";
    private static final String fmt_copy_file = "Copying file '%s' as '%s'%n";
    private static final String fmt_src_noexist = "'%s' does not exist";
    private static final String fmt_src_noread = "'%s' cannot be read";
    private static final String fmt_src_device = "'%s' is a device";
    private static final String fmt_copy_dir_self = "Cannot copy directory '%s' into itself";
    private static final String fmt_copy_file_self = "Cannot copy file '%s' to itself";
    private static final String fmt_copy_sub = "Cannot copy directory '%s' into a subdirectory ('%s')";
    private static final String fmt_no_copy_dir = "Cannot copy '%s' onto directory '%s'";
    private static final String fmt_no_copy_dev = "Cannot copy '%s' to device '%s'";
    private static final String fmt_exists = "'%s' already exists";
    private static final String fmt_newer = "'%s' is newer than '%s'";
    private static final String fmt_ask_overwrite = "Overwrite '%s' with '%s'? [y/n]%n";
    private static final String err_copy_eof = "EOF - abandoning copying";
    private static final String err_copy_ioex = "IO Error - abandoning copying";
    private static final String str_ask_again = "Answer 'y' or 'n'";
    private static final String fmt_skip = "%s: skipping%n";
    
    static final byte MODE_NORMAL = 0;
    static final byte MODE_INTERACTIVE = 1;
    static final byte MODE_FORCE = 2;
    static final byte MODE_UPDATE = 3;

    private final FileArgument argSource;
    private final FileArgument argTarget;
    private final FlagArgument argForce;
    private final FlagArgument argInteractive;
    private final FlagArgument argUpdate;
    private final FlagArgument argRecursive;
    private final FlagArgument argVerbose;

    private byte mode = MODE_NORMAL;
    private boolean recursive = false;
    private boolean verbose = false;
    private int filesCopied = 0;
    private int directoriesCreated = 0;
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter err;
    private byte[] buffer = new byte[1024 * 8];

    public CpCommand() {
        super(help_super);
        argSource = new FileArgument("source", Argument.MANDATORY | Argument.MULTIPLE | Argument.EXISTING, help_source);
        argTarget      = new FileArgument("target", Argument.MANDATORY, help_target);
        argForce       = new FlagArgument("force", Argument.OPTIONAL, help_force);
        argInteractive = new FlagArgument("interactive", Argument.OPTIONAL, help_interactive);
        argUpdate      = new FlagArgument("update", Argument.OPTIONAL, help_update);
        argRecursive   = new FlagArgument("recursive", Argument.OPTIONAL, help_recurse);
        argVerbose     = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
        registerArguments(argSource, argTarget, argForce, argInteractive, argRecursive, argUpdate, argVerbose);
    }
    
    public static void main(String[] args) throws Exception {
        new CpCommand().execute(args);
    }

    public void execute() throws Exception {
        this.out = getOutput().getPrintWriter();
        this.err = getError().getPrintWriter();
        processFlags();
        if (mode == MODE_INTERACTIVE) {
            this.in = new BufferedReader(getInput().getReader());
        }
        File[] sources = argSource.getValues();
        File target = argTarget.getValue();
        if (sources.length == 0) {
            error(err_no_source);
        }
        if (target.isDirectory()) {
            if (!target.canWrite()) {
                error(err_no_write);
            }
            for (File source : sources) {
                if (checkSafe(source, target)) {
                    copyIntoDirectory(source, target);
                }
            }
        } else if (sources.length > 1) {
            error(err_multi_dir);
        } else {
            File source = sources[0];
            if (source.isDirectory()) {
                error(err_copy_dir_file);
            } else if (target.exists() && !target.isFile()) {
                error(err_copy_dev);
            } else {
                if (checkSafe(source, target)) {
                    copyToFile(source, target);
                }
            }
        }
        if (verbose) {
            out.format(fmt_verbose_copy, filesCopied, directoriesCreated);
        }
    }
    
    private void processFlags() {
        recursive = argRecursive.isSet();
        verbose = argVerbose.isSet();
        // The mode flags are mutually exclusive ...
        if (argForce.isSet()) {
            mode = MODE_FORCE;
        }
        if (argInteractive.isSet()) {
            if (mode != MODE_NORMAL) {
                error(err_mutex_flags);
            }
            mode = MODE_INTERACTIVE;
        }
        if (argUpdate.isSet()) {
            if (mode != MODE_NORMAL) {
                error(err_mutex_flags);
            }
            mode = MODE_UPDATE;
        }
    }
    
    /**
     * Copy a file or directory into a supplied target directory.
     * 
     * @param source the name of the object to be copied
     * @param targetDir the destination directory
     * @throws IOException
     */
    private void copyIntoDirectory(File source, File targetDir) throws IOException {
        if (!targetDir.canWrite()) {
            skip(String.format(fmt_no_write, targetDir));
        } else if (source.isDirectory()) {
            if (recursive) {
                File newDir = new File(targetDir, source.getName());
                if (!newDir.exists()) {
                    if (verbose) {
                        out.format(fmt_dir_create, newDir);
                    }
                    newDir.mkdir();
                    directoriesCreated++;
                } else if (!newDir.isDirectory()) {
                    if (mode == MODE_FORCE) {
                        if (verbose) {
                            out.format(fmt_dir_replace, newDir);
                        }
                        newDir.delete();
                        newDir.mkdir();
                        directoriesCreated++;
                    } else {
                        skip(String.format(fmt_dir_skip, newDir));
                        return;
                    }
                }
                String[] contents = source.list();
                for (String name : contents) {
                    if (name.equals(".") || name.equals("..")) {
                        continue;
                    }
                    copyIntoDirectory(new File(source, name), newDir);
                }
            } else {
                skip(String.format(fmt_is_dir, source));
            }
        } else {
            File newFile = new File(targetDir, source.getName());
            copyToFile(source, newFile);
        }
    }
    
    /**
     * Copy a file to (as) a file
     * 
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    private void copyToFile(File sourceFile, File targetFile) throws IOException {
        if (!checkSafe(sourceFile, targetFile) || 
                !checkSource(sourceFile) || 
                !checkTarget(targetFile, sourceFile)) {
            return;
        }
        if (verbose) {
            out.format(fmt_copy_file, sourceFile, targetFile);
        }
        
        InputStream sin = null;
        OutputStream tout = null;
        try {
            sin = new FileInputStream(sourceFile);
            tout = new FileOutputStream(targetFile);
            while (true) {
                int nosBytesRead = sin.read(buffer);
                if (nosBytesRead <= 0) {
                    break;
                }
                tout.write(buffer, 0, nosBytesRead);
            }
        } finally {
            if (sin != null) {
                try {
                    sin.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (tout != null) {
                try {
                    tout.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        filesCopied++;
    }

    /**
     * Check that a source object exists, is readable and is either 
     * a file or a directory.
     * 
     * @param source
     * @return
     */
    private boolean checkSource(File source) {
        if (!source.exists()) {
            return skip(String.format(fmt_src_noexist, source));
        } else if (!source.canRead()) {
            return skip(String.format(fmt_src_noread, source));
        } else if (!(source.isFile() || source.isDirectory())) {
            return vskip(String.format(fmt_src_device, source));
        } else {
            return true;
        }
    }

    /**
     * Check that a copy is going to be safe.  Unsafe things are copying a
     * file to itself and copying a directory into itself or a subdirectory.
     * 
     * @param source
     * @param target
     * @return
     * @throws IOException
     */
    private boolean checkSafe(File source, File target) throws IOException {
        // These checks must be done with canonical paths.  But fortunately they
        // don't need to be repeated for every file/directory in a recursive copy.
        String sourcePath = source.getCanonicalPath();
        String targetPath = target.getCanonicalPath();
        if (target.isDirectory()) {
            if (recursive && source.isDirectory()) {
                if (sourcePath.equals(targetPath)) {
                    return skip(String.format(fmt_copy_dir_self, source));
                }
                if (!sourcePath.endsWith(File.separator)) {
                    sourcePath = sourcePath + File.separatorChar;
                }
                if (targetPath.startsWith(sourcePath)) {
                    return skip(String.format(fmt_copy_sub, source, target));
                }
            }
        } else {
            if (sourcePath.equals(targetPath)) {
                return skip(String.format(fmt_copy_file_self, source));
            }
        }
        return true;
    }

    /**
     * Check that the target can be written / overwritten.  In interactive mode,
     * the user is asked about clobbering existing files.  In update mode, they
     * are overwritten if the source is newer than the target.  In force mode, they
     * are clobbered without asking.  In normal mode, existing target files are
     * skipped.
     * 
     * @param target
     * @param source
     * @return
     */
    private boolean checkTarget(File target, File source) {
        if (!target.exists()) {
            return true;
        }
        if (target.isDirectory() && !source.isDirectory()) {
            return skip(String.format(fmt_no_copy_dir, source, target));
        }
        if (!target.isFile()) {
            return vskip(String.format(fmt_no_copy_dev, source, target));
        }
        switch (mode) {
            case MODE_NORMAL:
                return vskip(String.format(fmt_exists, target));
            case MODE_FORCE:
                return true;
            case MODE_UPDATE:
                return (source.lastModified() > target.lastModified() ||
                        vskip(String.format(fmt_newer, target, source)));
            case MODE_INTERACTIVE:
                out.format(fmt_ask_overwrite, target, source);
                while (true) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            error(err_copy_eof);
                        }
                        if (line.length() > 0) {
                            if (line.charAt(0) == 'y' || line.charAt(0) == 'Y') {
                                return true;
                            } else if (line.charAt(0) == 'n' || line.charAt(0) == 'N') {
                                return vskip("'" + target + "'");
                            }
                        }
                        out.print(str_ask_again);
                    } catch (IOException ex) {
                        error(err_copy_ioex);
                    }
                }
        }
        return false;
    }

    private void error(String msg) {
        err.println(msg);
        exit(1);
    }
    
    private boolean skip(String msg) {
        err.format(fmt_skip, msg);
        return false;
    }
    
    private boolean vskip(String msg) {
        if (verbose) {
            err.format(fmt_skip, msg);
        }
        return false;
    }
}
