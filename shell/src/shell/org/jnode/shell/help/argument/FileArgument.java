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

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author qades
 */
public class FileArgument extends Argument {

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

        if (values == null) return new File[0];

        File[] files = new File[values.length];
        for(int i = 0; i < values.length; i ++)
            files[i] = new File(values[i]);

        return files;
    }

    public String complete(String partial) {
        // Get last full directory
        final int idx = partial.lastIndexOf(File.separatorChar);
        final String dir;
        if (idx >= 0) {
            dir = partial.substring(0, idx);
        } else {
            dir = "";
        }

        // Find the
        final File f = new File(dir);
        final String[] names = (String[]) AccessController
                .doPrivileged(new PrivilegedAction() {

                    public Object run() {
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
            final String prefix = (dir.length() == 0) ? "" : dir + File.separatorChar;
            for (String n : names) {
                final String name = prefix + n;
                if (name.startsWith(partial)) {
                    list.add(name);
                }
            }
            return complete(partial, list);
        }
    }
}
