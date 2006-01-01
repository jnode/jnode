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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.block.usb.storage;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.scsi.CDB;
import org.jnode.driver.bus.scsi.SCSIDevice;
import org.jnode.driver.bus.scsi.SCSIException;
import org.jnode.driver.bus.scsi.SCSIHostControllerAPI;
import org.jnode.driver.bus.scsi.cdb.spc.CDBInquiry;
import org.jnode.driver.bus.scsi.cdb.spc.InquiryData;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBStorageSCSIHostDriver extends Driver implements SCSIHostControllerAPI, USBPipeListener, USBStorageConstants, USBConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(USBStorageSCSIHostDriver.class);

    /** Storage specific device data */
    private USBStorageDeviceData usbDevData;
    
    /** The SCSI device that i'm host of */
    private USBStorageSCSIDevice scsiDevice;

    /**
     * Initialize this instance.
     */
    public USBStorageSCSIHostDriver() {
    }

    @Override
    protected void startDevice() throws DriverException {
        try {
            final USBDevice usbDev = (USBDevice)getDevice();
            this.usbDevData = new USBStorageDeviceData();

//           Set configuration 0
            final USBConfiguration conf = usbDev.getConfiguration(0);
            usbDev.setConfiguration(conf);
			// Set transport protocol
            usbDevData.setMaxLun((byte)0);
			if(conf.getInterface(0).getDescriptor().getInterfaceProtocol() == US_PR_BULK){
				log.info("*** Set transport protocol to BULK ONLY");
				usbDevData.setTransport(new USBStorageBulkTransport(usbDev, usbDevData));
				((USBStorageBulkTransport)usbDevData.getTransport()).getMaxLun(usbDev);
			} else if(conf.getInterface(0).getDescriptor().getInterfaceProtocol() == US_PR_SCM_ATAPI){
				log.info("*** Set transport protocol to SCM ATAPI");
			}
			
            USBEndPoint ep;
			
            for (int i = 0; i < conf.getInterface(0).getDescriptor().getNumEndPoints(); i++) {
                ep = conf.getInterface(0).getEndPoint(i);
                // Is it a bulk endpoint ?
                if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_BULK) {
                    // In or Out ?
                    if ((ep.getDescriptor().getEndPointAddress() & USB_DIR_IN) == 0) {
                        usbDevData.setBulkInEndPoint(ep);
                    } else {
                        usbDevData.setBulkOutEndPoint(ep);
                    }
                } else if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT) {
                    usbDevData.setIntrEndPoint(ep);
                }
            }

            usbDev.registerAPI(SCSIHostControllerAPI.class, this);
            final Bus hostBus = new USBStorageSCSIHostBus(getDevice());
            scsiDevice = new USBStorageSCSIDevice(hostBus, "_sg");

            // Execute INQUIRY
            try {
                scsiDevice.inquiry();
            } catch (SCSIException ex) {
                throw new DriverException("Cannot INQUIRY device", ex);
            } catch (TimeoutException ex) {
                throw new DriverException("Cannot INQUIRY device", ex);
            } catch (InterruptedException ex) {
                throw new DriverException("Interrupted while INQUIRY device", ex);
            }

            // Register the generic SCSI device.
            try {
                final DeviceManager dm = usbDev.getManager();
                dm.rename(usbDev, SCSIHostControllerAPI.DEVICE_PREFIX, true);
                dm.register(scsiDevice);
            } catch (DeviceAlreadyRegisteredException ex) {
                throw new DriverException(ex);
            }
        } catch (USBException e) {
            throw new DriverException(e);
        }

    }

    @Override
    protected void stopDevice() throws DriverException {
        final Device dev = getDevice();

        // Unregister the SCSI device that we host
        dev.getManager().unregister(scsiDevice);
        dev.unregisterAPI(SCSIHostControllerAPI.class);
    }

    public void requestCompleted(USBRequest request) {
		log.debug("USBStorageSCSIHostDriver completed with status:" + request.getStatus());
    }

    public void requestFailed(USBRequest request) {
        log.debug("USBStorageSCSIHostDriver failed with status:" + request.getStatus());
    }
 
    private final class USBStorageSCSIHostBus extends Bus {

        /**
         * @param parent
         */
        public USBStorageSCSIHostBus(Device parent) {
            super(parent);
        }        
    }
    
    private final class USBStorageSCSIDevice extends SCSIDevice {

        private InquiryData inquiryResult;

        public USBStorageSCSIDevice(Bus bus, String id) {
            super(bus, id);
        }

        @Override
        public int executeCommand(CDB cdb, byte[] data, int dataOffset, long timeout)
                throws SCSIException, TimeoutException, InterruptedException {
			log.debug("*** execute command ***");
            ITransport t = usbDevData.getTransport();
			t.transport(cdb);
            return 0;
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
            final byte[] inqData = new byte[96];
            this.executeCommand(new CDBInquiry(inqData.length), inqData, 0, 5000);
            inquiryResult = new InquiryData(inqData);
        }

        /**
         * @see org.jnode.driver.bus.scsi.SCSIDeviceAPI#getDescriptor()
         */
        public final InquiryData getDescriptor() {
            return inquiryResult;
        }

    }
}
