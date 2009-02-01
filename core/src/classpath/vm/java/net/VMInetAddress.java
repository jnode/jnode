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

import java.io.Serializable;
import java.util.StringTokenizer;

final class VMInetAddress implements Serializable {

    /**
     * This method looks up the hostname of the local machine we are on. If the
     * actual hostname cannot be determined, then the value "localhost" will be
     * used. This native method wrappers the "gethostname" function.
     * 
     * @return The local hostname.
     */
    public static String getLocalHostname() {
        return "localhost";
    }

    /**
     * Returns the value of the special address INADDR_ANY
     */
    public static byte[] lookupInaddrAny() throws UnknownHostException {
        return new byte[] { 0, 0, 0, 0 };
    }

    /**
     * This method returns the hostname for a given IP address. It will throw an
     * UnknownHostException if the hostname cannot be determined.
     * 
     * @param ip
     *            The IP address as a byte array
     * @return The hostname
     * @exception UnknownHostException
     *                If the reverse lookup fails
     */
    public static String getHostByAddr(byte[] ip) throws UnknownHostException {
        return VMNetUtils.getAPI().getHostByAddr(ip);
    }

    /**
     * Returns a list of all IP addresses for a given hostname. Will throw an
     * UnknownHostException if the hostname cannot be resolved.
     */
    public static byte[][] getHostByName(String hostname)
            throws UnknownHostException {
        // Test for the local hostname
        if (hostname.equals(getLocalHostname())) {
            final byte[][] result = new byte[1][];
            result[0] = VMNetUtils.getAPI().getLocalAddress().getAddress();
            return result;
        }
        // Test for an IP address (a.b.c.d)
        final byte[] addr = getHostnameAsAddress(hostname);
        if (addr != null) {
            final byte[][] result = new byte[1][];
            result[0] = addr;
            return result;
        }
        // Lookup the hostname
        return VMNetUtils.getAPI().getHostByName(hostname);
    }
    
    /**
     * Test if the hostname is an IP address and if so returns the address.
     * @param hostname
     * @return The ip address, or null if it is not an IP address.
     */
    private static byte[] getHostnameAsAddress(String hostname) {
        final StringTokenizer tok = new StringTokenizer(hostname, ".");
        if (tok.countTokens() != 4) {
            return null;
        }
        try {
            final byte b1 = parseUnsignedByte(tok.nextToken());
            final byte b2 = parseUnsignedByte(tok.nextToken());
            final byte b3 = parseUnsignedByte(tok.nextToken());
            final byte b4 = parseUnsignedByte(tok.nextToken());
            return new byte[] { b1, b2, b3, b4 };
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    /**
     * Parse a string that is supported to b an unsigned byte.
     * @param str
     * @throws NumberFormatException 
     * @return
     */
    private static byte parseUnsignedByte(String str) {
        final int v = Integer.parseInt(str);
        if ((v >= 0) && (v < 256)) {
            return (byte)v;
        } else {
            throw new NumberFormatException(str);
        }
    }
}
