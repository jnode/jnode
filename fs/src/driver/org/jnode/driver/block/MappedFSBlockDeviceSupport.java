/*
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.partitions.PartitionTableEntry;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MappedFSBlockDeviceSupport extends MappedBlockDeviceSupport implements FSBlockDeviceAPI {

    private final FSBlockDeviceAPI parentApi;
    
    /**
     * @param parent
     * @param offset
     * @param length
     * @throws IOException
     */
    public MappedFSBlockDeviceSupport(Device parent, long offset, long length)
            throws IOException {
        super(parent, offset, length);
        try {
            this.parentApi = (FSBlockDeviceAPI) parent.getAPI(FSBlockDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
			final IOException ioe = new IOException("BlockDeviceAPI not found on device");
			ioe.initCause(ex);
			throw ioe;
        }
        registerAPI(FSBlockDeviceAPI.class, this);
    }
    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return parentApi.getPartitionTableEntry();
    }
    
    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() throws IOException {
        return parentApi.getSectorSize();
    }
}
