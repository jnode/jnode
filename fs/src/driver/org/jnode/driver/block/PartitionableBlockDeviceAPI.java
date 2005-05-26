/*
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

/**
 * This device API is implemented by block devices that
 * support partition tables.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PartitionableBlockDeviceAPI extends BlockDeviceAPI {

    /**
     * Gets the sector size for this device.
     * @return The sector size in bytes
     */
    public int getSectorSize() throws IOException;
}
