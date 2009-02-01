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
 
package org.jnode.jnasm.preprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class FileResolver {
    private final List<File> directoryList;

    public FileResolver(List<File> directoryList) {
        this.directoryList = directoryList;
    }

    public File resolveFile(String fileName) throws FileNotFoundException {
        if (directoryList == null) {
            File file = new File(fileName);
            if (file.exists()) {
                return file;
            } else {
                throw new FileNotFoundException(fileName);
            }
        } else {
            File resolved;
            for (File directory : directoryList) {
                resolved = new File(directory, fileName);
                if (resolved.exists()) {
                    return resolved;
                }
            }
        }
        throw new FileNotFoundException(fileName);
    }
}
