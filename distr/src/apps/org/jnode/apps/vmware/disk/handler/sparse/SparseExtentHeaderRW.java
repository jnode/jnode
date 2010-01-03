/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtentHeaderRW {
    static final Logger LOG = Logger.getLogger(SparseExtentHeaderRW.class);

    private static final byte singleEndLineChar = '\n';
    private static final byte nonEndLineChar = ' ';
    private static final byte doubleEndLineChar1 = '\r';
    private static final byte doubleEndLineChar2 = '\n';

    private static final int PAD_SIZE = 435;
    private static final int MAGIC_NUMBER = 0x564d444b;
    private static final int VERSION = 1;

    private static final byte TRUE = (byte) 1;
    private static final byte FALSE = (byte) 0;

    private static final int numGTEsPerGT = 512;

    /**
     * 
     * @param bb
     * @return
     * @throws IOException
     * @throws UnsupportedFormatException
     */
    public SparseExtentHeader read(ByteBuffer bb) throws IOException, UnsupportedFormatException {
        // TODO optimise the size
        // ByteBuffer bb = IOUtils.getByteBuffer(raf, 1024);

        SparseExtentHeader header = new SparseExtentHeader();

        int magicNum = bb.getInt();
        LOG.debug("magicNum=" + Long.toHexString(magicNum));
        if (magicNum != MAGIC_NUMBER) {
            throw new UnsupportedFormatException("not the magic number");
        }

        int version = bb.getInt();
        if (version != VERSION) {
            throw new IOException("bad version number (found:" + version + ")");
        }

        int flags = bb.getInt();
        header.setValidNewLineDetectionTest((flags & 0x01) == 0x01); // bit 0
        header.setRedundantGrainTableWillBeUsed((flags & 0x02) == 0x02); // bit
                                                                            // 1

        header.setCapacity(bb.getLong());
        header.setGrainSize(bb.getLong());
        header.setDescriptorOffset(bb.getLong());
        header.setDescriptorSize(bb.getLong());

        LOG.debug("read: offset(NumGTEsPerGT)=" + bb.position());
        int nb = bb.getInt();
        if (nb != numGTEsPerGT) {
            throw new IOException("bad number of entries per grain table (found:" + nb + ")");
        }
        header.setNumGTEsPerGT(nb);

        header.setRgdOffset(bb.getLong());

        LOG.debug("read: offset(GdOffset)=" + bb.position());
        header.setGdOffset(bb.getLong());
        header.setOverHead(bb.getLong());
        header.setUncleanShutdown(bb.get() == TRUE);

        LOG.debug("read: offset(singleEndLineChar)=" + bb.position());
        byte b = bb.get();
        if (b != singleEndLineChar) {
            throw new IOException("file corrupted after a FTP (singleEndLineChar=" + b + ")");
        }
        b = bb.get();
        if (b != nonEndLineChar) {
            throw new IOException("file corrupted after a FTP (nonEndLineChar=" + b + ")");
        }
        b = bb.get();
        if (b != doubleEndLineChar1) {
            throw new IOException("file corrupted after a FTP (doubleEndLineChar1=" + b + ")");
        }
        b = bb.get();
        if (b != doubleEndLineChar2) {
            throw new IOException("file corrupted after a FTP (doubleEndLineChar2=" + b + ")");
        }

        if (bb.remaining() < PAD_SIZE) {
            throw new UnsupportedFormatException("bad pad size (size=" + bb.remaining() + ")");
        }

        // additional/computed attributes
        IOUtils.computeGrainTableCoverage(header);

        if (header.getGrainSize() <= 8) {
            throw new IOException("grainSize must be greater than 8 (actual:" +
                    header.getGrainSize() + ")");
        }
        if (!IOUtils.isPowerOf2(header.getGrainSize())) {
            throw new IOException("grainSize must be a power of 2 (actual:" +
                    header.getGrainSize() + ")");
        }

        // TODO: according to the spec the following test shouldn't fail but
        // **it is actually failing**
        // if((header.getCapacity() % header.getGrainSize()) != 0)
        // {
        // throw new IOException("capacity must be a multiple of grainSize
        // (actual grainSize:"+header.getGrainSize()+", actual
        // capacity="+header.getCapacity()+")");
        // }

        LOG.debug("header=" + header);

        return header;
    }

    /**
     * 
     * @param channel
     * @param header
     * @throws IOException
     */
    public void write(FileChannel channel, SparseExtentHeader header) throws IOException {
        ByteBuffer bb = IOUtils.allocate(1024);
        bb.putInt(MAGIC_NUMBER);
        bb.putInt(VERSION);

        int flags = 0;
        if (header.isValidNewLineDetectionTest()) {
            flags &= 0x01; // bit 0
        }
        if (header.isRedundantGrainTableWillBeUsed()) {
            flags &= 0x02; // bit 1
        }
        bb.putInt(flags);

        bb.putLong(header.getCapacity());
        bb.putLong(header.getGrainSize());
        bb.putLong(header.getDescriptorOffset());
        bb.putLong(header.getDescriptorSize());

        LOG.debug("write: offset(NumGTEsPerGT)=" + bb.position());
        bb.putInt(header.getNumGTEsPerGT());
        bb.putLong(header.getRgdOffset());

        LOG.debug("write: offset(GdOffset)=" + bb.position());
        bb.putLong(header.getGdOffset());
        bb.putLong(header.getOverHead());
        bb.put(header.isUncleanShutdown() ? TRUE : FALSE);

        LOG.debug("write: offset(singleEndLineChar)=" + bb.position());
        bb.put(singleEndLineChar);
        bb.put(nonEndLineChar);
        bb.put(doubleEndLineChar1);
        bb.put(doubleEndLineChar2);

        for (int i = 0; i < PAD_SIZE; i++) {
            bb.put((byte) 0);
        }

        bb.limit(bb.position());
        bb.rewind();
        LOG.debug("write: buffer=" + bb);

        channel.write(bb);
    }
}
