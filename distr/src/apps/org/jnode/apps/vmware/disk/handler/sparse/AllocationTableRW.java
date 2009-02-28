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
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.handler.IOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class AllocationTableRW {
    private static final Logger LOG = Logger.getLogger(SparseExtentRW.class);

    /**
     * 
     * @param raf
     * @param header
     * @return
     * @throws IOException
     */
    public AllocationTable read(RandomAccessFile raf, SparseExtentHeader header) throws IOException {
        long nbGrains = header.getCapacity() / header.getGrainSize();
        int nbGrainTables = (int) (nbGrains / header.getNumGTEsPerGT());
        LOG.debug("read: capacity=" + header.getCapacity() + " grainSize=" + header.getGrainSize() +
                " NumGTEsPerGT=" + header.getNumGTEsPerGT() + " => nbGrainTables=" + nbGrainTables);

        GrainDirectory grainDirectory = new GrainDirectory(readEntries(raf, nbGrainTables));
        GrainTable[] grainTables = new GrainTable[nbGrainTables];
        for (int i = 0; i < grainTables.length; i++) {
            if (LOG.isDebugEnabled()) {
                long pos = raf.getChannel().position();
                if ((pos % IOHandler.SECTOR_SIZE) != 0) {
                    LOG.fatal("read: FATAL: pos not begin of a sector");
                }

                final long gtOffset = (pos / IOHandler.SECTOR_SIZE);
                final long gde = grainDirectory.getEntry(i);
                if (gde != gtOffset) {
                    LOG.fatal("read: FATAL: grainTables[" + i + "] (value:" + gtOffset +
                            ") doesn't match to GrainDirectoryEntry #" + i + "(value:" + gde + ")");
                }

                raf.getChannel().position(gde);
            }

            grainTables[i] = new GrainTable(readEntries(raf, header.getNumGTEsPerGT()));
        }

        return new AllocationTable(grainDirectory, grainTables);
    }

    /**
     * 
     * @param channel
     * @param table
     * @throws IOException
     */
    public void write(FileChannel channel, AllocationTable table) throws IOException {
        write(channel, table.getGrainDirectory());
        for (int gtNum = 0; gtNum < table.getNbGrainTables(); gtNum++) {
            write(channel, table.getGrainTable(gtNum));
        }
    }

    protected void write(FileChannel channel, EntryArray ea) throws IOException {
        ByteBuffer b = IOUtils.allocate(ea.getSize() * IOUtils.INT_SIZE);
        IntBuffer ib = b.asIntBuffer();
        for (int i = 0; i < ea.getSize(); i++) {
            ib.put(ea.getEntry(i));
        }
        ib.rewind();
        channel.write(b);
    }

    protected int[] readEntries(RandomAccessFile raf, int nbEntries) throws IOException {
        IntBuffer bb = IOUtils.getByteBuffer(raf, nbEntries * IOUtils.INT_SIZE).asIntBuffer();

        int[] entries = new int[nbEntries];
        for (int entryNumber = 0; entryNumber < nbEntries; entryNumber++) {
            int entry = bb.get();
            if (entry > 0) {
                LOG.debug("readEntries: entry[" + entryNumber + "]=" + entry);
            }
            entries[entryNumber] = entry;
        }
        return entries;
    }
}
