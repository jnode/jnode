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

package org.jnode.driver.input.usb;

import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBInterface;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerAPIAdapter;
import org.jnode.driver.input.PointerEvent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBMouseDriver extends Driver implements USBPipeListener, USBConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(USBMouseDriver.class);
    /** The endpoint we're communicating with */
    private USBEndPoint ep;
    /** The interrupt pipe */
    private USBDataPipe intPipe;
    /** The request data packet */
    private USBPacket intData;
    /** The pointer API implementation */
    private final PointerAPIAdapter apiAdapter = new PointerAPIAdapter();

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            final USBDevice dev = (USBDevice) getDevice();

            // Get active configuration
            final USBConfiguration conf = dev.getConfiguration();
            final USBInterface intf = conf.getInterface(0);
            this.ep = null;
            for (int i = 0; i < intf.getDescriptor().getNumEndPoints(); i++) {
                ep = intf.getEndPoint(i);
                if (((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT) &&
                        ((ep.getDescriptor().getEndPointAddress() & USB_DIR_IN) == 0))
                    break;
            }
            if (this.ep == null)
                throw new DriverException(
                        "Found no interrupt endpoint, HID specs required at least one.");

            // Create the interrupt request
            intPipe = (USBDataPipe) ep.getPipe();
            intPipe.addListener(this);
            intPipe.open();
            intData = new USBPacket(ep.getDescriptor().getMaxPacketSize());
            final USBRequest req = intPipe.createRequest(intData);
            intPipe.asyncSubmit(req);

            // Register the PointerAPI
            dev.registerAPI(PointerAPI.class, apiAdapter);
        } catch (USBException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        // Unregister API
        getDevice().unregisterAPI(PointerAPI.class);
        apiAdapter.clear();
        // Close the pipe
        if (intPipe != null) {
            intPipe.close();
            intPipe.removeListener(this);
        }
        intData = null;
        ep = null;
    }

    /**
     * @see org.jnode.driver.bus.usb.USBPipeListener#requestCompleted(org.jnode.driver.bus.usb.USBRequest)
     */
    public void requestCompleted(USBRequest request) {
        // log.debug("Completed");
        final byte[] data = intData.getData();
        int buttons = 0;
        final int v0 = data[0];
        if ((v0 & 0x01) != 0) {
            buttons |= PointerEvent.BUTTON_LEFT;
        }
        if ((v0 & 0x02) != 0) {
            buttons |= PointerEvent.BUTTON_RIGHT;
        }
        if ((v0 & 0x04) != 0) {
            buttons |= PointerEvent.BUTTON_MIDDLE;
        }

        final PointerEvent event =
                new PointerEvent(buttons, data[1], data[2], data[3], PointerEvent.RELATIVE);
        apiAdapter.fireEvent(event);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBPipeListener#requestFailed(org.jnode.driver.bus.usb.USBRequest)
     */
    public void requestFailed(USBRequest request) {
        log.debug("Mouse interrupt error status:" + request.getStatus());
        intPipe.close();
    }

}
