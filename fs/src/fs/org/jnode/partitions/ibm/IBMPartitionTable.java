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
 
package org.jnode.partitions.ibm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;
import org.jnode.util.LittleEndian;

/**
 * @author epr
 */
public class IBMPartitionTable implements PartitionTable<IBMPartitionTableEntry> {
    private static final int TABLE_SIZE = 4;

    /** The type of partition table */
    private final IBMPartitionTableType tableType;
    
    /** The partition entries */
    private final IBMPartitionTableEntry[] partitions;

    /** The device */
    private final Device drivedDevice;

    /** Extended partition */
    private final ArrayList<IBMPartitionTableEntry> extendedPartitions =
            new ArrayList<IBMPartitionTableEntry>();

    /** My logger */
    private static final Logger log = Logger.getLogger(IBMPartitionTable.class);

    /** The position of the extendedPartition in the table */
    private int extendedPartitionEntry = -1;

    /**
     * Create a new instance
     * 
     * @param bootSector
     */
    public IBMPartitionTable(IBMPartitionTableType tableType, byte[] bootSector, Device device) {
        // this.bootSector = bootSector;
        this.tableType = tableType;
        this.drivedDevice = device;
        if (containsPartitionTable(bootSector)) {
            this.partitions = new IBMPartitionTableEntry[TABLE_SIZE];
            for (int partNr = 0; partNr < partitions.length; partNr++) {
                log.debug("try part " + partNr);
                partitions[partNr] = new IBMPartitionTableEntry(this, bootSector, partNr);
                if (partitions[partNr].isExtended()) {
                    extendedPartitionEntry = partNr;
                    log.debug("Found Extended partitions");
                    handleExtended(partitions[partNr]);
                }
            }
        } else {
            partitions = null;
        }
    }

    /**
     * Fill the extended Table
     */
    private void handleExtended(IBMPartitionTableEntry current) {

        final long startLBA = current.getStartLba();
        final ByteBuffer sector = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
        try {
            log.debug("Try to read the Extended Partition Table");
            BlockDeviceAPI api = drivedDevice.getAPI(BlockDeviceAPI.class);
            api.read(startLBA * IDEConstants.SECTOR_SIZE, sector);
        } catch (ApiNotFoundException e) {
            // I think we ca'nt get it
            log.error("API Not Found Exception");
        } catch (IOException e) {
            // I think we ca'nt get it
            log.error("IOException");
        }

        IBMPartitionTableEntry entry = null;
        for (int i = 0; i < TABLE_SIZE; i++) {
            entry = new IBMPartitionTableEntry(this, sector.array(), i);
            if (entry.isValid() && !entry.isEmpty()) {
                // correct the offset
                if (entry.isExtended()) {
                    entry.setStartLba(entry.getStartLba() +
                            partitions[extendedPartitionEntry].getStartLba());
                    handleExtended(entry);
                } else {
                    entry.setStartLba(entry.getStartLba() + current.getStartLba());
                    extendedPartitions.add(entry);
                }
            } 
        }
    }

    public boolean hasExtended() {
        return !extendedPartitions.isEmpty();
    }

    /**
     * Does the given boot sector contain an IBM partition table?
     * 
     * @param bootSector the data to check.
     * @return {@code true} if the data contains an IBM partition table, {@code false} otherwise.
     */
    public static boolean containsPartitionTable(byte[] bootSector) {
        if (LittleEndian.getUInt16(bootSector, 510) != 0xaa55) {
            return false;
        }

        if (LittleEndian.getUInt16(bootSector, 428) == 0x5678) {
            // Matches the AAP MBR extra signature, probably an valid partition table
            return true;
        }

        if (LittleEndian.getUInt16(bootSector, 380) == 0xa55a) {
            // Matches the AST/NEC MBR extra signature, probably an valid partition table
            return true;
        }

        if (LittleEndian.getUInt16(bootSector, 252) == 0x55aa) {
            // Matches the Disk Manager MBR extra signature, probably an valid partition table
            return true;
        }

        if (LittleEndian.getUInt32(bootSector, 2) == 0x4c57454e) {
            // Matches the NEWLDR MBR extra signature, probably an valid partition table
            return true;
        }

        // Nothing matched, fall back to validating any specified partition entries
        IBMPartitionTableEntry lastValid = null;
        boolean foundValidEntry = false;
        for (int partitionNumber = 0; partitionNumber < TABLE_SIZE; partitionNumber++) {
            IBMPartitionTableEntry partition = new IBMPartitionTableEntry(null, bootSector, partitionNumber);

            if (partition.isValid()) {
                if (lastValid != null) {
                    if (lastValid.getStartLba() + lastValid.getNrSectors() > partition.getStartLba()) {
                        // End of previous partition entry after the start of the next one
                        return false;
                    }
                }

                foundValidEntry = true;
                lastValid = partition;
            }
        }

        return foundValidEntry;
    }

    public Iterator<IBMPartitionTableEntry> iterator() {
        return new Iterator<IBMPartitionTableEntry>() {
            private int index = 0;
            private final int last = (partitions == null) ? 0 : partitions.length - 1;

            public boolean hasNext() {
                return index < last;
            }

            public IBMPartitionTableEntry next() {
                return partitions[index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @return Returns the extendedPartitions.
     */
    public List<IBMPartitionTableEntry> getExtendedPartitions() {
        return extendedPartitions;
    }

    /**
     * @see org.jnode.partitions.PartitionTable#getType()
     */
    public PartitionTableType getType() {
        return tableType;
    }
}
