/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.driver.net.usb.bluetooth;

import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.usb.InterfaceDescriptor;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBInterface;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.driver.net.usb.UsbNetConstant;
import org.jnode.util.NumberUtils;

public class UsbBluetoothDriver extends Driver implements USBPipeListener, UsbNetConstant {

    private static final Logger log = Logger.getLogger(UsbBluetoothDriver.class);

    USBDataPipe intPipe;
    USBPacket intData;

    @Override
    protected void startDevice() throws DriverException {

        UsbBluetoothDevice UsbBtDevice = new UsbBluetoothDevice();

        final USBDevice dev = (USBDevice) getDevice();
        UsbBtDevice.setUsbDevice(dev);
        final USBConfiguration conf = dev.getConfiguration(0);
        final USBInterface intf = conf.getInterface(0);
        final InterfaceDescriptor iDesc = intf.getDescriptor();

        USBEndPoint bulkInEndpoint[] = new USBEndPoint[8];
        USBEndPoint bulkOutEndpoint[] = new USBEndPoint[8];
        USBEndPoint intrInEndpoint[] = new USBEndPoint[8];

        int num_bulk_in = 0;
        int num_bulk_out = 0;
        int num_bulk_intr = 0;

        USBEndPoint ep = null;

        for (int i = 0; i < iDesc.getNumEndPoints(); i++) {
            ep = intf.getEndPoint(i);
            // Is it a bulk endpoint ?
            if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_BULK) {
                // In or Out ?
                if ((ep.getDescriptor().getEndPointAddress() & USB_DIR_IN) == 0) {
                    bulkInEndpoint[num_bulk_in] = ep;
                    num_bulk_in++;
                    log.debug("*** Set bulk in endpoint");
                } else {
                    bulkOutEndpoint[num_bulk_out] = ep;
                    num_bulk_out++;
                    log.debug("*** Set bulk out endpoint");
                }
            } else if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT) {
                intrInEndpoint[num_bulk_intr] = ep;
                num_bulk_intr++;
                log.debug("*** Set interrupt endpoint");
            }
        }
        if ((num_bulk_intr != 1) || (num_bulk_intr != 1) || (num_bulk_intr != 1)) {
            throw new DriverException("Must have one bulk-in (" + num_bulk_in +
                    "), one bulk out (" + num_bulk_out + ") and one interrupt (" + num_bulk_intr +
                    ") endpoints. Driver not bound.");
        }
        UsbBtDevice.setBulkInEndpoint(bulkInEndpoint[0]);
        UsbBtDevice.setBulkOutEndpoint(bulkOutEndpoint[0]);
        UsbBtDevice.setIntrInEndpoint(intrInEndpoint[0]);

        intPipe = (USBDataPipe) UsbBtDevice.getIntrInEndpoint().getPipe();
        intPipe.addListener(this);
        try {
            intPipe.open();
            intData =
                    new USBPacket(UsbBtDevice.getIntrInEndpoint().getDescriptor()
                            .getMaxPacketSize());
            final USBRequest req = intPipe.createRequest(intData);
            intPipe.asyncSubmit(req);
        } catch (USBException e1) {
            log.debug("*** USB exception occurs.");
            e1.printStackTrace();
        }
        log.debug("*** Send test command via control endpoint");
        try {
            UsbBtDevice.testCommand();
        } catch (USBException e) {
            log.debug("*** USB exception occurs.");
            e.printStackTrace();
        }
    }

    @Override
    protected void stopDevice() throws DriverException {
        if (intPipe != null) {
            intPipe.close();
            intPipe.removeListener(this);
        }
        intData = null;
    }

    public void requestCompleted(USBRequest request) {
        // log.debug("Bluetooth Completed");
        final byte[] data = intData.getData();
        StringBuffer buffer = new StringBuffer();
        buffer.append("Data received : ");
        for (int i = 0; i < data.length; i++) {
            buffer.append(NumberUtils.hex(data[0], 2)).append(" ");
        }
        log.debug(buffer.toString());
    }

    public void requestFailed(USBRequest request) {
        log.debug("Bluetooth interrupt error status:" + request.getStatus());
        intPipe.close();
    }

}
