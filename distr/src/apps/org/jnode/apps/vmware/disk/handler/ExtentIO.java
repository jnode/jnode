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
 
package org.jnode.apps.vmware.disk.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.extent.Extent;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class ExtentIO {
    private static final Logger LOG = Logger.getLogger(ExtentIO.class);

    protected final RandomAccessFile raf;
    protected final FileChannel channel;
    protected final Extent extent;

    public ExtentIO(RandomAccessFile raf, Extent extent) {
        this.raf = raf;
        this.channel = raf.getChannel();
        this.extent = extent;
    }

    public void read(long sector, ByteBuffer dst) throws IOException {
        int oldLimit = dst.limit();
        dst.limit((int) (dst.position() + IOHandler.SECTOR_SIZE));

        channel.position(IOHandler.SECTOR_SIZE * sector);
        LOG.debug("channel pos before : " + channel.position());
        int read = channel.read(dst);
        LOG.debug("channel pos after : " + channel.position());
        LOG.debug("nb bytes read: " + read);

        dst.limit(oldLimit);
    }

    public void write(long sector, ByteBuffer src) throws IOException {
        channel.position(IOHandler.SECTOR_SIZE * sector);
        channel.write(src);
    }

    public void flush() throws IOException {
        raf.close();
        channel.close();
    }
}
