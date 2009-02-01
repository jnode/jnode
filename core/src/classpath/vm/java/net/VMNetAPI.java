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
 
package java.net;

import java.util.Collection;
import java.util.List;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VMNetAPI {

    /**
     * Gets a network device by its name, or null if not found.
     * 
     * @param name
     * @return
     */
    public VMNetDevice getByName(String name);

    /**
     * Create an list of all InetAddresses of the given device.
     * 
     * @param netDevice
     * @return List of InetAddress instances.
     */
    public List<InetAddress> getInetAddresses(VMNetDevice netDevice);

    /**
     * Return a network device by its address
     * 
     * @param addr
     *            The address of the interface to return
     * 
     * @exception SocketException
     *                If an error occurs
     * @exception NullPointerException
     *                If the specified addess is null
     */
    public VMNetDevice getByInetAddress(InetAddress addr)
            throws SocketException;

    /**
     * Gets all net devices.
     * 
     * @return A list of VMNetDevice instances.
     */
    public Collection<VMNetDevice> getNetDevices();

    /**
     * Gets the default local address.
     * 
     * @return InetAddress
     */
    public InetAddress getLocalAddress() throws UnknownHostException;

    /**
     * Gets the address of a host by its name.
     * @param hostname
     * @return
     * @throws UnknownHostException
     */
    public byte[][] getHostByName(String hostname)
            throws UnknownHostException;

    /**
     * Gets the name of a host by its address.
     * @param ip The host address.
     * @return
     * @throws UnknownHostException
     */
    public String getHostByAddr(byte[] ip)
            throws UnknownHostException;
}
