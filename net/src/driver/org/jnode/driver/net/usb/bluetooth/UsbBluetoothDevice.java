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

import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBRequest;

public class UsbBluetoothDevice implements USBConstants {

    private USBDevice usbDevice;

    private USBEndPoint bulkInEndpoint;
    private USBEndPoint bulkOutEndpoint;
    private USBEndPoint intrInEndpoint;

    public UsbBluetoothDevice() {
    }

    public USBEndPoint getBulkInEndpoint() {
        return bulkInEndpoint;
    }

    public void setBulkInEndpoint(USBEndPoint bulkInEndpoint) {
        this.bulkInEndpoint = bulkInEndpoint;
    }

    public USBEndPoint getBulkOutEndpoint() {
        return bulkOutEndpoint;
    }

    public void setBulkOutEndpoint(USBEndPoint bulkOutEndpoint) {
        this.bulkOutEndpoint = bulkOutEndpoint;
    }

    public USBEndPoint getIntrInEndpoint() {
        return intrInEndpoint;
    }

    public void setIntrInEndpoint(USBEndPoint intrInEndpoint) {
        this.intrInEndpoint = intrInEndpoint;
    }

    public void testCommand() throws USBException {
        final USBControlPipe pipe = usbDevice.getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(
                new SetupPacket(USB_DIR_IN | USB_TYPE_CLASS | USB_RECIP_DEVICE, 0x20, 0, 0, 0), null);
        pipe.syncSubmit(req, GET_TIMEOUT);
    }

    public USBDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(USBDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

}
