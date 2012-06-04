/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import org.jnode.annotation.SharedStatics;

/**
 * @see java.net.InetAddress
 */
@SharedStatics
class NativeInetAddress {

    /**
     * The special IP address INADDR_ANY.
     */
    private static InetAddress inaddr_any;
    /**
     * Dummy InetAddress, used to bind socket to any (all) network interfaces.
     */
    static InetAddress ANY_IF;

    /**
     * Stores static localhost address object.
     */
    static InetAddress LOCALHOST;

    static {
        // precompute the ANY_IF address
        try {
            ANY_IF = getInaddrAny();

            byte[] ip_localhost = {127, 0, 0, 1};
            LOCALHOST = new Inet4Address("localhost", ip_localhost);
        }
        catch (UnknownHostException uhe) {
            // Hmmm, make one up and hope that it works.
            byte[] zeros = {0, 0, 0, 0};
            ANY_IF = new Inet4Address("0.0.0.0", zeros);
        }
    }

    /**
     * Returns the special address INADDR_ANY used for binding to a local
     * port on all IP addresses hosted by a the local host.
     *
     * @return An InetAddress object representing INDADDR_ANY
     * @throws UnknownHostException If an error occurs
     */
    static InetAddress getInaddrAny() throws UnknownHostException {
        if (inaddr_any == null) {
            byte[] tmp = VMInetAddress.lookupInaddrAny();
            inaddr_any = new Inet4Address(null, tmp);
            inaddr_any.hostName = inaddr_any.getHostName();
        }

        return inaddr_any;
    }

    static InetAddress[] getAllByName0(String hostname) throws UnknownHostException {

        InetAddress[] addresses;

        if (hostname != null)
            hostname = hostname.trim();

        // Default to current host if necessary
        if (hostname == null || hostname.equals("")) {
            addresses = new InetAddress[1];
            addresses[0] = LOCALHOST;
            return addresses;
        }

        // Not in cache, try the lookup
        byte[][] iplist = VMInetAddress.getHostByName(hostname);

        if (iplist.length == 0)
            throw new UnknownHostException(hostname);

        addresses = new InetAddress[iplist.length];

        for (int i = 0; i < iplist.length; i++) {
            if (iplist[i].length != 4)
                throw new UnknownHostException(hostname);

            addresses[i] = new Inet4Address(hostname, iplist[i]);
        }

        return addresses;
    }


    /**
     * @see java.net.InetAddress#init()
     */
    private static void init() {
        //nothing to do
    }
}
