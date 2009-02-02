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
 
package org.jnode.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains information needed for the assembly compilation
 * of the nano kernel sources.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class AsmSourceInfo {

    /**
     * Include directories
     */
    private final List<File> includeDirs = new ArrayList<File>();

    /**
     * Main source file
     */
    private File sourceFile;

    /**
     * Return a list of all configured include directories.
     *
     * @return
     */
    public final List<File> includeDirs() {
        return Collections.unmodifiableList(includeDirs);
    }

    /**
     * Gets the main source file.
     *
     * @return Returns the sourceFile.
     */
    public final File getSrcFile() {
        return sourceFile;
    }

    /**
     * Sets the main source file.
     *
     * @param sourceFile The sourceFile to set.
     */
    public final void setSrcFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Create an includeDir sub element.
     *
     * @return
     */
    public IncludeDir createIncludeDir() {
        return new IncludeDir();
    }

    public final class IncludeDir {
        public void setDir(File dir) {
            includeDirs.add(dir);
        }
    }
}
