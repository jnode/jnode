/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PartitionableBlockAlignmentSupport<PTE extends PartitionTableEntry>
    extends BlockAlignmentSupport
    implements PartitionableBlockDeviceAPI<PTE> {

    private final PartitionableBlockDeviceAPI<PTE> parentApi;

    /**
     * @param parentApi
     * @param alignment
     */
    public PartitionableBlockAlignmentSupport(PartitionableBlockDeviceAPI<PTE> parentApi, int alignment) {
        super(parentApi, alignment);
        this.parentApi = parentApi;
    }

    /**
     * @see org.jnode.driver.block.PartitionableBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() throws IOException {
        return parentApi.getSectorSize();
    }

    /**
     * Gets the partition table that this block device contains.
     *
     * @return Null if no partition table is found.
     * @throws IOException
     */
    public PartitionTable<PTE> getPartitionTable() throws IOException {
        return parentApi.getPartitionTable();
    }
}
