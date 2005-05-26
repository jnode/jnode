/*
 * $Id$
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
