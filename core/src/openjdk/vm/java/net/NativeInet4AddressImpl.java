/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

/**
 * @see java.net.Inet4AddressImpl
 */
class NativeInet4AddressImpl {
    /**
     * @see java.net.Inet4AddressImpl#getLocalHostName()
     */
    private static String getLocalHostName(Inet4AddressImpl instance) {
        return VMInetAddress.getLocalHostname();
    }
    /**
     * @see java.net.Inet4AddressImpl#lookupAllHostAddr(java.lang.String)
     */
    private static InetAddress[] lookupAllHostAddr(Inet4AddressImpl instance, String hostname)
        throws UnknownHostException {
        return NativeInetAddress.getAllByName0(hostname);
    }
    /**
     * @see java.net.Inet4AddressImpl#getHostByAddr(byte[])
     */
    private static String getHostByAddr(Inet4AddressImpl instance, byte[] arg1) {
        //todo implement it
        if(arg1 != null && arg1.length == 4)
            return arg1[3] + "." + arg1[2] + "." + arg1[1] + "." + arg1[0]; 
        else
            return null;
        //throw new UnsupportedOperationException();
    }
    /**
     * @see java.net.Inet4AddressImpl#isReachable0(byte[], int, byte[], int)
     */
    private static boolean isReachable0(Inet4AddressImpl instance, byte[] arg1, int arg2, byte[] arg3, int arg4) {
        //todo implement it
        //return false;
        throw new UnsupportedOperationException();
    }
}
