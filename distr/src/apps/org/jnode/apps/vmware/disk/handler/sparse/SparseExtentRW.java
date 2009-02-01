/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtentRW {
    private static final Logger LOG = Logger.getLogger(SparseExtentRW.class);

    private static final AllocationTableRW ALLOC_TABLE_RW = new AllocationTableRW();
    private static final SparseExtentHeaderRW SPARSE_EXT_HEADER_RW = new SparseExtentHeaderRW();

    public void write(FileChannel channel, SparseExtent extent) throws IOException {
        channel.position(0L);
        SPARSE_EXT_HEADER_RW.write(channel, extent.getHeader());

        IOUtils.positionSector(channel, extent.getHeader().getRgdOffset());
        LOG.debug("write: position(redundantAllocTable)=" + channel.position());
        ALLOC_TABLE_RW.write(channel, extent.getRedundantAllocationTable());

        IOUtils.positionSector(channel, extent.getHeader().getGdOffset());
        LOG.debug("write: position(allocTable)=" + channel.position());
        ALLOC_TABLE_RW.write(channel, extent.getAllocationTable());

        LOG.debug("write: position=" + channel.position());
    }

    public SparseExtent read(FileChannel channel, SparseFileDescriptor fileDescriptor,
            ExtentDeclaration extentDecl) throws IOException {
        LOG.debug("fileDescriptor=" + fileDescriptor);
        Descriptor descriptor = fileDescriptor.getDescriptor();

        RandomAccessFile raf = fileDescriptor.getRandomAccessFile();
        SparseExtentHeader header = fileDescriptor.getHeader();

        IOUtils.positionSector(raf.getChannel(), header.getRgdOffset());
        LOG.debug("read: position(redundantAllocTable)=" + channel.position());
        AllocationTable redundantAllocationTable = ALLOC_TABLE_RW.read(raf, header);

        IOUtils.positionSector(raf.getChannel(), header.getGdOffset());
        LOG.debug("read: position(allocTable)=" + channel.position());
        AllocationTable allocationTable = ALLOC_TABLE_RW.read(raf, header);

        return new SparseExtent(descriptor, extentDecl, header, redundantAllocationTable,
                allocationTable);
    }
}
