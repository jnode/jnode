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
import org.jnode.driver.bus.scsi.CDB;
import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.util.NumberUtils;

final class USBStorageBulkTransport implements ITransport, USBPipeListener,
        USBStorageConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(USBStorageBulkTransport.class);

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
     * Bulk only transport workflow.
     * 
     * @param cdb
     *  
     */
    public void transport(CDB cdb) {
        try {
            byte[] scsiCmd = cdb.toByteArray();
            // Setup command wrapper 
            CBW cbw = new CBW();
            cbw.setSignature(US_BULK_CB_SIGN);
            cbw.setTag(1);
            cbw.setDataTransferLength((byte)cdb.getDataTransfertCount());
            cbw.setFlags((byte) 0);
            cbw.setLun((byte)0);
            cbw.setLength((byte)scsiCmd.length);
            cbw.setCdb(scsiCmd);
            log.debug(cbw.toString());
            // Sent CBW to device
            pipe = (USBDataPipe) devData.getBulkOutEndPoint().getPipe();
            pipe.addListener(this);
			pipe.open();
            USBRequest req = pipe.createRequest(cbw);
            log.debug("*** Request data     : " + req.toString());
            log.debug("*** Request status   : 0x" + NumberUtils.hex(req.getStatus(),4));
            pipe.asyncSubmit(req);
        } catch (USBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bulk-Only mass storage reset.
     */
    public void reset() throws USBException {
        final USBControlPipe pipe = device.getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(new SetupPacket(USB_DIR_OUT
                | USB_TYPE_CLASS | USB_RECIP_INTERFACE, 0xFF, 0, 0, 0), null);
        pipe.syncSubmit(req, GET_TIMEOUT);
    }
    
    /**
     * Get max logical unit allowed by device. Device not support multiple LUN <i>may</i> stall.
     * 
     * @param usbDev
     * @throws USBException
     */
    public void getMaxLun(USBDevice usbDev) throws USBException {
    	log.info("*** Get max lun ***");
    	final USBControlPipe pipe = usbDev.getDefaultControlPipe();
    	final USBPacket packet = new USBPacket(1);
        final USBRequest req = pipe.createRequest(new SetupPacket(USB_DIR_IN
                | USB_TYPE_CLASS | USB_RECIP_INTERFACE, 0xFE, 0, 0, 1), packet);
        pipe.syncSubmit(req, GET_TIMEOUT);
        log.debug("*** Request data     : " + req.toString());
        log.debug("*** Request status   : 0x" + NumberUtils.hex(req.getStatus(),4));
        if(req.getStatus() == USBREQ_ST_COMPLETED){
        	devData.setMaxLun(packet.getData()[0]);
        } else if (req.getStatus() == USBREQ_ST_STALLED){
        	devData.setMaxLun((byte)0);
        } else {
        	throw new USBException("Request status   : 0x" + NumberUtils.hex(req.getStatus(),4));
        }
    }
    

    public void requestCompleted(USBRequest request) {
    	log.debug("*** Request Completed ***");
    	log.debug("*** Request data     : " + request.toString());
		log.debug("USBStorageBulkTransport completed with status : 0x" + NumberUtils.hex(request.getStatus(),4));
		
		CSW csw = (CSW) request;
		log.debug(csw.toString());
    }
        
    public void requestFailed(USBRequest request) {
    	log.debug("*** Request data     : " + request.toString());
        log.debug("USBStorageBulkTransport failed with status : 0x" + NumberUtils.hex(request.getStatus(),4));
        pipe.close();
    }
}
