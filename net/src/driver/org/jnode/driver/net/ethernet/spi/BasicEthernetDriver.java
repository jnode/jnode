/*
 * $Id$
 *
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
 
package org.jnode.driver.net.ethernet.spi;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author Martin Husted Hartvig
 */

public abstract class BasicEthernetDriver extends AbstractEthernetDriver {

    private static final long TRANSMIT_TIMEOUT = 5000;

    /**
     * My logger
     */
    protected static final Logger log = Logger.getLogger(BasicEthernetDriver.class);

    /**
     * The device flags
     */
    protected Flags flags;
    /**
     * The actual device driver
     */
    private AbstractDeviceCore abstractDeviceCore;

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(SocketBuffer, HardwareAddress)
     */
    protected void doTransmitEthernet(SocketBuffer skbuf, HardwareAddress destination) throws NetworkException {
        try {
            // Pad
            if (skbuf.getSize() < ETH_ZLEN) {
                skbuf.append(ETH_ZLEN - skbuf.getSize());
            }

            abstractDeviceCore.transmit(skbuf, destination, TRANSMIT_TIMEOUT);
        } catch (InterruptedException ex) {
            throw new NetworkException("Interrupted", ex);
        } catch (TimeoutException ex) {
            throw new NetworkException("Timeout", ex);
        }
    }

    /**
     * Gets the hardware address of this device
     */
    public HardwareAddress getAddress() {
        return abstractDeviceCore.getHwAddress();
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            log.info("Starting " + flags.getName());
            abstractDeviceCore = newCore(getDevice(), flags);
            try {
                abstractDeviceCore.initialize();
            } catch (DriverException ex) {
                abstractDeviceCore.release();
                throw ex;
            }

            super.startDevice();
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim " + flags.getName() + " resources", ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        super.stopDevice();

        abstractDeviceCore.disable();
        abstractDeviceCore.release();
        abstractDeviceCore = null;
    }

    /**
     * Create a new RTL8139Core instance
     */
    protected abstract AbstractDeviceCore newCore(Device device, Flags flags)
        throws DriverException, ResourceNotFreeException;

    /**
     * Get the flags for this device
     *
     * @return The flags
     */
    public Flags getFlags() {
        return flags;
    }

    /**
     * @return Returns the device core.
     */
    protected final AbstractDeviceCore getDeviceCore() {
        return abstractDeviceCore;
    }
}
