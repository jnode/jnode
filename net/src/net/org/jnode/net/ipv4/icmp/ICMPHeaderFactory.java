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
 
package org.jnode.net.ipv4.icmp;

import java.net.SocketException;

import org.jnode.net.SocketBuffer;

public class ICMPHeaderFactory {
	
	/**
     * Create a type specific ICMP header. The type is read from the first first
     * in the skbuf.
     * 
     * @param skbuf
     * @throws SocketException
     */
    public static ICMPHeader createHeader(SocketBuffer skbuf) throws SocketException {
        final ICMPType type = ICMPType.getType(skbuf.get(0));
        switch (type) {
            case ICMP_DEST_UNREACH:
                return new ICMPUnreachableHeader(skbuf);

            case ICMP_TIMESTAMP:
            case ICMP_TIMESTAMPREPLY:
                return new ICMPTimestampHeader(skbuf);

            case ICMP_ADDRESS:
            case ICMP_ADDRESSREPLY:
                return new ICMPAddressMaskHeader(skbuf);

            case ICMP_ECHOREPLY:
            case ICMP_ECHO:
                return new ICMPEchoHeader(skbuf);

            case ICMP_SOURCE_QUENCH:
            case ICMP_REDIRECT:
            case ICMP_TIME_EXCEEDED:
            case ICMP_PARAMETERPROB:
            case ICMP_INFO_REQUEST:
            case ICMP_INFO_REPLY:
                throw new SocketException("Not implemented");
            default:
                throw new SocketException("Unknown ICMP type " + type);
        }
    }

}
