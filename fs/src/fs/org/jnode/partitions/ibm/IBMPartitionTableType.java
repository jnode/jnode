/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.partitions.ibm;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;

/**
 * IBM partition table table.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IBMPartitionTableType implements PartitionTableType {

    /**
     * @see org.jnode.partitions.PartitionTableType#create(org.jnode.driver.Device)
     */
    public PartitionTable create(byte[] firstSector, Device device) throws PartitionTableException {
        return new IBMPartitionTable(firstSector, device);
    }

    /**
     * @see org.jnode.partitions.PartitionTableType#getName()
     */
    public String getName() {
        return "IBM";
    }

    /**
     * @see org.jnode.partitions.PartitionTableType#supports(org.jnode.driver.block.BlockDeviceAPI)
     */
    public boolean supports(byte[] firstSector, BlockDeviceAPI devApi) {
        // TODO Make a suitable implementation
        return true;
    }
}
