/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.apps.vmware.disk.extent;

import java.io.File;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class Extent {
    private static final Logger LOG = Logger.getLogger(Extent.class);

    private Descriptor descriptor;

    private final Access access;
    
    /**
     * Number of sectors. A sector is 512 bytes.
     */
    private final long sizeInSectors;
    
    private final ExtentType extentType;
    
    /**
     * Filename. Relative to the location of the descriptor.
     */
    private final String fileName;
    
    private final File file;
    private final long offset;

    /**
     * 
     * @param descriptor
     * @param extentDecl
     */
    public Extent(Descriptor descriptor, ExtentDeclaration extentDecl) {
        this.descriptor = descriptor;
        this.access = extentDecl.getAccess();
        this.sizeInSectors = extentDecl.getSizeInSectors();
        this.extentType = extentDecl.getExtentType();
        this.fileName = extentDecl.getFileName();
        this.file = extentDecl.getExtentFile();
        this.offset = extentDecl.getOffset();

        LOG.debug("created extent for file " + file.getAbsolutePath() + " offset=" + offset +
                " fileSize=" + file.length());
    }

    /**
     * 
     * @return
     */
    public Access getAccess() {
        return access;
    }

    /**
     * 
     * @return
     */
    public long getSizeInSectors() {
        return sizeInSectors;
    }

    /**
     * 
     * @return
     */
    public ExtentType getExtentType() {
        return extentType;
    }

    /**
     * 
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 
     * @return
     */
    public long getOffset() {
        return offset;
    }

    /**
     * 
     * @return
     */
    public final Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * 
     * @param descriptor
     */
    public void setDescriptor(Descriptor descriptor) {
        if (this.descriptor != null) {
            throw new IllegalStateException("descriptor already assigned");
        }

        this.descriptor = descriptor;
    }

    /**
     * 
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * @return 
     */
    @Override
    public String toString() {
        return "Extent[" + fileName + "]";
    }

    /**
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Extent)) {
            return false;
        }

        Extent e = (Extent) obj;

        return this.access.equals(e.access) && (this.sizeInSectors == e.sizeInSectors) &&
                this.extentType.equals(e.extentType) && this.fileName.equals(e.fileName) &&
                this.file.equals(e.file) && (this.offset == e.offset);
    }
}
