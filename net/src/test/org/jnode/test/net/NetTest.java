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
 
package org.jnode.test.net;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;

/**
 * @author epr
 */
public class NetTest {

    public static void main(String[] args) {

        String devId = (args.length > 0) ? args[0] : "eth0";

        try {
            final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
            final Device dev = dm.getDevice(devId);
            final NetDeviceAPI api = dev.getAPI(NetDeviceAPI.class);
            final EthernetAddress mac = (EthernetAddress) api.getAddress();

            SocketBuffer skbuf = new SocketBuffer();
            skbuf.insert(28);
            skbuf.set16(0, 0x0001); // Hardware type
            skbuf.set16(2, 0x0800); // Protocol type
            skbuf.set(4, 6); // Hardware address size
            skbuf.set(5, 4); // Protocol address size
            skbuf.set16(6, 0x01); // Operation APR request
            mac.writeTo(skbuf, 8); // Source mac
            skbuf.set32(14, 0xc0a8c853); // Source IP
            skbuf.set32(14, 0xc0a8c801); // Target IP

            // Prefix ethernet header
            skbuf.insert(14);
            // Set dest address
            EthernetAddress dst = new EthernetAddress("ff-ff-ff-ff-ff-ff");
            dst.writeTo(skbuf, 0);

            // Set packet type
            skbuf.set16(12, 0x0806);

            //api.transmit(skbuf);

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
