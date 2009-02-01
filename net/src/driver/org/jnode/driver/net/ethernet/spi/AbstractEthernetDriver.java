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
 
package org.jnode.driver.net.ethernet.spi;

import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractNetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ethernet.EthernetHeader;
import org.jnode.net.ethernet.EthernetUtils;

/**
 * @author epr
 */
public abstract class AbstractEthernetDriver extends AbstractNetDriver
    implements EthernetConstants {

    /**
     * Gets the maximum transfer unit, the number of bytes this device can
     * transmit at a time.
     */
    public int getMTU() {
        return ETH_DATA_LEN;
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
     */
    protected String getDevicePrefix() {
        return ETH_DEVICE_PREFIX;
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#onReceive(org.jnode.net.SocketBuffer)
     */
    public void onReceive(SocketBuffer skbuf) throws NetworkException {
        // Extract ethernet header
        final EthernetHeader hdr = new EthernetHeader(skbuf);
        skbuf.setLinkLayerHeader(hdr);
        skbuf.setProtocolID(EthernetUtils.getProtocol(hdr));
        skbuf.pull(hdr.getLength());
        // Send to PM
        super.onReceive(skbuf);
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(org.jnode.net.SocketBuffer,
     *      org.jnode.net.HardwareAddress)
     */
    protected final void doTransmit(SocketBuffer skbuf,
                                    HardwareAddress destination) throws NetworkException {
        skbuf.insert(ETH_HLEN);
        if (destination == null) {
            destination = EthernetAddress.BROADCAST;
        }
        destination.writeTo(skbuf, 0);
        getAddress().writeTo(skbuf, 6);
        skbuf.set16(12, skbuf.getProtocolID());

        // check to see if it's for one self, if so don't send on the net and
        // just put in into recieved
        if (getAddress().equals(destination))
            onReceive(skbuf);
        else
            doTransmitEthernet(skbuf, destination);
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(org.jnode.net.SocketBuffer,
     *      org.jnode.net.HardwareAddress)
     */
    protected abstract void doTransmitEthernet(SocketBuffer skbuf, HardwareAddress destination)
        throws NetworkException;
}
