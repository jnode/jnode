/*
 * $Id$
 *
 * JNode.org
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

    static final byte MODE_NORMAL = 0;
    static final byte MODE_INTERACTIVE = 1;
    static final byte MODE_FORCE = 2;
    static final byte MODE_UPDATE = 3;

    private final FileArgument ARG_SOURCE = 
        new FileArgument("source", Argument.MANDATORY | Argument.MULTIPLE | Argument.EXISTING, 
                "source files or directories");

    private final FileArgument ARG_TARGET = 
        new FileArgument("target", Argument.MANDATORY, "target file or directory");

    private final FlagArgument FLAG_FORCE = 
        new FlagArgument("force", Argument.OPTIONAL, "if set, force overwrite of existing files");
    
    private final FlagArgument FLAG_INTERACTIVE = 
        new FlagArgument("interactive", Argument.OPTIONAL, "if set, ask before overwriting existing files");
    
    private final FlagArgument FLAG_UPDATE = 
        new FlagArgument("update", Argument.OPTIONAL, "if set, overwrite existing files older than their source");
    
    private final FlagArgument FLAG_RECURSIVE = 
        new FlagArgument("recursive", Argument.OPTIONAL, "if set, recursively copy source directories");
    
    private final FlagArgument FLAG_VERBOSE = 
        new FlagArgument("verbose", Argument.OPTIONAL, "if set, output a line for each file copied");

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
        super("Copy files or directories");
        registerArguments(ARG_SOURCE, ARG_TARGET, FLAG_FORCE, FLAG_INTERACTIVE, FLAG_RECURSIVE,
                FLAG_UPDATE, FLAG_VERBOSE);
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
        File[] sources = ARG_SOURCE.getValues();
        File target = ARG_TARGET.getValue();
        if (sources.length == 0) {
            error("No source files or directories supplied");
        }
        if (target.isDirectory()) {
            if (!target.canWrite()) {
                error("Target directory is not writable");
            }
            for (File source : sources) {
                if (checkSafe(source, target)) {
                    copyIntoDirectory(source, target);
                }
            }
        } else if (sources.length > 1) {
            error("Multi-file copy requires the target to be a directory");
        } else {
            File source = sources[0];
            if (source.isDirectory()) {
                error("Cannot copy a directory to a file");
            } else if (target.exists() && !target.isFile()) {
                error("Cannot copy to a device");
            } else {
                if (checkSafe(source, target)) {
                    copyToFile(source, target);
                }
            }
        }
        if (verbose) {
            out.println("Files copied: " + filesCopied + ", directories created: " + directoriesCreated);
        }
    }

    private void processFlags() {
        recursive = FLAG_RECURSIVE.isSet();
        verbose = FLAG_VERBOSE.isSet();
        // The mode flags are mutually exclusive ...
        if (FLAG_FORCE.isSet()) {
            mode = MODE_FORCE;
        }
        if (FLAG_INTERACTIVE.isSet()) {
            if (mode != MODE_NORMAL) {
                error("The 'force', 'interactive' and 'update' flags are mutually exclusive");
            }
            mode = MODE_INTERACTIVE;
        }
        if (FLAG_UPDATE.isSet()) {
            if (mode != MODE_NORMAL) {
                error("The 'force', 'interactive' and 'update' flags are mutually exclusive");
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
            skip("directory '" + targetDir + "' is not writable");
        } else if (source.isDirectory()) {
            if (recursive) {
                File newDir = new File(targetDir, source.getName());
                if (!newDir.exists()) {
                    if (verbose) {
                        out.println("Creating directory '" + newDir + "'");
                    }
                    newDir.mkdir();
                    directoriesCreated++;
                } else if (!newDir.isDirectory()) {
                    if (mode == MODE_FORCE) {
                        if (verbose) {
                            out.println("Replacing file '" + newDir + "' with a directory");
                        }
                        newDir.delete();
                        newDir.mkdir();
                        directoriesCreated++;
                    } else {
                        skip("not overwriting '" + newDir + "' with a directory");
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
                skip("'" + source + "' is a directory");
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
            out.println("Copying file '" + sourceFile + "' as '" + targetFile + "'");
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
            return skip("'" + source + "' does not exist");
        } else if (!source.canRead()) {
            return skip("'" + source + "' cannot be read");
        } else if (!(source.isFile() || source.isDirectory())) {
            return vskip("'" + source + "' is a device");
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
                    return skip("Cannot copy directory '" + source + "' into itself");
                }
                if (!sourcePath.endsWith(File.separator)) {
                    sourcePath = sourcePath + File.separatorChar;
                }
                if (targetPath.startsWith(sourcePath)) {
                    return skip("Cannot copy directory '" + source + 
                            "' into a subdirectory ('" + target + "')");
                }
            }
        } else {
            if (sourcePath.equals(targetPath)) {
                return skip("Cannot copy file '" + source + "' to itself");
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
            return skip("Cannot copy '" + source + "' onto directory '" + target + "'");
        }
        if (!target.isFile()) {
            return vskip("Cannot copy '" + source + "' to device '" + target + "'");
        }
        switch (mode) {
            case MODE_NORMAL:
                return vskip("'" + target + "' already exists");
            case MODE_FORCE:
                return true;
            case MODE_UPDATE:
                return (source.lastModified() > target.lastModified() ||
                        vskip("'" + target + "' is newer than '" + source + "'"));
            case MODE_INTERACTIVE:
                out.print("Overwrite '" + target + "' with '" + source + "'? [y/n]");
                while (true) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            error("EOF - abandoning copying");
                        }
                        if (line.length() > 0) {
                            if (line.charAt(0) == 'y' || line.charAt(0) == 'Y') {
                                return true;
                            } else if (line.charAt(0) == 'n' || line.charAt(0) == 'N') {
                                return vskip("'" + target + "'");
                            }
                        }
                        out.print("Answer 'y' or 'n'");
                    } catch (IOException ex) {
                        error("IO Error - abandoning copying");
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
        err.println(msg + ": skipping");
        return false;
    }
    
    private boolean vskip(String msg) {
        if (verbose) {
            err.println(msg + ": skipping");
        }
        return false;
    }
}
