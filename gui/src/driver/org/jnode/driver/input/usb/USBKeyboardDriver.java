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
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardAPIAdapter;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardInterpreterFactory;
import org.jnode.util.ByteQueue;
import org.jnode.util.ByteQueueProcessor;
import org.jnode.util.ByteQueueProcessorThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBKeyboardDriver extends Driver implements USBPipeListener, USBConstants,
        ByteQueueProcessor {

    /** My logger */
    private static final Logger log = Logger.getLogger(USBKeyboardDriver.class);
    /** The endpoint we're communicating with */
    private USBEndPoint ep;
    /** The interrupt pipe */
    private USBDataPipe intPipe;
    /** The request data packet */
    private USBPacket intData;
    /** The keyboard API implementation */
    private final KeyboardAPIAdapter apiAdapter = new KeyboardAPIAdapter();
    private byte[] old;
    /** KeyEvent queue */
    private final ByteQueue keyCodeQueue = new ByteQueue();
    /** KeyEvent send thread */
    private ByteQueueProcessorThread keyEventThread;

    private static final char usb_kbd_keycode[] = {
        0, 0, 0, 0, 30, 48, 46, 32, 18, 33, 34, 35, 23, 36, 37, 38, 50, 49, 24, 25, 16, 19,
        31, 20, 22, 47, 17, 45, 21, 44, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 28, 1, 14, 15, 57,
        12, 13, 26, 27, 43, 84, 39, 40, 41, 51, 52, 53, 58, 59, 60, 61, 62, 63, 64, 65, 66,
        67, 68, 87, 88, 99, 70, 119, 110, 102, 104, 111, 107, 109, 106, 105, 108, 103, 69,
        98, 55, 74, 78, 96, 79, 80, 81, 75, 76, 77, 71, 72, 73, 82, 83, 86, 127, 116, 117,
        85, 89, 90, 91, 92, 93, 94, 95, 120, 121, 122, 123, 134, 138, 130, 132, 128, 129,
        131, 137, 133, 135, 136, 113, 115, 114, 0, 0, 0, 124, 0, 181, 182, 183, 184, 185,
        186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 42, 56, 125, 97, 54, 100, 126, 164, 166, 165, 163,
        161, 115, 114, 113, 150, 158, 159, 128, 136, 177, 178, 176, 142, 152, 173, 140};

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            final USBDevice dev = (USBDevice) getDevice();
            final USBConfiguration conf = dev.getConfiguration(0);
            // dev.setConfiguration(conf);
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

            // 
            log.debug("Interval " + ep.getDescriptor().getInterval());

            // Create the interrupt request
            old = new byte[8];
            intData = new USBPacket(ep.getDescriptor().getMaxPacketSize());
            intPipe = (USBDataPipe) ep.getPipe();
            intPipe.addListener(this);
            intPipe.open();
            final USBRequest req = intPipe.createRequest(intData);
            intPipe.asyncSubmit(req);

            // Register the PointerAPI
            apiAdapter.setKbInterpreter(KeyboardInterpreterFactory.getDefaultKeyboardInterpreter());
            dev.registerAPI(KeyboardAPI.class, apiAdapter);

            // Start the key event thread
            keyEventThread =
                    new ByteQueueProcessorThread(dev.getId() + "-daemon", keyCodeQueue, this);
            keyEventThread.start();
        } catch (USBException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        // Stop the key event thread
        keyEventThread.stopProcessor();
        // Unregister API
        getDevice().unregisterAPI(KeyboardAPI.class);
        apiAdapter.clear();
        // Close the pipe
        if (intPipe != null) {
            intPipe.removeListener(this);
            intPipe.close();
            intPipe = null;
        }
        intData = null;
        ep = null;
    }

    /**
     * @see org.jnode.driver.bus.usb.USBPipeListener#requestCompleted(org.jnode.driver.bus.usb.USBRequest)
     */
    public void requestCompleted(USBRequest request) {
        // log.debug("Keyboard interrupt: " + intData);

        final byte[] cur = intData.getData();

        for (int i = 0; i < 8; i++) {
            final int bit = (1 << i);
            if ((old[0] & bit) != (cur[0] & bit)) {
                // Modifier changed
                final int keyCode = usb_kbd_keycode[i + 224];
                // It is an extended keycode
                keyCodeQueue.push((byte) KeyboardInterpreter.XT_EXTENDED);
                if ((old[0] & bit) != 0) {
                    // Released
                    keyCodeQueue.push((byte) (keyCode | KeyboardInterpreter.XT_RELEASE));
                } else {
                    // Pressed
                    keyCodeQueue.push((byte) keyCode);
                }
            }
        }

        for (int i = 2; i < 8; i++) {
            if (((old[i] & 0xFF) > 3) && !contains(cur, 2, old[i])) {
                // Key released
                final int keyCode = usb_kbd_keycode[old[i] & 0xFF];
                if (keyCode > 0) {
                    keyCodeQueue.push((byte) (keyCode | KeyboardInterpreter.XT_RELEASE));
                } else {
                    log.debug("Unknown scancode released " + (old[i] & 0xFF));
                }
            }
            if (((cur[i] & 0xFF) > 3) && !contains(old, 2, cur[i])) {
                // Key pressed
                final int keyCode = usb_kbd_keycode[cur[i] & 0xFF];
                if (keyCode > 0) {
                    keyCodeQueue.push((byte) keyCode);
                } else {
                    log.debug("Unknown scancode pressed " + (cur[i] & 0xFF));
                }
            }

            System.arraycopy(cur, 0, old, 0, cur.length);
        }
    }

    /**
     * @see org.jnode.driver.bus.usb.USBPipeListener#requestFailed(org.jnode.driver.bus.usb.USBRequest)
     */
    public void requestFailed(USBRequest request) {
        log.debug("Keyboard interrupt error status:" + request.getStatus());
        intPipe.close();
    }

    /**
     * Process the given object from the queue.
     * 
     * @param value
     */
    public void process(byte value) throws Exception {
        final int keyCode = value & 0xFF;
        final KeyboardInterpreter intp = apiAdapter.getKbInterpreter();
        apiAdapter.fireEvent(intp.interpretScancode(keyCode));
    }

    private final boolean contains(byte[] arr, int start, byte value) {
        final int max = arr.length;
        for (int i = start; i < max; i++) {
            if (arr[i] == value) {
                return true;
            }
        }
        return false;
    }

}
