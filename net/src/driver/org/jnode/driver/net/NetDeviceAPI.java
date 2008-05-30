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

package org.jnode.driver.net;

import org.jnode.driver.DeviceAPI;
import org.jnode.net.HardwareAddress;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.SocketBuffer;

/**
 * Generic API for network devices.
 * <p/>
 * <h3>Transmission</h3>
 * To transmit data, each network device should has a queue of Frame's.
 * The transmit method is called to add a frame to this queue. The device
 * should start a worker thread to process this queue.
 * Once a frame has been transmitted, the notifyTransmission method of that
 * frame must be called.
 * <p/>
 * <h3>Reception</h3>
 * On reception of a frame, a network device must call the receive
 * method of the NetworkLayerManager.
 *
 * @author epr
 * @see org.jnode.net.NetworkLayerManager
 */
public interface NetDeviceAPI extends DeviceAPI {

    /**
     * Gets the hardware address of this device
     */
    public HardwareAddress getAddress();

    /**
     * Gets the maximum transfer unit, the number of bytes this device can
     * transmit at a time.
     */
    public int getMTU();

    /**
     * Add the given frame to the transmit queue of this device.
     * A client to this interface should use methods in Frame to checks for
     * errors and wait for the actual transmission.
     * After the frame has actually been transmitted by the device, the
     * Frame.notifyTransmission method must be called.
     *
     * @param packet      The network packet to transmit. No linklayer header has
     *                    been added yet.
     * @param destination The destination address, or null for a broadcast.
     * @throws NetworkException
     */
    public void transmit(SocketBuffer packet, HardwareAddress destination)
        throws NetworkException;

    /**
     * Gets the protocol address information for a given protocol.
     *
     * @param protocolID
     * @return The protocol address information, or null if not found.
     */
    public ProtocolAddressInfo getProtocolAddressInfo(int protocolID);

    /**
     * Sets the protocol address information for a given protocol.
     *
     * @param protocolID
     */
    public void setProtocolAddressInfo(int protocolID, ProtocolAddressInfo addressInfo);

    /**
     * Add a net device listener to this device.
     *
     * @param listener
     */
    public void addEventListener(NetDeviceListener listener);

    /**
     * Add a net device listener to this device.
     *
     * @param listener
     */
    public void removeEventListener(NetDeviceListener listener);    
}
