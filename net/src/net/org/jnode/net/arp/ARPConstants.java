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
 

package org.jnode.net.arp;

/**
 * @author epr
 */
public interface ARPConstants {

    public static final int ARP_REQUEST = 1;
    public static final int ARP_REPLY = 2;
    public static final int RARP_REQUEST = 3;
    public static final int RARP_REPLY = 4;

    /** Delay between ARP requests */
    public static final int ARP_REQUEST_DELAY = 1500;
}
