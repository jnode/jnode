/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.CDB;
import org.jnode.driver.bus.scsi.SCSIConstants;
import org.jnode.driver.bus.scsi.SCSIDevice;
import org.jnode.driver.bus.scsi.SCSIException;
import org.jnode.util.TimeoutException;

/**
 * Command utility methods for the Multimedia command set.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MMCUtils {

    /**
     * Read the capacity data of a given SCSI device.
     *
     * @param dev
     * @return
     */
    public static CapacityData readCapacity(SCSIDevice dev)
        throws SCSIException, TimeoutException, InterruptedException {
        final byte[] data = new byte[CapacityData.DEFAULT_LENGTH];
        final CDB cdb = new CDBReadCapacity();
        dev.executeCommand(cdb, data, 0, SCSIConstants.GROUP1_TIMEOUT);
        return new CapacityData(data);
    }

    /**
     * Read data from the given device.
     *
     * @param dev
     * @param lba
     * @param nrBlocks
     * @param data
     * @param dataOffset
     * @throws SCSIException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public static void readData(SCSIDevice dev, int lba, int nrBlocks,
                                byte[] data, int dataOffset) throws SCSIException,
        TimeoutException, InterruptedException {
        final CDB cdb = new CDBRead10(lba, nrBlocks);
        dev.executeCommand(cdb, data, dataOffset, SCSIConstants.GROUP1_TIMEOUT);
    }

    /**
     * Set the media removal state to locak / unlock the device.
     *
     * @param prevent
     * @param persistent
     * @throws SCSIException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public static void setMediaRemoval(SCSIDevice dev, boolean prevent,
                                       boolean persistent) throws SCSIException, TimeoutException,
        InterruptedException {
        final CDB cdb = new CDBMediaRemoval(prevent, persistent);
        dev.executeCommand(cdb, null, 0, SCSIConstants.GROUP1_TIMEOUT);
    }

    /**
     * Start/stop/eject/load/idle/standby/sleep the device.
     *
     * @throws SCSIException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public static void startStopUnit(SCSIDevice dev,
                                     CDBStartStopUnit.Action action, boolean returnASAP)
        throws SCSIException, TimeoutException, InterruptedException {
        final CDB cdb = new CDBStartStopUnit(action, returnASAP);
        dev.executeCommand(cdb, null, 0, SCSIConstants.GROUP1_TIMEOUT);
    }
}
