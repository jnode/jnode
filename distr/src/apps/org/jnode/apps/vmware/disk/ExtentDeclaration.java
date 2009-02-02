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
 
package org.jnode.apps.vmware.disk;

import java.io.File;
import org.jnode.apps.vmware.disk.extent.Access;
import org.jnode.apps.vmware.disk.extent.ExtentType;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class ExtentDeclaration {
    private final Access access;
    private final long sizeInSectors;
    private final ExtentType extentType;
    private final String fileName;
    private final File extentFile;
    private final long offset;
    private final boolean isMainExtent;

    public ExtentDeclaration(Access access, long sizeInSectors, ExtentType extentType,
            String fileName, File extentFile, long offset, boolean isMainExtent) {
        this.access = access;
        this.sizeInSectors = sizeInSectors;
        this.extentType = extentType;
        this.fileName = fileName;
        this.extentFile = extentFile;
        this.offset = offset;
        this.isMainExtent = isMainExtent;
    }

    public Access getAccess() {
        return access;
    }

    public long getSizeInSectors() {
        return sizeInSectors;
    }

    public ExtentType getExtentType() {
        return extentType;
    }

    public String getFileName() {
        return fileName;
    }

    public File getExtentFile() {
        return extentFile;
    }

    public boolean isMainExtent() {
        return isMainExtent;
    }

    public long getOffset() {
        return offset;
    }

}
