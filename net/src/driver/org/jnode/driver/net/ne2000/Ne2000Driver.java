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

package org.jnode.driver.net.ne2000;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.spi.AbstractEthernetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public abstract class Ne2000Driver extends AbstractEthernetDriver {

    /**
     * The actual device driver
     */
    private Ne2000Core dd;
    
    /**
     * The device flags
     */
    private final Ne2000Flags flags;

    /**
     * Create a new instance
     * 
     * @param flags
     */
    public Ne2000Driver(Ne2000Flags flags) {
        this.flags = flags;
    }

    /**
     * Gets the hardware address of this device
     */
    public HardwareAddress getAddress() {
        return dd.getHwAddress();
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(SocketBuffer,
     *      HardwareAddress)
     */
    protected void doTransmitEthernet(SocketBuffer skbuf, HardwareAddress destination)
        throws NetworkException {
        try {
            // Pad
            if (skbuf.getSize() < ETH_ZLEN) {
                skbuf.append(ETH_ZLEN - skbuf.getSize());
            }
            dd.transmit(skbuf, destination, 5000);
        } catch (InterruptedException ex) {
            throw new NetworkException("Interrupted", ex);
        } catch (TimeoutException ex) {
            throw new NetworkException("Timeout", ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            dd = newCore(getDevice(), flags);
            dd.initialize();
            super.startDevice();
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim " + flags.getName() + " resources", ex);
        }
    }

    /**
     * Create a new Ne2000Core instance
     */
    protected abstract Ne2000Core newCore(Device device, Ne2000Flags flags)
        throws DriverException, ResourceNotFreeException;

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        super.stopDevice();
        dd.disable();
        dd.release();
        dd = null;
    }

    /**
     * Gets the device flags
     */
    public Ne2000Flags getFlags() {
        return flags;
    }
}
