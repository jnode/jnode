/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTableEntry;


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
            this.parentApi = parent.getAPI(FSBlockDeviceAPI.class);
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
