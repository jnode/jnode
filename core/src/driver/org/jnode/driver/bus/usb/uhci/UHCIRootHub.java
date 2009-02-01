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
 
package org.jnode.driver.bus.usb.uhci;

import org.jnode.driver.bus.usb.USBBus;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBHubAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIRootHub implements USBHubAPI, UHCIConstants {

    private final UHCIIO io;
    private final USBBus bus;
    private final USBDevice[] devices;

    /**
     * Initialize this instance.
     *
     * @param io
     */
    public UHCIRootHub(UHCIIO io, USBBus bus) {
        this.io = io;
        this.bus = bus;
        this.devices = new USBDevice[getNumPorts()];
    }

    public void resetHub() {
        final int ports = getNumPorts();
        for (int i = 0; i < ports; i++) {
            io.setPortSC(i, 0);
        }
    }

    /**
     * Gets the bus to which this HUB is connected.
     */
    public USBBus getUSBBus() {
        return bus;
    }

    /**
     * Gets the device for the given port.
     *
     * @param port
     */
    public USBDevice getDevice(int port) {
        return devices[port];
    }

    /**
     * Set the device for the given port.
     *
     * @param dev
     * @param port
     */
    public void setDevice(USBDevice dev, int port) {
        if (devices[port] != null) {
            throw new IllegalStateException("Cannot overwrite the device at port " + port);
        } else {
            this.devices[port] = dev;
        }
    }

    /**
     * Unset the device for the given port.
     *
     * @param port
     */
    public void unsetDevice(int port) {
        this.devices[port] = null;
    }

    /**
     * Gets the number of downstream ports connected to this HUB.
     */
    public int getNumPorts() {
        return 2;
    }

    /**
     * Is the given port enabled.
     */
    public boolean isPortEnabled(int port) {
        testPort(port);
        return io.getPortSCBits(port, USBPORTSC_PE);
    }

    /**
     * Enable/disable a given port
     *
     * @param enabled
     */
    public void setPortEnabled(int port, boolean enabled) {
        testPort(port);
        io.setPortSCBits(port, USBPORTSC_PE, enabled);
    }

    /**
     * Reset a given port
     */
    public void resetPort(int port) {
        try {
            testPort(port);
            io.setPortSCBits(port, USBPORTSC_PR, true);
            Thread.sleep(100);
            io.setPortSCBits(port, USBPORTSC_PR, false);
            Thread.sleep(1);
            io.setPortSCBits(port, USBPORTSC_PE, true);
            Thread.sleep(10);
            io.setPortSCBits(port, 0xA, true);
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // Ignore
        }
    }

    /**
     * Gets the port connection status.
     *
     * @return True if a device is connected, false otherwise
     */
    public boolean isPortConnected(int port) {
        testPort(port);
        return io.getPortSCBits(port, USBPORTSC_CCS);
    }

    /**
     * Has the port connection status changed.
     */
    public boolean isPortConnectionStatusChanged(int port) {
        testPort(port);
        return io.getPortSCBits(port, USBPORTSC_CSC);
    }

    /**
     * Clear the port connection status changed flag.
     */
    public void clearPortConnectionStatusChanged(int port) {
        testPort(port);
        io.setPortSCBits(port, USBPORTSC_CSC, true);
    }

    /**
     * Is a lowspeed device connected to the given port. This method is only relevant if the port
     * connection status is true.
     *
     * @return True if a lowspeed device is connected, false otherwise (full or high speed)
     */
    public boolean isPortConnectedToLowSpeed(int port) {
        testPort(port);
        return io.getPortSCBits(port, USBPORTSC_LSDA);
    }

    /**
     * Is a highspeed device connected to the given port. This method is only relevant if the port
     * connection status is true.
     *
     * @return True if a highspeed device is connected, false otherwise (low or full)
     */
    public boolean isPortConnectedToHighSpeed(int port) {
        testPort(port);
        return false;
    }

    public int getPortStatus(int port) {
        testPort(port);
        return io.getPortSC(port);
    }

    /**
     * Test for a valid port number
     *
     * @param port
     * @throws IllegalArgumentException
     */
    private final void testPort(int port) throws IllegalArgumentException {
        if ((port < 0) || (port > 1)) {
            throw new IllegalArgumentException("Invalid port " + port);
        }
    }
}
