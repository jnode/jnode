/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.shell.syntax;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import sun.security.action.GetPropertyAction;

/**
 * This argument class performs completion against the file system namespace.  This
 * Argument class understands the {@link Argument#EXISTING} and {@link Argument#NONEXISTENT}
 * flags when accepting argument values.  Neither {@link Argument#EXISTING} or 
 * {@link Argument#NONEXISTENT} currently affect completion.  (You might expect that
 * {@link Argument#NONEXISTENT} would suppress completion, but consider that the user
 * may want to complete the directory path for some file to be created by a command.)
 * <p>
 * FileArgument normally treats pathname components starting with a "-" as invalid pathnames
 * and won't accept them.  (The rationale is that they are probably a misplaced or unknown
 * option names.)  This behavior can be changed using the {@link #ALLOW_DODGY_NAMES} flag.
 * <p>
 * Some commands use "-" to denote (for example) "standard input" instead of a file named
 * "-".  To support this, FileArgument provides a {@link #HYPHEN_IS_SPECIAL} flag which
 * suppresses the {@link Argument#EXISTING} and {@link Argument#NONEXISTENT} flags so that
 * a "-" argument is always accepted.  It is up to the command to deal with the resulting
 * {@code File("-")} instance, which of course should not be opened in the normal way.
 * (Note: this is an experimental feature, and may be replaced with a conceptually cleaner
 * solution in the future.)
 *
 * @author crawley@jnode.org
 */
public class FileArgument extends Argument<File> {
    
    /**
     * This Argument flag tells the FileArgument to accept filenames that,
     * while strictly legal, will cause problems.  At the moment, this means
     * pathnames where one or more component names starts with a '-'.  (Such
     * names may be problematic for some commands, and are probably entered
     * by mistake.)
     */
    public static final int ALLOW_DODGY_NAMES = 0x00010000;
    
    /**
     * This Argument flag tells the FileArgument that the command will
     * interpret {@code File("-")} as meaning something other than a regular 
     * pathname, and that FileArgument should allow "-" as a valid argument
     * or completion, not withstanding the existence of a real file with
     * that name.  This flag cannot be set by a Syntax. 
     */
    public static final int HYPHEN_IS_SPECIAL = 0x01000000;

    public FileArgument(String label, int flags, String description) {
        super(label, flags, new File[0], description);
    }

    public FileArgument(String label, int flags) {
        this(label, flags, null);
    }

    @Override
    protected File doAccept(final Token token, final int flags) throws CommandSyntaxException {
        if (token.text.length() > 0) {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<File>() {
                    @Override
                    public File run() throws Exception {
                        File file = new File(token.text);
                        if ((flags & HYPHEN_IS_SPECIAL) == 0 || !file.getPath().equals("-")) {
                            if (isExisting(flags) && !file.exists()) {
                                throw new CommandSyntaxException("this file or directory does not exist");
                            }
                            if (isNonexistent(flags) && file.exists()) {
                                throw new CommandSyntaxException("this file or directory already exist");
                            }
                            if ((flags & ALLOW_DODGY_NAMES) == 0) {
                                File f = file;
                                do {
                                    // This assumes that option names start with '-'.
                                    if (f.getName().startsWith("-")) {
                                        if (f == file && !file.isAbsolute() && f.getParent() == null) {
                                            // The user most likely meant this to be an option name ...
                                            throw new CommandSyntaxException("unexpected or unknown option");
                                        } else {
                                            throw new CommandSyntaxException(
                                                "file or directory name starts with a '-'");
                                        }
                                    }
                                    f = f.getParentFile();
                                } while (f != null);
                            }
                        }
                        return file;
                    }
                });
            } catch (PrivilegedActionException x) {
                Exception e = x.getException();
                if (e instanceof CommandSyntaxException) {
                    throw (CommandSyntaxException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new CommandSyntaxException("invalid file name");
        }
    }

    @Override
    public void doComplete(final CompletionInfo completions, 
            final String partial, final int flags) {
        // Get last full directory from the partial pathname.
        final int idx = partial.lastIndexOf(File.separatorChar);
        final String dir;
        if (idx == 0) {
            dir = String.valueOf(File.separatorChar);
        } else if (idx > 0) {
            dir = partial.substring(0, idx);
        } else {
            dir = "";
        }

        // Get the contents of that directory.  (Note that the call to getProperty()
        // is needed because new File("").exists() returns false.  According to Sun, this
        // behavior is "not a bug".)
        String user_dir = AccessController.doPrivileged(new GetPropertyAction("user.dir"));
        final File f = dir.isEmpty() ? new File(user_dir) : new File(dir);
        final String[] names = AccessController.doPrivileged(
            new PrivilegedAction<String[]>() {
                public String[] run() {
                    if (!f.exists()) {
                        return null;
                    } else {
                        return f.list();
                    }
                }
            });
        if (names == null) {
            // The dir (or user.dir) denotes a non-existent directory.  
            // No completions are possible for this path name.
            return;
        }
        final String prefix = (dir.length() == 0) ? "" : dir.equals("/") ? "/" : dir + File.separatorChar;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                for (String n : names) {
                    String name = prefix + n;
                    if (name.startsWith(partial)) {
                        if (new File(f, n).isDirectory()) {
                            completions.addCompletion(name + File.separatorChar, true);
                        } else {
                            completions.addCompletion(name);
                        }
                    }
                }
                return null;
            }
        });

        // Completion of "." and ".." as the last pathname component have to be dealt with 
        // explicitly.  The 'f.list()' call does not include "." and ".." in the result array.
        int tmp = partial.length() - idx;
        if ((tmp == 3 && partial.endsWith("..")) ||
            (tmp == 2 && partial.endsWith("."))) {
            completions.addCompletion(partial + File.separatorChar, true);
        }
        
        // Add "-" as a possible completion?
        if (partial.length() == 0 && (flags & HYPHEN_IS_SPECIAL) != 0) {
            completions.addCompletion("-");
        }
    }

    @Override
    protected String argumentKind() {
        return "file";
    }

    @Override
    public int nameToFlag(String name) throws IllegalArgumentException {
        if (name.equals("ALLOW_DODGY_NAMES")) {
            return ALLOW_DODGY_NAMES;
        } else if (name.equals("HYPHEN_IS_SPECIAL")) {
            return HYPHEN_IS_SPECIAL;
        } else {
            return super.nameToFlag(name);
        }
    }
}
