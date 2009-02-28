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
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.IOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtent extends Extent {
    private static final Logger LOG = Logger.getLogger(SparseExtent.class);

    private final SparseExtentHeader header;
    private final AllocationTable redundantAllocationTable;
    private final AllocationTable allocationTable;

    public SparseExtent(Descriptor descriptor, ExtentDeclaration extentDecl,
            SparseExtentHeader header, AllocationTable redundantAllocationTable,
            AllocationTable allocationTable) {
        super(descriptor, extentDecl);

        this.header = header;
        this.redundantAllocationTable = redundantAllocationTable;
        this.allocationTable = allocationTable;
    }

    protected int getOffset(long sector, boolean allocate, RandomAccessFile raf) throws IOException {
        final long grainTableCoverage = header.getGrainTableCoverage();
        final int grainDirNum = (int) Math.floor(sector / grainTableCoverage);
        final int grainDirEntry = allocationTable.getGrainDirectory().getEntry(grainDirNum);

        LOG.debug("getGrainTableEntry: grainTableCoverage=" + grainTableCoverage + " grainDirNum=" +
                grainDirNum + " grainDirEntry=" + grainDirEntry + " nbGrainTables=" +
                allocationTable.getNbGrainTables());

        GrainTable grainTable = allocationTable.getGrainTable(grainDirEntry);
        final int grainTableNum =
                (int) Math.floor((sector % grainTableCoverage) / header.getGrainSize());
        int grainTableEntry = grainTable.getEntry(grainTableNum);

        LOG.debug("getGrainTableEntry: grainTableNum=" + grainTableNum + " grainTableEntry=" +
                grainTableEntry);

        if (allocate && (grainTableEntry == 0)) {
            long offset = raf.length();
            raf.setLength(offset + header.getGrainSize() * IOHandler.SECTOR_SIZE);
            LOG.debug("getGrainTableEntry: resized file " + getFileName() + " to " + raf.length());

            grainTableEntry = (int) (offset / IOHandler.SECTOR_SIZE);
            grainTable.setEntry(grainTableNum, grainTableEntry);

            // also modify the redundant table
            grainTable = redundantAllocationTable.getGrainTable(grainDirEntry);
            grainTable.setEntry(grainTableNum, grainTableEntry);
        }

        int grainOffset = grainTableEntry * IOHandler.SECTOR_SIZE;
        return grainOffset;
    }

    public SparseExtentHeader getHeader() {
        return header;
    }

    public AllocationTable getRedundantAllocationTable() {
        return redundantAllocationTable;
    }

    public AllocationTable getAllocationTable() {
        return allocationTable;
    }

    @Override
    public String toString() {
        return "SparseExtent[" + getFileName() + "]";
    }
}
