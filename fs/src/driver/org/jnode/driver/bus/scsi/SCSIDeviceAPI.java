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
 
package org.jnode.driver.bus.scsi;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.bus.scsi.cdb.spc.InquiryData;
import org.jnode.util.TimeoutException;


/**
 * API implemented by SCSI device for performing low-level SCSI commands.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface SCSIDeviceAPI extends DeviceAPI {

    /**
     * Get the device descriptor.
     */
    public InquiryData getDescriptor();
    
    /**
     * Execute a SCSI command on this device.
     * @param cdb
     * @param data
     * @param dataOffset Offset in data where to start reading / writing
     * @param timeout
     * @return the number of transfered bytes.
     */
    public int executeCommand(CDB cdb, byte[] data, int dataOffset, long timeout)
    throws SCSIException, TimeoutException, InterruptedException;
    
}
