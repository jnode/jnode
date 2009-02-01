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
 
package org.jnode.ant.taskdefs.classpath;

import java.io.File;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PackageDirectory implements Comparable {

    private final File baseDir;
    private final String packageFileName;

    public PackageDirectory(File baseDir, String directoryName) {
        this.baseDir = baseDir;
        this.packageFileName = directoryName;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        return packageFileName.compareTo(((PackageDirectory) obj).packageFileName);
    }

    public File getPackageDirectory() {
        return new File(baseDir, packageFileName);
    }

    public String getDirectoryName() {
        return packageFileName;
    }

    public String getPackageName() {
        return packageFileName.replace(File.separatorChar, '.');
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof PackageDirectory) {
            return ((PackageDirectory) obj).packageFileName.equals(packageFileName);
        } else {
            return packageFileName.equals(obj);
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return packageFileName.hashCode();
    }

    /**
     * @return Returns the baseDir.
     */
    public final File getBaseDir() {
        return this.baseDir;
    }
}
