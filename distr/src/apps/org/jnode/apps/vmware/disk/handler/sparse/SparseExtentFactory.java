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
 
package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.descriptor.DiskDatabase;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtentFactory extends ExtentFactory {
    private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);

    protected SparseFileDescriptor createFileDescriptor(File file, RandomAccessFile raf)
        throws IOException, UnsupportedFormatException {
        ByteBuffer bb = IOUtils.getByteBuffer(raf, 1024);

        SparseExtentHeaderRW reader = new SparseExtentHeaderRW();
        SparseExtentHeader header = reader.read(bb);
        Descriptor embeddedDescriptor;
        if (header.getDescriptorOffset() != 0) {
            embeddedDescriptor =
                    getDescriptorRW().read(file, (int) header.getDescriptorOffset(),
                            (int) header.getDescriptorSize());

            DiskDatabase ddb = embeddedDescriptor.getDiskDatabase();
            long nbSectors = ddb.getCylinders() * ddb.getHeads() * ddb.getSectors();
            LOG.debug("createFileDescriptor: capacity=" + header.getCapacity() + " nbSectors=" +
                    nbSectors);
        } else {
            LOG.debug("embeddedDescriptor = null");
            embeddedDescriptor = null;
        }
        return new SparseFileDescriptor(embeddedDescriptor, raf, this, header);
    }

    public SparseIOHandler createIOHandler(FileDescriptor fileDescriptor) throws IOException {
        SparseFileDescriptor sfd = (SparseFileDescriptor) fileDescriptor;
        SparseIOHandler handler = null;

        Descriptor desc = sfd.getDescriptor();
        handler = new SparseIOHandler(desc);

        return handler;
    }

    @Override
    protected SparseDescriptorRW getDescriptorRW() {
        return new SparseDescriptorRW();
    }
}
