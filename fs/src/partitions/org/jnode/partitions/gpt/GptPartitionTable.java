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
 
package org.jnode.partitions.gpt;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.util.LittleEndian;

/**
 * The main GPT partition table class.
 *
 * @author Luke Quinane
 */
public class GptPartitionTable implements PartitionTable<GptPartitionTableEntry> {

    /** The type of partition table */
    private final GptPartitionTableType tableType;

    /** The detected block size. */
    private final int blockSize;

    /** The partition entries */
    private final List<GptPartitionTableEntry> partitions = new ArrayList<GptPartitionTableEntry>();

    /** My logger */
    private static final Logger log = Logger.getLogger(GptPartitionTable.class);

    /**
     * Create a new instance
     *
     * @param tableType the partition table type.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param device the drive device.
     */
    public GptPartitionTable(GptPartitionTableType tableType, byte[] first16KiB, Device device) {
        this(tableType, detectBlockSize(first16KiB), first16KiB, device);
    }

    /**
     * Create a new instance
     *
     * @param tableType the partition table type.
     * @param blockSize the block size to use.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param device the drive device.
     */
    public GptPartitionTable(GptPartitionTableType tableType, int blockSize, byte[] first16KiB, Device device) {
        this.tableType = tableType;
        this.blockSize = blockSize;

        if (blockSize != -1) {
            long entries = LittleEndian.getUInt32(first16KiB, blockSize + 0x50);
            int entrySize = (int) LittleEndian.getUInt32(first16KiB, blockSize + 0x54);

            for (int partitionNumber = 0; partitionNumber < entries; partitionNumber++) {
                log.debug("try part " + partitionNumber);

                int offset = blockSize * 2 + (partitionNumber * entrySize);
                GptPartitionTableEntry entry = new GptPartitionTableEntry(this, first16KiB, offset, blockSize);

                log.debug(entry);

                if (entry.isValid()) {
                    partitions.add(entry);
                }
            }
        }
    }

    /**
     * Detects the block size of the GPT partition.
     *
     * @param first16KiB the start of the disk to search for the GPT partition in.
     * @return the detected block size or {@code -1} if no GPT partition is found.
     */
    public static boolean hasSignature(byte[] first16KiB, int blockSize) {
        if (first16KiB.length < blockSize + 8) {
            // Not enough data to check for a valid partition table
            return false;
        }

        byte[] signatureBytes = new byte[8];
        System.arraycopy(first16KiB, blockSize, signatureBytes, 0, signatureBytes.length);
        String signature = new String(signatureBytes, Charset.forName("US-ASCII"));

        if ("EFI PART".equals(signature)) {
            return true;
        }

        return false;
    }

    /**
     * Detects the block size of the GPT partition.
     *
     * @param first16KiB the start of the disk to search for the GPT partition in.
     * @return the detected block size or {@code -1} if no GPT partition is found.
     */
    private static int detectBlockSize(byte[] first16KiB) {
        int[] detectionSizes = new int[] {0x200, 0x1000, 0x2000 };

        for (int blockSize : detectionSizes) {
            if (hasSignature(first16KiB, blockSize)) {
                return blockSize;
            }
        }

        return -1;
    }

    /**
     * Checks if the given boot sector contain a GPT protective MBR partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @return {@code true} if the boot sector contains a protective MBR partition table.
     */
    public static boolean containsProtectiveMbr(byte[] first16KiB) {
        List<IBMPartitionTableEntry> entries = new ArrayList<IBMPartitionTableEntry>();
        for (int partitionNumber = 0; partitionNumber < IBMPartitionTable.TABLE_SIZE; partitionNumber++) {
            IBMPartitionTableEntry partition = new IBMPartitionTableEntry(null, first16KiB, partitionNumber);

            if (partition.isValid()) {
                entries.add(partition);
            }
        }

        if (entries.isEmpty() || entries.get(0).getSystemIndicator() != IBMPartitionTypes.PARTTYPE_EFI_GPT) {
            log.debug("No protective MBR found: " + entries);
            return false;
        }

        return true;
    }

    /**
     * Checks if the given boot sector contain a GPT partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param requireProtectiveMbr {@code true} if the protective MBR must be present.
     * @return {@code true} if the boot sector contains a GPT partition table.
     */
    public static boolean containsPartitionTable(byte[] first16KiB, boolean requireProtectiveMbr) {
        if (requireProtectiveMbr && !containsProtectiveMbr(first16KiB)) {
            return false;
        }

        return detectBlockSize(first16KiB) != -1;
    }

    /**
     * Checks if the given boot sector contain a GPT partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param requireProtectiveMbr {@code true} if the protective MBR must be present.
     * @return {@code true} if the boot sector contains a GPT partition table.
     */
    public static boolean containsPartitionTable(byte[] first16KiB, boolean requireProtectiveMbr, int blockSize) {
        if (requireProtectiveMbr && !containsProtectiveMbr(first16KiB)) {
            return false;
        }

        return hasSignature(first16KiB, blockSize);
    }

    @Override
    public Iterator<GptPartitionTableEntry> iterator() {
        return Collections.unmodifiableList(partitions).iterator();
    }

    /**
     * Gets the block size.
     *
     * @return the block size.
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @see org.jnode.partitions.PartitionTable#getType()
     */
    @Override
    public PartitionTableType getType() {
        return tableType;
    }
}
