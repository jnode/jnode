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

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;

/**
 * GPT partition table type.
 *
 * @author Luke Quinane
 */
public class GptPartitionTableType implements PartitionTableType {

    /**
     * Indicates whether to require the protective MBR for detection.
     */
    private boolean requireProtectiveMbr;

    /**
     * The block size to expect, or {@code 0} to detect a range of possible block sizes.
     */
    private int blockSize;

    /**
     * Creates a new instance.
     */
    public GptPartitionTableType() {
        this(true, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param requireProtectiveMbr indicates whether to require the protective MBR for detection.
     * @param blockSize the block size to expect, or {@code 0} to expect a range of possible block sizes.
     */
    public GptPartitionTableType(boolean requireProtectiveMbr, int blockSize) {
        this.requireProtectiveMbr = requireProtectiveMbr;
        this.blockSize = blockSize;
    }

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) {
        if (blockSize == 0) {
            return new GptPartitionTable(this, firstSector, device);
        } else {
            return new GptPartitionTable(this, blockSize, firstSector, device);
        }
    }

    @Override
    public String getName() {
        return "EFI PART";
    }

    @Override
    public boolean supports(byte[] first16KiB, BlockDeviceAPI devApi) {
        if (blockSize == 0) {
            return GptPartitionTable.containsPartitionTable(first16KiB, requireProtectiveMbr);
        } else {
            return GptPartitionTable.containsPartitionTable(first16KiB, requireProtectiveMbr, blockSize);
        }
    }
}
