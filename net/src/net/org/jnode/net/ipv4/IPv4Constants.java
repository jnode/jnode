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
 
package org.jnode.net.ipv4;

/**
 * @author epr
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public interface IPv4Constants {

    // Well known protocols
    public static final int IPPROTO_IP       = 0;       /* Dummy protocol for TCP */
    public static final int IPPROTO_ICMP     = 1;       /* Internet Control Message Protocol */
    public static final int IPPROTO_IGMP     = 2;       /* Internet Group Management Protocol */
    public static final int IPPROTO_IPIP     = 4;       /* IPIP tunnels (older KA9Q tunnels use 94) */
    public static final int IPPROTO_TCP      = 6;       /* Transmission Control Protocol */
    public static final int IPPROTO_EGP      = 8;       /* Exterior Gateway Protocol */
    public static final int IPPROTO_PUP      = 12;      /* PUP protocol */
    public static final int IPPROTO_UDP      = 17;      /* User Datagram Protocol */
    public static final int IPPROTO_IDP      = 22;      /* XNS IDP protocol */
    public static final int IPPROTO_RSVP     = 46;      /* RSVP protocol */
    public static final int IPPROTO_GRE      = 47;      /* Cisco GRE tunnels (rfc 1701,1702) */

    public static final int IPPROTO_IPv6     = 41;      /* IPv6-in-IPv4 tunnelling */

    public static final int IPPROTO_PIM      = 103;     /* Protocol Independent Multicast */

    public static final int IPPROTO_ESP      = 50;      /* Encapsulation Security Payload protocol */
    public static final int IPPROTO_AH       = 51;      /* Authentication Header protocol */
    public static final int IPPROTO_COMP     = 108;     /* Compression Header protocol */

    public static final int IPPROTO_RAW      = 255;     /* Raw IP packets */

    // Fragmentation bits
    public static final int IP_MF            = 0x2000;  /* More fragment will follow */
    public static final int IP_DF            = 0x4000;  /* Don't fragment */
    public static final int IP_FRAGOFS_MASK  = 0x1FFF;  /* Mask to get fragment offset */
    
    public static final long IP_FRAGTIMEOUT  = 120000;  /* Number of ms till a fragment list is timed out. */
    
    public static final int IP_MIN_FRAG_SIZE = 8;       /* Minimum size of a fragment */

    // Route flags
    public static final int RTF_UP           = 0x0001;  /* route usable */
    public static final int RTF_GATEWAY      = 0x0002;  /* destination is a gateway */
    public static final int RTF_HOST         = 0x0004;  /* host entry (net otherwise) */
    public static final int RTF_REINSTATE    = 0x0008;  /* reinstate route after timeout */
    public static final int RTF_DYNAMIC      = 0x0010;  /* created dyn. (by redirect) */
    public static final int RTF_MODIFIED     = 0x0020;  /* modified dyn. (by redirect) */
    public static final int RTF_MTU          = 0x0040;  /* specific MTU for this route */
    public static final int RTF_MSS          = RTF_MTU; /* Compatibility :-( */
    public static final int RTF_WINDOW       = 0x0080;  /* per route window clamping */
    public static final int RTF_IRTT         = 0x0100;  /* Initial round trip time */
    public static final int RTF_REJECT       = 0x0200;  /* Reject route*/

    // Port ranges
    public static final int IPPORT_RESERVED     = 1024;
    public static final int IPPORT_USERRESERVED = 5000;

    // Network classes upper limits
    public static final int NETWORK_CLASSA   = 128;     /*   0.0.0.0 - 127.255.255.255  x.0.0.0  255.0.0.0 */
    public static final int NETWORK_CLASSB   = 192;     /* 128.0.0.0 - 191.255.255.255  x.x.0.0  255.255.0.0 */
    public static final int NETWORK_CLASSC   = 224;     /* 192.0.0.0 - 223.255.255.255  x.x.x.0  255.255.255.0 */
}
