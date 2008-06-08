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
 
package org.jnode.net.ipv4.icmp;

import org.jnode.net.ipv4.IPv4Address;

/**
 * @author JPG
 */
public class ICMPListenerCriteria implements ICMPConstants {
    IPv4Address src;
    int icmp_type;

    public static final int ANY_TYPE = -1;

    public ICMPListenerCriteria(IPv4Address src) {
        this.src = src;
        this.icmp_type = ANY_TYPE;
    }

    public ICMPListenerCriteria(IPv4Address src, int icmp_type) {
        this.src = src;
        this.icmp_type = icmp_type;
    }

    public ICMPListenerCriteria(int icmp_type) {
        this.icmp_type = icmp_type;
    }

    public boolean equals(Object obj) {
        // FIXME ... this method throws class-cast if obj is not a listener
        // criteria instance.  This violates the 'contract' for equals!!!
        ICMPListenerCriteria c = (ICMPListenerCriteria) obj;

        boolean result = true;

        if (src != null)
            result = result & src.equals(c.getSrc());
        if (icmp_type != ANY_TYPE)
            result = result & icmp_type == c.getIcmpType();

        return result;
    }

    public IPv4Address getSrc() {
        return src;
    }

    public int getIcmpType() {
        return icmp_type;
    }
}
