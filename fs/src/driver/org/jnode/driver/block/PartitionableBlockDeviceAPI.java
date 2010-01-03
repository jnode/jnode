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
 
package org.jnode.driver.block;

import java.io.IOException;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableEntry;

/**
 * This device API is implemented by block devices that
 * support partition tables.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PartitionableBlockDeviceAPI
    <PTE extends PartitionTableEntry>
    extends BlockDeviceAPI {

    /**
     * Gets the sector size for this device.
     *
     * @return The sector size in bytes
     * @throws IOException
     */
    public int getSectorSize() throws IOException;

    /**
     * Gets the partition table that this block device contains.
     *
     * @return Null if no partition table is found.
     * @throws IOException
     */
    public PartitionTable<PTE> getPartitionTable() throws IOException;
}
