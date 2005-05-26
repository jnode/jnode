/*
 * $Id$
 */
package org.jnode.partitions;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PartitionTableType {

    /**
     * Gets the unique name of this partition table type.
     */
    public String getName();

    /**
     * Can this partition table type be used on the given first sector of a
     * blockdevice?
     * 
     * @param devApi
     */
    public boolean supports(byte[] firstSector, BlockDeviceAPI devApi);

    /**
     * Create a partition table for a given device.
     * 
     * @param device
     * @param readOnly
     */
    public PartitionTable create(byte[] firstSector, Device device) throws PartitionTableException;

}
