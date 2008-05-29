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

package org.jnode.ant.taskdefs.classpath;

import java.io.File;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SourceFile implements Comparable {

    private final File baseDir;
    private final String fileName;
    private final String target;
    private final boolean ignoreMissing;
    private SourceFile next;

    public SourceFile(File baseDir, String fileName, String target, boolean ignoreMissing) {
        this.baseDir = baseDir;
        this.fileName = fileName;
        this.target = target;
        this.ignoreMissing = ignoreMissing;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        return fileName.compareTo(((SourceFile) obj).fileName);
    }

    public File getFile() {
        return new File(baseDir, fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public String getClassName() {
        return fileName.substring(0, fileName.length() - ".java".length()).replace(File.separatorChar, '.');
    }

    public String getReportName() {
        if (isJavaFile()) {
            return getClassName();
        } else {
            return getFileName().replace(File.separatorChar, '.');
        }
    }

    public boolean isJavaFile() {
        return fileName.endsWith(".java");
    }

    public String getPackageName() {
        final String clsName = getClassName();
        final int idx = clsName.lastIndexOf('.');
        if (idx > 0) {
            return clsName.substring(0, idx);
        } else {
            return "";
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof SourceFile) {
            return ((SourceFile) obj).fileName.equals(fileName);
        } else {
            return fileName.equals(obj);
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fileName.hashCode();
    }

    /**
     * @return Returns the baseDir.
     */
    public final File getBaseDir() {
        return this.baseDir;
    }

    /**
     * @return Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @return Returns the ignoreMissing.
     */
    public final boolean isIgnoreMissing() {
        return ignoreMissing;
    }

    public final SourceFile getBestFileForTarget(String target) {
        if (!this.target.equals(target)) {
            if (next != null) {
                return next.getBestFileForTarget(target);
            }
        }
        return this;
    }

    final void append(SourceFile jf) {
        if (this.next == null) {
            this.next = jf;
        } else {
            this.next.append(jf);
        }
    }
}
