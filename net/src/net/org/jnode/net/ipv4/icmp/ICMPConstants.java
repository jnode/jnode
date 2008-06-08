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

/**
 * @author epr
     */
public interface ICMPConstants {

    public static final int ICMP_ECHOREPLY      = 0; /* Echo Reply */
    public static final int ICMP_DEST_UNREACH   = 3; /* Destination Unreachable */
    public static final int ICMP_SOURCE_QUENCH  = 4; /* Source Quench */
    public static final int ICMP_REDIRECT       = 5; /* Redirect (change route) */
    public static final int ICMP_ECHO           = 8;  /* Echo Request */
    public static final int ICMP_TIME_EXCEEDED  = 11; /* Time Exceeded */
    public static final int ICMP_PARAMETERPROB  = 12; /* Parameter Problem */
    public static final int ICMP_TIMESTAMP      = 13; /* Timestamp Request */
    public static final int ICMP_TIMESTAMPREPLY = 14; /* Timestamp Reply */
    public static final int ICMP_INFO_REQUEST   = 15; /* Information Request */
    public static final int ICMP_INFO_REPLY     = 16; /* Information Reply */
    public static final int ICMP_ADDRESS        = 17; /* Address Mask Request */
    public static final int ICMP_ADDRESSREPLY   = 18; /* Address Mask Reply */
    public static final int NR_ICMP_TYPES       = 18;


    /* Codes for UNREACH. */
    public static final int ICMP_NET_UNREACH    = 0;  /* Network Unreachable */
    public static final int ICMP_HOST_UNREACH   = 1;  /* Host Unreachable */
    public static final int ICMP_PROT_UNREACH   = 2;  /* Protocol Unreachable */
    public static final int ICMP_PORT_UNREACH   = 3;  /* Port Unreachable */
    public static final int ICMP_FRAG_NEEDED    = 4;  /* Fragmentation Needed/DF set */
    public static final int ICMP_SR_FAILED      = 5;  /* Source Route failed */
    public static final int ICMP_NET_UNKNOWN    = 6;
    public static final int ICMP_HOST_UNKNOWN   = 7;
    public static final int ICMP_HOST_ISOLATED  = 8;
    public static final int ICMP_NET_ANO        = 9;
    public static final int ICMP_HOST_ANO       = 10;
    public static final int ICMP_NET_UNR_TOS    = 11;
    public static final int ICMP_HOST_UNR_TOS   = 12;
    public static final int ICMP_PKT_FILTERED   = 13; /* Packet filtered */
    public static final int ICMP_PREC_VIOLATION = 14; /* Precedence violation */
    public static final int ICMP_PREC_CUTOFF    = 15; /* Precedence cut off */
    public static final int NR_ICMP_UNREACH     = 15; /* instead of hardcoding immediate value */

    /* Codes for REDIRECT. */
    public static final int ICMP_REDIR_NET      = 0; /* Redirect Net */
    public static final int ICMP_REDIR_HOST     = 1; /* Redirect Host */
    public static final int ICMP_REDIR_NETTOS   = 2; /* Redirect Net for TOS */
    public static final int ICMP_REDIR_HOSTTOS  = 3; /* Redirect Host for TOS */

    /* Codes for TIME_EXCEEDED. */
    public static final int ICMP_EXC_TTL        = 0; /* TTL count exceeded */
    public static final int ICMP_EXC_FRAGTIME   = 1; /* Fragment Reass time exceeded */

}
