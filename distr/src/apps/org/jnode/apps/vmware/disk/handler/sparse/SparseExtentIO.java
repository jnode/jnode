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
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.ExtentIO;
import org.jnode.apps.vmware.disk.handler.IOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtentIO extends ExtentIO {
    private static final Logger LOG = Logger.getLogger(SparseExtentIO.class);

    public SparseExtentIO(RandomAccessFile raf, Extent extent) {
        super(raf, extent);
    }

    @Override
    public void read(long sector, ByteBuffer dst) throws IOException {
        SparseExtent spe = (SparseExtent) extent;

        int oldLimit = dst.limit();
        dst.limit((int) (dst.position() + IOHandler.SECTOR_SIZE));

        int offset = spe.getOffset(sector, false, raf); // false: don't allocate
        if (offset < 0) {
            // grain not yet allocated
            // this is the vmware disk behavior : we don't allocate new grains
            // on read
            // we put zeros in the buffer
            for (int i = 0; i < IOHandler.SECTOR_SIZE; i++) {
                dst.put((byte) 0);
            }
        } else {
            LOG.debug("read: sector=" + sector + " offset=" + offset + " size=" + dst.remaining());
            channel.position(offset);
            int read = channel.read(dst);
            LOG.debug("read: nbRead=" + read);
        }

        dst.limit(oldLimit);
    }

    @Override
    public void write(long sector, ByteBuffer src) throws IOException {
        SparseExtent spe = (SparseExtent) extent;

        // true: allocate if necessary
        int offset = spe.getOffset(sector, true, raf);

        LOG.debug("write: sector=" + sector + " offset=" + offset + " size=" + src.remaining());
        channel.position(offset);
        channel.write(src);
    }

    @Override
    public void flush() throws IOException {
        SparseExtent spe = (SparseExtent) extent;

        new SparseExtentRW().write(channel, spe);

        super.flush();
    }
}
