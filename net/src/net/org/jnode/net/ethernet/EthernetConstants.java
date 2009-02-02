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
 
package org.jnode.net.ethernet;

/**
 * @author epr
 */
public interface EthernetConstants {

    /*
     * IEEE 802.3 Ethernet magic constants. The frame sizes omit the preamble
     * and FCS/CRC (frame check sequence).
     */
    public static final int ETH_ALEN       = 6;     /* Octets in one ethernet addr */
    public static final int ETH_HLEN       = 14;    /* Total octets in header. */
    public static final int ETH_ZLEN       = 60;    /* Min. octets in frame sans FCS */
    public static final int ETH_DATA_LEN   = 1500;  /* Max. octets in payload */
    public static final int ETH_FRAME_LEN  = 1514;  /* Max. octets in frame sans FCS */

    /*
     * These are the defined Ethernet Protocol ID's.
     */
    public static final int ETH_P_LOOP      = 0x0060;   /* Ethernet Loopback packet */
    public static final int ETH_P_PUP       = 0x0200;   /* Xerox PUP packet */
    public static final int ETH_P_PUPAT     = 0x0201;   /* Xerox PUP Addr Trans packet */
    public static final int ETH_P_IP        = 0x0800;   /* Internet Protocol packet */
    public static final int ETH_P_X25       = 0x0805;   /* CCITT X.25 */
    public static final int ETH_P_ARP       = 0x0806;   /* Address Resolution packet */
    public static final int ETH_P_BPQ       = 0x08FF;   /* G8BPQ AX.25 Ethernet Packet [ NOT REGISTERED ] */
    public static final int ETH_P_IEEEPUP   = 0x0a00;   /* Xerox IEEE802.3 PUP packet */
    public static final int ETH_P_IEEEPUPAT = 0x0a01;   /* Xerox IEEE802.3 PUP Addr Trans packet */
    public static final int ETH_P_DEC       = 0x6000;   /* DEC Assigned proto */
    public static final int ETH_P_DNA_DL    = 0x6001;   /* DEC DNA Dump/Load */
    public static final int ETH_P_DNA_RC    = 0x6002;   /* DEC DNA Remote Console */
    public static final int ETH_P_DNA_RT    = 0x6003;   /* DEC DNA Routing */
    public static final int ETH_P_LAT       = 0x6004;   /* DEC LAT */
    public static final int ETH_P_DIAG      = 0x6005;   /* DEC Diagnostics */
    public static final int ETH_P_CUST      = 0x6006;   /* DEC Customer use */
    public static final int ETH_P_SCA       = 0x6007;   /* DEC Systems Comms Arch */
    public static final int ETH_P_RARP      = 0x8035;   /* Reverse Addr Res packet */
    public static final int ETH_P_ATALK     = 0x809B;   /* Appletalk DDP */
    public static final int ETH_P_AARP      = 0x80F3;   /* Appletalk AARP */
    public static final int ETH_P_8021Q     = 0x8100;   /* 802.1Q VLAN Extended Header */
    public static final int ETH_P_IPX       = 0x8137;   /* IPX over DIX */
    public static final int ETH_P_IPV6      = 0x86DD;   /* IPv6 over bluebook */
    public static final int ETH_P_PPP_DISC  = 0x8863;   /* PPPoE discovery messages */
    public static final int ETH_P_PPP_SES   = 0x8864;   /* PPPoE session messages */
    public static final int ETH_P_ATMMPOA   = 0x884c;   /* MultiProtocol Over ATM */
    public static final int ETH_P_ATMFATE   = 0x8884;   /* Frame-based ATM Transport over Ethernet */

    /*
     * Non DIX types. Won't clash for 1500 types.
     */
    public static final int ETH_P_802_3     = 0x0001;   /* Dummy type for 802.3 frames */
    public static final int ETH_P_AX25      = 0x0002;   /* Dummy protocol id for AX.25 */
    public static final int ETH_P_ALL       = 0x0003;   /* Every packet (be careful!!!) */
    public static final int ETH_P_802_2     = 0x0004;   /* 802.2 frames */
    public static final int ETH_P_SNAP      = 0x0005;   /* Internal only */
    public static final int ETH_P_DDCMP     = 0x0006;   /* DEC DDCMP: Internal only */
    public static final int ETH_P_WAN_PPP   = 0x0007;   /* Dummy type for WAN PPP frames */
    public static final int ETH_P_PPP_MP    = 0x0008;   /* Dummy type for PPP MP frames */
    public static final int ETH_P_LOCALTALK = 0x0009;   /* Localtalk pseudo type */
    public static final int ETH_P_PPPTALK   = 0x0010;   /* Dummy type for Atalk over PPP */
    public static final int ETH_P_TR_802_2  = 0x0011;   /* 802.2 frames */
    public static final int ETH_P_MOBITEX   = 0x0015;   /* Mobitex (kaz@cafe.net) */
    public static final int ETH_P_CONTROL   = 0x0016;   /* Card specific control frames */
    public static final int ETH_P_IRDA      = 0x0017;   /* Linux-IrDA */
    public static final int ETH_P_ECONET    = 0x0018;   /* Acorn Econet */
}
