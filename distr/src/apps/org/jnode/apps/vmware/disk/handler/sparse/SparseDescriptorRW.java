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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.descriptor.DescriptorRW;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
final class SparseDescriptorRW extends DescriptorRW {
    static final Logger LOG = Logger.getLogger(SparseDescriptorRW.class);

    @Override
    public SparseExtent createMainExtent(Descriptor desc, ExtentDeclaration extentDecl)
        throws IOException, UnsupportedFormatException {
        SparseExtentFactory factory = new SparseExtentFactory();

        RandomAccessFile raf = new RandomAccessFile(extentDecl.getExtentFile(), "rw");
        ByteBuffer bb = IOUtils.getByteBuffer(raf, 1024);

        SparseExtentHeaderRW reader = new SparseExtentHeaderRW();
        SparseExtentHeader header = reader.read(bb);
        SparseFileDescriptor sfd = new SparseFileDescriptor(desc, raf, factory, header);
        return createExtent(sfd, extentDecl);
    }

    @Override
    public SparseExtent createExtent(FileDescriptor fileDescriptor, ExtentDeclaration extentDecl)
        throws IOException, UnsupportedFormatException {
        SparseFileDescriptor sfd = (SparseFileDescriptor) fileDescriptor;
        return new SparseExtentRW().read(sfd.getRandomAccessFile().getChannel(), sfd, extentDecl);
    }
}
