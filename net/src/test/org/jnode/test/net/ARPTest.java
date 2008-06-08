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
 
package org.jnode.test.net;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.util.NetUtils;

/**
 * @author epr
 */
public class ARPTest {

    /**
     * Perform an ARP request
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final ARPNetworkLayer arp =
                (ARPNetworkLayer) NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_ARP);
        final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
        final IPv4Address addr = new IPv4Address(args[0]);
        final IPv4Address myAddr = new IPv4Address(args[1]);
        final Device dev = dm.getDevice(args[2]);
        final long timeout = 5000;

        final HardwareAddress hwAddr;
        hwAddr = arp.getHardwareAddress(addr, myAddr, dev, timeout);

        System.out.println("Found hwaddress:" + hwAddr);
    }

}
