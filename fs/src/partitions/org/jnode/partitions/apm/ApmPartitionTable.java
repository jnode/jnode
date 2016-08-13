/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.partitions.apm;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;
import org.jnode.util.BigEndian;

/**
 * The main Apple Partition Map (APM) partition table class.
 *
 * @author Luke Quinane
 */
public class ApmPartitionTable implements PartitionTable<ApmPartitionTableEntry> {

    /** The type of partition table */
    private final ApmPartitionTableType tableType;

    /** The partition entries */
    private final List<ApmPartitionTableEntry> partitions = new ArrayList<ApmPartitionTableEntry>();

    /** My logger */
    private static final Logger log = Logger.getLogger(ApmPartitionTable.class);

    /**
     * Create a new instance
     *
     * @param tableType the partition table type.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param device the drive device.
     */
    public ApmPartitionTable(ApmPartitionTableType tableType, byte[] first16KiB, Device device) {
        this.tableType = tableType;

        long entries = BigEndian.getUInt32(first16KiB, 0x204);

        for (int partitionNumber = 0; partitionNumber < entries; partitionNumber++) {
            log.debug("try part " + partitionNumber);

            int offset = 0x200 + (partitionNumber * 0x200);

            ApmPartitionTableEntry entry = new ApmPartitionTableEntry(this, first16KiB, offset);

            if (entry.isValid()) {
                partitions.add(entry);
            }
        }
    }

    /**
     * Checks if the given boot sector contain a APM partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @return {@code true} if the boot sector contains a APM partition table.
     */
    public static boolean containsPartitionTable(byte[] first16KiB) {
        if (first16KiB.length < 0x250) {
            // Not enough data for detection
            return false;
        }

        if ((first16KiB[0x200] & 0xFF) != 0x50) {
            return false;
        }
        if ((first16KiB[0x201] & 0xFF) != 0x4d) {
            return false;
        }

        byte[] typeBytes = new byte[31];
        System.arraycopy(first16KiB, 0x230, typeBytes, 0, typeBytes.length);
        String type = new String(typeBytes, Charset.forName("ASCII")).replace("\u0000", "");

        if (!"Apple_partition_map".equalsIgnoreCase(type)) {
            return false;
        }

        return true;
    }

    @Override
    public Iterator<ApmPartitionTableEntry> iterator() {
        return Collections.unmodifiableList(partitions).iterator();
    }

    /**
     * @see org.jnode.partitions.PartitionTable#getType()
     */
    @Override
    public PartitionTableType getType() {
        return tableType;
    }
}
