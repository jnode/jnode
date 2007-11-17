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
 
package org.jnode.shell.help.argument;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jnode.shell.PathnamePattern;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author qades
 */
public class FileArgument extends Argument {
    
    private static final int PATTERN_FLAGS = 0;  /* just '*' and '?' */

    public FileArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public FileArgument(String name, String description) {
        super(name, description, SINGLE);
    }

    // here goes the command line completion

    public File getFile(ParsedArguments args) {
        String value = getValue(args);
        if (value == null) return null;
        return new File(value);
    }

    public File[] getFiles(ParsedArguments args) {
        String[] values = getValues(args);
        if (values == null) {
            return new File[0];
        }

        File cwd = new File(".");
        HashSet<File> files = new HashSet<File>();
        for (String val : values) {
            if (PathnamePattern.isPattern(val, PATTERN_FLAGS)) {
                List<String> matches = PathnamePattern.compile(val, PATTERN_FLAGS).expand(cwd);
                if (matches.isEmpty()) {
                    // A pathname pattern that produces no matches needs to be treated as
                    // literal pathname.
                    files.add(new File(val));
                }
                else {
                    for (String match : matches) {
                        files.add(new File(match));
                    }
                }
            }
            else {
                files.add(new File(val));
            }
        }

        return files.toArray(new File[files.size()]);
    }

    public String complete(String partial) {
        // Get last full directory
        final int idx = partial.lastIndexOf(File.separatorChar);
        final String dir;
        if(idx == 0){
            dir = String.valueOf(File.separatorChar);
        } else if (idx > 0) {
            dir = partial.substring(0, idx);
        } else {
            dir = "";
        }

        // Find the
        final File f = new File(dir);
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
        if (names == null) {
            return partial;
        } else if (names.length == 0) {
            return partial;
        } else {
            final ArrayList<String> list = new ArrayList<String>(names.length);
            final String prefix = (dir.length() == 0) ? "" :
                    dir.equals("/") ? "/" : dir + File.separatorChar;
            for (String n : names) {
                final String name = prefix + n;
                if (name.startsWith(partial)) {
                    list.add(name);
                }
            }
            String completed = complete(partial, list);
            if (completed.endsWith(" ")) {
            	String path = completed.substring(0, completed.length() - 1);
            	if (new File(path).isDirectory()) {
            		completed = path + File.separatorChar;
            	}
            }
            return completed;
        }
    }
}
