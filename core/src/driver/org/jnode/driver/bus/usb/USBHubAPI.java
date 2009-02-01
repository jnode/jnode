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
 
package org.jnode.driver.bus.usb;

import org.jnode.driver.DeviceAPI;

/**
 * Generic interface for controlling an USB HUB.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface USBHubAPI extends DeviceAPI {

    /**
     * Gets the bus to which this HUB is connected.
     */
    public USBBus getUSBBus();

    /**
     * Gets the device for the given port.
     *
     * @param port
     */
    public USBDevice getDevice(int port);

    /**
     * Set the device for the given port.
     *
     * @param dev
     * @param port
     */
    public void setDevice(USBDevice dev, int port);

    /**
     * Unset the device for the given port.
     *
     * @param port
     */
    public void unsetDevice(int port);

    /**
     * Gets the number of downstream ports connected to this HUB.
     */
    public int getNumPorts();

    /**
     * Is the given port enabled.
     */
    public boolean isPortEnabled(int port)
        throws USBException;

    /**
     * Enable/disable a given port
     *
     * @param enabled
     */
    public void setPortEnabled(int port, boolean enabled)
        throws USBException;

    /**
     * Reset a given port
     */
    public void resetPort(int port)
        throws USBException;

    /**
     * Gets the port connection status.
     *
     * @return True if a device is connected, false otherwise
     */
    public boolean isPortConnected(int port)
        throws USBException;

    /**
     * Has the port connection status changed.
     */
    public boolean isPortConnectionStatusChanged(int port)
        throws USBException;

    /**
     * Clear the port connection status changed flag.
     */
    public void clearPortConnectionStatusChanged(int port)
        throws USBException;

    /**
     * Is a lowspeed device connected to the given port.
     * This method is only relevant if the port connection status is true.
     *
     * @return True if a lowspeed device is connected, false otherwise (full or high speed)
     */
    public boolean isPortConnectedToLowSpeed(int port)
        throws USBException;

    /**
     * Is a highspeed device connected to the given port.
     * This method is only relevant if the port connection status is true.
     *
     * @return True if a highspeed device is connected, false otherwise (low or full)
     */
    public boolean isPortConnectedToHighSpeed(int port)
        throws USBException;

    /**
     * Gets the status of the port.
     * This method is here for debugging only.
     *
     * @param port
     * @return Implementation dependent.
     * @throws USBException
     */
    public int getPortStatus(int port)
        throws USBException;

}
