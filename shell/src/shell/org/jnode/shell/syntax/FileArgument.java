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

package org.jnode.shell.syntax;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import org.jnode.annotation.DoPrivileged;
import sun.security.action.GetPropertyAction;

/**
 * This argument class performs completion against the file system namespace.  This
 * Argument class understands the {@link Argument#EXISTING} and {@link Argument#NONEXISTENT}
 * flags when accepting argument values, but not (yet) when completing them.
 *
 * @author crawley@jnode.org
 */
public class FileArgument extends Argument<File> {

    public FileArgument(String label, int flags, String description) {
        super(label, flags, new File[0], description);
    }

    public FileArgument(String label, int flags) {
        this(label, flags, null);
    }

    @Override
    @DoPrivileged
    protected File doAccept(Token token) throws CommandSyntaxException {
        if (token.text.length() > 0) {
            File file = new File(token.text);
            if (isExisting() && !file.exists()) {
                throw new CommandSyntaxException("this file or directory does not exist");
            }
            if (isNonexistent() && file.exists()) {
                throw new CommandSyntaxException("this file or directory already exist");
            }
            return file;
        } else {
            throw new CommandSyntaxException("invalid file name");
        }
    }

    @Override
    public void complete(final CompletionInfo completion, final String partial) {
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
                            completion.addCompletion(name + File.separatorChar, true);
                        } else {
                            completion.addCompletion(name);
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
            completion.addCompletion(partial + File.separatorChar, true);
        }
    }

    @Override
    protected String argumentKind() {
        return "file";
    }
}
