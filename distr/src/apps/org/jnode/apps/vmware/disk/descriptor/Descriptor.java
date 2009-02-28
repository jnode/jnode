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
 
package org.jnode.apps.vmware.disk.descriptor;

import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.extent.Extent;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class Descriptor {
    private static final Logger LOG = Logger.getLogger(Descriptor.class);

    private final File mainFile;
    private final Header header;
    private final List<Extent> extents;
    private final DiskDatabase diskDatabase;

    public Descriptor(File mainFile, Header header, List<Extent> extents, DiskDatabase diskDatabase) {
        this.mainFile = mainFile;
        this.header = header;
        this.extents = extents;
        this.diskDatabase = diskDatabase;
    }

    public Header getHeader() {
        return header;
    }

    public List<Extent> getExtents() {
        return extents;
    }

    public DiskDatabase getDiskDatabase() {
        return diskDatabase;
    }

    public File getMainFile() {
        return mainFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Descriptor)) {
            return false;
        }

        Descriptor desc = (Descriptor) obj;

        String file1 = "";
        String file2 = "";
        try {
            file1 = this.mainFile.getCanonicalPath();
            file2 = desc.mainFile.getCanonicalPath();
        } catch (Exception e) {
            LOG.error("can't compare filenames", e);
        }

        return file1.equals(file2) && this.header.equals(desc.header) &&
                this.extents.equals(desc.extents) && this.diskDatabase.equals(desc.diskDatabase);
    }

    @Override
    public String toString() {
        String file1 = "";
        try {
            file1 = this.mainFile.getCanonicalPath();
        } catch (Exception e) {
            LOG.error("can't compare filenames", e);
        }

        return "Descriptor: file=" + file1 + "," + header + ",extents=" + extents + "," +
                diskDatabase;
    }
}
