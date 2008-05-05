/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

public class FileArgument extends Argument<File> {

    public FileArgument(String label, int flags, String description) {
        super(label, flags, new File[0], description);
    }

    public FileArgument(String label, int flags) {
        this(label, flags, null);
    }

    @Override
    protected File doAccept(Token token) throws CommandSyntaxException {
        // FIXME ... do proper filename checks ...
        if (token.token.length() > 0) {
            return new File(token.token);
        }
        else {
            throw new CommandSyntaxException("invalid file name '" + token.token + "'");
        }
    }
    
    @Override
    public void complete(CompletionInfo completion, String partial) {
     // Get last full directory
        final int idx = partial.lastIndexOf(File.separatorChar);
        final String dir;
        if (idx == 0) {
            dir = String.valueOf(File.separatorChar);
        } else if (idx > 0) {
            dir = partial.substring(0, idx);
        } else {
            dir = "";
        }

        // Get the contents of the directory.  (Note that the call to getProperty()
        // is needed because new File("").exists() returns false.  According to Sun, this
        // behavior is "not a bug".)
        final File f = dir.isEmpty() ? 
                new File(System.getProperty("user.dir")) : new File(dir);
        final String[] names = AccessController
                .doPrivileged(new PrivilegedAction <String[]>() {
                    public String[] run() {
                        if (!f.exists()) {
                            return null;
                        } else {
                            return f.list();
                        }
                    }
                });
        if (names != null && names.length > 0) {
            final String prefix = (dir.length() == 0) ? "" :
                    dir.equals("/") ? "/" : dir + File.separatorChar;
            for (String n : names) {
                String name = prefix + n;
                if (name.startsWith(partial)) {
                    if (new File(f, name).isDirectory()) {
                        name += File.separatorChar;
                        completion.addCompletion(name, true);
                    }
                    else {
                        completion.addCompletion(name);
                    }
                }
            }
        }
    }

    @Override
	public String toString() {
	    return "FileArgument{" + super.toString() + "}";
	}
	
	@Override
    protected String argumentKind() {
        return "file";
    }
}
