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
import org.jnode.driver.bus.scsi.cdb.mmc.CapacityData;
import org.jnode.driver.bus.scsi.cdb.mmc.MMCUtils;
import org.jnode.driver.bus.scsi.cdb.spc.CDBInquiry;
import org.jnode.driver.bus.scsi.cdb.spc.CDBTestUnitReady;
import org.jnode.driver.bus.scsi.cdb.spc.InquiryData;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBStorageSCSIHostDriver extends Driver implements SCSIHostControllerAPI, USBPipeListener, USBStorageConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(USBStorageSCSIHostDriver.class);

    /** Storage specific device data */
    private USBStorageDeviceData USBMassStorage;

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
            final USBConfiguration conf = usbDev.getConfiguration(0);
            usbDev.setConfiguration(conf);
			// Set transport protocol
            this.USBMassStorage = new USBStorageDeviceData(conf.getInterface(0).getDescriptor());
            switch (USBMassStorage.getProtocol()) {
            case US_PR_CBI:
            	log.info("*** Set transport protocol to CONTROL/BULK/INTERRUPT");
				break;
			case US_PR_BULK:
				log.info("*** Set transport protocol to BULK ONLY");
				USBMassStorage.setTransport(new USBStorageBulkTransport(usbDev, USBMassStorage));
				((USBStorageBulkTransport)USBMassStorage.getTransport()).getMaxLun(usbDev);
				break;
			case US_PR_SCM_ATAPI:
				log.info("*** Set transport protocol to SCM ATAPI");
			default:
				throw new DriverException("Transport protocol not implemented.");
			}

            USBEndPoint ep;

            for (int i = 0; i < conf.getInterface(0).getDescriptor().getNumEndPoints(); i++) {
                ep = conf.getInterface(0).getEndPoint(i);
                // Is it a bulk endpoint ?
                if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_BULK) {
                    // In or Out ?
                    if ((ep.getDescriptor().getEndPointAddress() & USB_DIR_IN) == 0) {
                    	USBMassStorage.setBulkInEndPoint(ep);
                        log.info("*** Set bulk in endpoint");
                    } else {
                    	USBMassStorage.setBulkOutEndPoint(ep);
                        log.info("*** Set bulk out endpoint");
                    }
                } else if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT) {
                	USBMassStorage.setIntrEndPoint(ep);
                    log.info("*** Set interrupt endpoint");
                }
            }

            usbDev.registerAPI(SCSIHostControllerAPI.class, this);
            final Bus hostBus = new USBStorageSCSIHostBus(getDevice());
            scsiDevice = new USBStorageSCSIDevice(hostBus, "_sg");
            /*try {
                scsiDevice.testUnit();
            } catch (SCSIException ex) {
                throw new DriverException("Cannot TEST UNIT READY device", ex);
            } catch (TimeoutException ex) {
                throw new DriverException("Cannot TEST UNIT READY device : timeout", ex);
            } catch (InterruptedException ex) {
                throw new DriverException("Interrupted while TEST UNIT READY device", ex);
            }*/
            // Execute INQUIRY
            try {
                scsiDevice.inquiry();
                scsiDevice.capacity();
            } catch (SCSIException ex) {
                throw new DriverException("Cannot INQUIRY device", ex);
            } catch (TimeoutException ex) {
                throw new DriverException("Cannot INQUIRY device : timeout", ex);
            } catch (InterruptedException ex) {
                throw new DriverException("Interrupted while INQUIRY device", ex);
            }
            // Register the generic SCSI device.
            try {
                final DeviceManager dm = usbDev.getManager();
                dm.rename(scsiDevice, "sg", true);
                dm.register(scsiDevice);
                dm.rename(usbDev, SCSIHostControllerAPI.DEVICE_PREFIX, true);
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
    /**
     *
     * @author Fabien Lesire
     */
    public final class USBStorageSCSIDevice extends SCSIDevice {

        private InquiryData inquiryResult;

        public USBStorageSCSIDevice(Bus bus, String id) {
            super(bus, id);
        }

        @Override
        public int executeCommand(CDB cdb, byte[] data, int dataOffset, long timeout)
                throws SCSIException, TimeoutException, InterruptedException {
			log.debug("*** execute command ***");
            ITransport t = USBMassStorage.getTransport();
			t.transport(cdb);
            return 0;
        }

        protected final void testUnit() throws SCSIException, TimeoutException, InterruptedException{
        	log.info("*** Test unit ready ***");
        	int res = this.executeCommand(new CDBTestUnitReady(),null,0,50000);
        	log.debug("*** result : 0x" + NumberUtils.hex(res) + " ***" );
        }

        /**
         * Execute an INQUIRY command.
         *
         * @throws SCSIException
         * @throws TimeoutException
         * @throws InterruptedException
         */
        protected final void inquiry() throws SCSIException, TimeoutException,
                InterruptedException {
        	log.info("*** INQUIRY ***");
            final byte[] inqData = new byte[96];
            this.executeCommand(new CDBInquiry(inqData.length), inqData, 0, 50000);
            inquiryResult = new InquiryData(inqData);
            log.debug("INQUIRY Data : " + inquiryResult.toString());
        }

        protected final void capacity() throws SCSIException, TimeoutException, InterruptedException {
        	log.info("*** Read capacity ***");
            CapacityData cd = MMCUtils.readCapacity(this);
            log.debug("Capacity Data : " + cd.toString());

        }

        /**
         * @see org.jnode.driver.bus.scsi.SCSIDeviceAPI#getDescriptor()
         */
        public final InquiryData getDescriptor() {
            return inquiryResult;
        }

    }
}
