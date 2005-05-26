/*
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PartitionableBlockAlignmentSupport extends BlockAlignmentSupport
        implements PartitionableBlockDeviceAPI {

    private final PartitionableBlockDeviceAPI parentApi;
    
    /**
     * @param parentApi
     * @param alignment
     */
    public PartitionableBlockAlignmentSupport(PartitionableBlockDeviceAPI parentApi, int alignment) {
        super(parentApi, alignment);
        this.parentApi = parentApi;
    }

    /**
     * @see org.jnode.driver.block.PartitionableBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() throws IOException {
        return parentApi.getSectorSize();
    }        
}
