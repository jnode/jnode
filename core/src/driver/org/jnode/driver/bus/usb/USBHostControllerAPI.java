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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface USBHostControllerAPI extends DeviceAPI {

    /**
     * Create a default control pipe for a given device.
     *
     * @param device
     * @return The created pipe.
     */
    public USBControlPipe createDefaultControlPipe(USBDevice device);

    /**
     * Create a new pipe for a given endpoint.
     * Depending on the type of endpoint, this method will return an instance
     * of USBControlType for control type endpoints and an instance of
     * USBDataPipe for other endpoint transfer types.
     *
     * @param endPoint
     * @return The new pipe.
     * @see USBControlPipe
     * @see USBDataPipe
     */
    public USBPipe createPipe(USBEndPoint endPoint);

    /**
     * Gets the API to control the root HUB.
     */
    public USBHubAPI getRootHUB();

}
