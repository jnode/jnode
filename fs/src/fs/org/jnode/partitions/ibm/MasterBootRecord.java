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
 
package org.jnode.partitions.ibm;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;

public class MasterBootRecord {
    private static final int PARTITION_TABLE_OFFSET = 0x1be;
    private static final int PARTITION_TABLE_END_OFFSET = PARTITION_TABLE_OFFSET + 64;

    private final ByteBuffer mbr;
    private boolean dirty;
    private final IBMPartitionTableEntry[] partitions;

    public MasterBootRecord() {
        mbr = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
        dirty = false;
        partitions = new IBMPartitionTableEntry[4];
    }

    public MasterBootRecord(byte[] buffer) throws IOException {
        mbr = ByteBuffer.wrap(buffer);
        dirty = false;
        partitions = new IBMPartitionTableEntry[4];
    }

    public MasterBootRecord(BlockDeviceAPI devApi) throws IOException {
        this();
        read(devApi);
    }

    public final boolean containsPartitionTable() {
        return IBMPartitionTable.containsPartitionTable(mbr.array());
    }

    public final void copyPartitionTableFrom(MasterBootRecord srcMbr) {
        srcMbr.mbr.position(PARTITION_TABLE_OFFSET).limit(PARTITION_TABLE_END_OFFSET);
        mbr.position(PARTITION_TABLE_OFFSET);
        mbr.put(srcMbr.mbr);
    }

    /**
     * Write the BPB to the MBR to its Correct Position.
     * 
     * @param bpb
     */
    public final void setBPB(byte[] bpb) {
        System.arraycopy(bpb, 0, mbr.array(), 0x3, bpb.length);
    }

    /**
     * Write the contents of this bootsector to the given device.
     * 
     * @param device
     */
    public final synchronized void write(BlockDeviceAPI devApi) throws IOException {
        devApi.write(0, mbr);
        devApi.flush();
        dirty = false;
    }

    /**
     * Read the contents of this bootsector from the given device.
     * 
     * @param device
     */
    public final synchronized void read(BlockDeviceAPI api) throws IOException {
        api.read(0, mbr);
        dirty = false;
    }

    /**
     * TODO remove the temporary workaround : internal array shouldn't be exposed
     * @return
     */
    public byte[] array() {
        return mbr.array();
    }
}
