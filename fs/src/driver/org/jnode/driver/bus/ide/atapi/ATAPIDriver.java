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
 
package org.jnode.driver.bus.ide.atapi;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.command.IDEPacketCommand;
import org.jnode.driver.bus.scsi.CDB;
import org.jnode.driver.bus.scsi.SCSIDevice;
import org.jnode.driver.bus.scsi.SCSIException;
import org.jnode.driver.bus.scsi.SCSIHostControllerAPI;
import org.jnode.driver.bus.scsi.cdb.spc.CDBInquiry;
import org.jnode.driver.bus.scsi.cdb.spc.InquiryData;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class ATAPIDriver extends Driver implements SCSIHostControllerAPI {

    private ATAPIBus atapiBus;

    private ATAPISCSIDevice scsiDevice;
    
    private static final Logger log = Logger.getLogger(ATAPIDriver.class);
    
    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        final Device ideDev = getDevice();
        
        // Register my api's
        ideDev.registerAPI(SCSIHostControllerAPI.class, this);
        
        // Create the ATAPI bus
        this.atapiBus = new ATAPIBus(ideDev);

        // Create the generic SCSI device, attached to the ATAPI bus
        scsiDevice = new ATAPISCSIDevice(atapiBus, "_sg");
        
        // Execute INQUIRY
        try {
            scsiDevice.inquiry();
        } catch (SCSIException ex) {
            throw new DriverException("Cannot INQUIRY device due to SCSIException", ex);
        } catch (TimeoutException ex) {
            throw new DriverException("Cannot INQUIRY device due to TimeoutException", ex);
        } catch (InterruptedException ex) {
            throw new DriverException("Interrupted while INQUIRY device", ex);
        }

        // Register the generic SCSI device.
        try {
            final DeviceManager dm = ideDev.getManager();
            synchronized (dm) {
                dm.rename(scsiDevice, "sg", true);
                dm.register(scsiDevice);
                dm.rename(ideDev, SCSIHostControllerAPI.DEVICE_PREFIX, true);
            }
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        final Device ideDev = getDevice();

        // Unregister the SCSI device
        try {
            ideDev.getManager().unregister(scsiDevice);
        } finally {
            scsiDevice = null;
            atapiBus = null;
        }
        
        // Unregister my api's
        ideDev.unregisterAPI(SCSIHostControllerAPI.class);
    }

    /**
     * ATAPI bus implementation.
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static class ATAPIBus extends Bus {

        public ATAPIBus(Device dev) {
            super(dev);
        }
    }

    class ATAPISCSIDevice extends SCSIDevice {

        private InquiryData inquiryResult;
        
        

        /**
         * Initialize this instance.
         * 
         * @param bus
         * @param id
         */
        public ATAPISCSIDevice(ATAPIBus bus, String id) {
            super(bus, id);
        }

        /**
         * (non-Javadoc)
         * @see org.jnode.driver.bus.scsi.SCSIDevice#executeCommand(org.jnode.driver.bus.scsi.CDB, byte[], int, long)
         */
        public final int executeCommand(CDB cdb, byte[] data,
                int dataOffset, long timeout) throws SCSIException,
                TimeoutException, InterruptedException {
            final IDEDevice dev = (IDEDevice) getDevice();
            final IDEBus bus = (IDEBus) dev.getBus();

            final IDEPacketCommand cmd = new IDEPacketCommand(
                    dev.isPrimary(), dev.isMaster(), cdb.toByteArray(), data,
                    dataOffset);
            bus.executeAndWait(cmd, timeout);

            if (!cmd.isFinished()) {
                throw new TimeoutException("Timeout in SCSI command");
            } else if (cmd.hasError()) {
                throw new SCSIException("Command error 0x" + NumberUtils.hex(cmd.getError(), 2));
            } else {
                return cmd.getDataTransfered();
            }
        }

        /**
         * Execute an INQUUIRY command.
         * 
         * @throws SCSIException
         * @throws TimeoutException
         * @throws InterruptedException
         */
        protected final void inquiry() throws SCSIException, TimeoutException,
                InterruptedException {
            final byte[] inqData = new byte[ 96];
            scsiDevice.executeCommand(new CDBInquiry(inqData.length), inqData,
                    0, 50000);
            inquiryResult = new InquiryData(inqData);
            log.debug("INQUIRY Data : " + inquiryResult.toString());
        }

        /**
         * (non-Javadoc)
         * @see org.jnode.driver.bus.scsi.SCSIDevice#getDescriptor()
         */
        public final InquiryData getDescriptor() {
            return inquiryResult;
        }
    }
}
