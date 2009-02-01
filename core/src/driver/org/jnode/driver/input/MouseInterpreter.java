/*
 * $Id$
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
 
package org.jnode.driver.input;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.util.NumberUtils;

/**
 * author qades
 */
public class MouseInterpreter implements PointerInterpreter {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(MouseInterpreter.class);
    /**
     * List of valid protocol handlers
     */
    private static final List<MouseProtocolHandler> protocolsHandlers = new ArrayList<MouseProtocolHandler>();

    private byte[] data; // will be defined as 3 or 4 bytes, according to the protocol
    private int pos = 0;
    private MouseProtocolHandler protocol;

    static {
        // should be configurable via an ExtensionPoint
        protocolsHandlers.add(new LogitechWheelMouseProtocol());
        protocolsHandlers.add(new LogitechProtocol());
    }

    public String getName() {
        if (protocol == null) {
            return "No Mouse";
        }
        return protocol.getName();
    }

    public boolean probe(AbstractPointerDriver d) {
        try {
            log.debug("Probe mouse");
            // reset the mouse
            if (!d.initPointer()) {
                log.debug("Reset mouse failed");
                return false;
            }
            int id = d.getPointerId();
            if (id != 0) {
                // does not seem to be a mouse, more likely a tablet of touch screen
                log.debug("PointerId 0x" + NumberUtils.hex(id, 2));
                return false;
            }

            //int protocolBytes = 3; // standard: 3 byte protocol

            // try to make this a 3 button + wheel
            boolean result = d.setRate(200);
            result &= d.setRate(100);
            result &= d.setRate(80);
            // a "normal" mouse doesn't recognize this sequence as special
            // but a mouse with a wheel will change its mouse ID

            id = d.getPointerId();
            log.debug("Actual pointerId 0x" + NumberUtils.hex(id, 2));
            // select protocol
            for (MouseProtocolHandler p : protocolsHandlers) {
                if (p.supportsId(id)) {
                    this.protocol = p;
                    break;
                }
            }
            if (protocol == null) {
                log.error("No mouse driver found for PointerID " + id);
                return false;
            }
            this.data = new byte[protocol.getPacketSize()];

            return result;
        } catch (DriverException ex) {
            log.error("Error probing for mouse", ex);
            return false;
        } catch (DeviceException ex) {
            log.error("Error probing for mouse", ex);
            return false;
        }
    }

    /**
     * Process a given byte from the device.
     *
     * @param scancode
     * @return A valid event, or null
     */
    public synchronized PointerEvent handleScancode(int scancode) {
        if (protocol == null) {
            return null;
        }

        // build the data block
        data[pos++] = (byte) (scancode & 0xff);
        pos %= data.length;
        if (pos != 0) {
            return null;
        }

        //System.out.println("data:" + NumberUtils.hex(data));
        // this debug output is for debugging the mouse protocol
        /*
           * String line = ""; for( int i = 0; i < data.length; i++ ) line += "[0x" +
           * Integer.toHexString(data[i]) + "]"; log.debug(line);
           */

        final PointerEvent event = protocol.buildEvent(data);
        // this debug output is to dump the pointer events
        // log.debug(event.toString());
        return event;
    }

    /**
     * Reset the state of this interpreter.
     */
    public synchronized void reset() {
        pos = 0;
    }
}
