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
import org.jnode.driver.bus.scsi.CDB;
import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;

final class USBStorageBulkTransport implements ITransport, USBPipeListener,
        USBStorageConstants, USBConstants {

    /** My logger */
    private static final Logger log = Logger
            .getLogger(USBStorageBulkTransport.class);

    /** The device */
    private final USBDevice device;
    
    /* */
    private final USBStorageDeviceData devData;

    USBDataPipe pipe;

    /**
     * 
     * @param dev
     */
    public USBStorageBulkTransport(USBDevice device, USBStorageDeviceData devData) {
        this.device = device;
        this.devData = devData;
    }

    /**
     * 
     */
    public void transport(CDB cdb) {
        try {
            byte[] scsiCmd = cdb.toByteArray();
            // Setup command wrapper 
            BulkCommandBlockWrapper bcb = new BulkCommandBlockWrapper();
            bcb.setSignature(US_BULK_CB_SIGN);
            bcb.setDataTransferLength(scsiCmd.length);
            bcb.setFlags((byte) 0);
            bcb.setLun((byte)0);
            bcb.setCdb(scsiCmd);
            // Sent CBW to device
            USBPacket data = new USBPacket(bcb.getCBW());
            pipe = (USBDataPipe) devData.getBulkOutEndPoint().getPipe();
			pipe.open();
            USBRequest req = pipe.createRequest(data);
            pipe.addListener(this);
            pipe.syncSubmit(req,5000);
            
        } catch (USBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bulk-Only mass storage reset.
     */
    public void reset() throws USBException {
        final USBControlPipe pipe = device.getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(new SetupPacket(0x01
                | USB_TYPE_CLASS | USB_RECIP_INTERFACE, 0xFF, 0, 0, 0), null);
        pipe.syncSubmit(req, GET_TIMEOUT);
    }
    
    public void getMaxLun(USBDevice usbDev) throws USBException {
    	final USBControlPipe pipe = usbDev.getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(new SetupPacket(USB_DIR_IN
                | USB_TYPE_CLASS | USB_RECIP_INTERFACE, 0xFE, 0, 0, 1), new USBPacket(1));
        pipe.syncSubmit(req, GET_TIMEOUT);
        log.debug("*** Time out reach, request status :" + req.getStatus());
    }
    

    public void requestCompleted(USBRequest request) {
		log.debug("USBStorageBulkTransport completed with status:" + request.getStatus());
    }

    public void requestFailed(USBRequest request) {
        log.debug("USBStorageBulkTransport failed with status:" + request.getStatus());
        pipe.close();
    }

}
