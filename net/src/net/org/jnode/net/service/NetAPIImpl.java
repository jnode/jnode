/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.net.service;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.VMNetAPI;
import java.net.VMNetDevice;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.net.NetworkLayer;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.ethernet.EthernetConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetAPIImpl implements VMNetAPI {

    private final DefaultNetworkLayerManager nlm;
    
    public NetAPIImpl(DefaultNetworkLayerManager nlm) {
        this.nlm = nlm;
    }
    
    /**
     * @see java.net.VMNetAPI#getInetAddresses(VMNetDevice)
     */
    public List getInetAddresses(VMNetDevice netDevice) {
        final Vector list = new Vector(1, 1);
        final SecurityManager sm = System.getSecurityManager();
        try {
            final NetDeviceImpl netDeviceImpl = (NetDeviceImpl) netDevice;
            final NetDeviceAPI api = (NetDeviceAPI) netDeviceImpl.getDevice()
                    .getAPI(NetDeviceAPI.class);
            final ProtocolAddressInfo info = api
                    .getProtocolAddressInfo(EthernetConstants.ETH_P_IP);

            for (Iterator i = info.addresses().iterator(); i.hasNext();) {
                final ProtocolAddress ipaddr = (ProtocolAddress) i.next();
                if (sm != null) {
                    try {
                        sm.checkConnect(ipaddr.toString(), 58000);
                        list.add(ipaddr.toInetAddress());
                    } catch (SecurityException ex) {
                        // Just don't add
                    }
                } else {
                    list.add(ipaddr.toInetAddress());
                }
            }
        } catch (ApiNotFoundException ex) {
            // Ignore
        }
        return list;
    }

    /**
     * Is the given device a network device.
     * 
     * @param device
     * @return boolean
     */
    public boolean isNetDevice(Device device) {
        return device.implementsAPI(NetDeviceAPI.class);
    }

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
            throws SocketException {

        for (Iterator i = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class)
                .iterator(); i.hasNext();) {
            final Device dev = (Device) i.next();
            try {
                final NetDeviceAPI api = (NetDeviceAPI) dev
                        .getAPI(NetDeviceAPI.class);
                final ProtocolAddressInfo info = api
                        .getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
                if (info.contains(addr)) { return new NetDeviceImpl(dev); }
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
        throw new SocketException(
                "no network interface is bound to such an IP address");
    }

    /**
     * Gets all net devices.
     * 
     * @return A list of Device instances.
     */
    public Collection getNetDevices() {
        final ArrayList list = new ArrayList();
        final Collection devs = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class);
        for (Iterator i = devs.iterator(); i.hasNext();) {
            list.add(new NetDeviceImpl((Device) i.next()));
        }
        return list;
    }

    /**
     * Gets the default local address.
     * 
     * @return InetAddress
     */
    public InetAddress getLocalAddress() throws UnknownHostException {
        final Collection devices = DeviceUtils
                .getDevicesByAPI(NetDeviceAPI.class);
        for (Iterator i = devices.iterator(); i.hasNext();) {
            final Device dev = (Device) i.next();
            try {
                final NetDeviceAPI api = (NetDeviceAPI) dev
                        .getAPI(NetDeviceAPI.class);
                final ProtocolAddressInfo addrInfo = api
                        .getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
                if (addrInfo != null) {
                    final ProtocolAddress addr = addrInfo.getDefaultAddress();
                    if (addr != null) { return addr.toInetAddress(); }
                }
            } catch (ApiNotFoundException ex) {
                // Strange, but ignore
            }
        }
        throw new UnknownHostException("No configured address found");
    }

    public InetAddress[] getHostByName(String hostname)
    throws UnknownHostException {
        
        ArrayList list = null;
        for (Iterator i = nlm.getNetworkLayers().iterator(); i.hasNext(); ) {
            final NetworkLayer layer = (NetworkLayer)i.next();
            final ProtocolAddress[] addrs = layer.getHostByName(hostname);
            if (addrs != null) {
                if (list == null) {
                    list = new ArrayList();
                }
                final int cnt = addrs.length;
                for (int j = 0; j < cnt; j++) {
                    final ProtocolAddress pa = addrs[j];
                    if (pa != null) {
                        list.add(pa.toInetAddress());
                    }
                }
            }
        }

        if (list == null) {
            return null;
        } else {
            return (InetAddress[])list.toArray(new InetAddress[list.size()]);
        }
    }

    /**
     * @see java.net.VMNetAPI#getByName(java.lang.String)
     */
    public VMNetDevice getByName(String name) {
        try {
            final Device dev = DeviceUtils.getDevice(name);
            if (isNetDevice(dev)) {
                return new NetDeviceImpl(dev);
            } else {
                return null;
            }
        } catch (DeviceNotFoundException ex) {
            return null;
        }
    }
}
