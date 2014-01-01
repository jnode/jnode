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

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;

/**
 * Apple Partition Map (APM) partition table type.
 *
 * @author Luke Quinane
 */
public class ApmPartitionTableType implements PartitionTableType {

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) throws PartitionTableException {
        return new ApmPartitionTable(this, firstSector, device);
    }

    @Override
    public String getName() {
        return "APM";
    }

    @Override
    public boolean supports(byte[] first16KiB, BlockDeviceAPI devApi) {
        return ApmPartitionTable.containsPartitionTable(first16KiB);
    }
}
