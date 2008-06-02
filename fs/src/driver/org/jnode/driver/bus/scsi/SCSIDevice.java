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

package org.jnode.driver.bus.scsi;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.bus.scsi.cdb.spc.CDBRequestSense;
import org.jnode.driver.bus.scsi.cdb.spc.InquiryData;
import org.jnode.driver.bus.scsi.cdb.spc.SenseData;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class SCSIDevice extends Device {

    /**
     * @param bus
     * @param id
     */
    public SCSIDevice(Bus bus, String id) {
        super(bus, id);
    }

    /**
     * Get the device descriptor.
     */
    public abstract InquiryData getDescriptor();

    /**
     * Execute a SCSI command on this device.
     *
     * @param cdb
     * @param data
     * @param dataOffset Offset in data where to start reading / writing
     * @param timeout
     * @return the number of transfered bytes.
     */
    public abstract int executeCommand(CDB cdb, byte[] data,
                                       int dataOffset, long timeout) throws SCSIException,
        TimeoutException, InterruptedException;

    /**
     * Execute a request sense command.
     *
     * @return The requested sense data.
     */
    public final SenseData requestSense() throws SCSIException, TimeoutException, InterruptedException {
        final byte[] data = new byte[96];
        final CDB cdb = new CDBRequestSense(data.length);
        executeCommand(cdb, data, 0, SCSIConstants.GROUP_NOTIMEOUT);
        return new SenseData(data);
    }

    public static final class SCSIDeviceAPIImpl implements SCSIDeviceAPI {

        private final SCSIDevice dev;

        public SCSIDeviceAPIImpl(SCSIDevice dev) {
            this.dev = dev;
        }

        /**
         * @return the number of transfered bytes.
         * @see org.jnode.driver.bus.scsi.SCSIDeviceAPI#executeCommand(org.jnode.driver.bus.scsi.CDB,
         *      byte[], int, long)
         */
        public int executeCommand(CDB cdb, byte[] data, int dataOffset,
                                  long timeout) throws SCSIException, TimeoutException,
            InterruptedException {
            return dev.executeCommand(cdb, data, dataOffset, timeout);
        }

        /**
         * @see org.jnode.driver.bus.scsi.SCSIDeviceAPI#getDescriptor()
         */
        public final InquiryData getDescriptor() {
            return dev.getDescriptor();
        }
    }
}
