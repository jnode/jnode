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
 
package org.jnode.net.ipv4.layer;

import java.net.NoRouteToHostException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4Route;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.util.NetUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class IPv4Sender implements IPv4Constants, EthernetConstants {

    /** The routing table */
    private final IPv4RoutingTable rt;
    /** The ARP service */
    private ARPNetworkLayer arp;
    /** Timeout for arp requests */
    private long arpTimeout = 5000;
    /** Last identification number */
    private int lastId = 1;
    /** My statistics */
    private final IPv4Statistics stat;

    /**
     * Create a new instance
     * 
     * @param ipNetworkLayer
     */
    public IPv4Sender(IPv4NetworkLayer ipNetworkLayer) {
        this.rt = ipNetworkLayer.getRoutingTable();
        this.stat = (IPv4Statistics) ipNetworkLayer.getStatistics();
    }

    /**
     * Transmit an IP packet. The given buffer must contain all packet data AND
     * the header(s) of any IP sub-protocols, before this method is called.
     * 
     * The following fields of the IP header must be set: tos, ttl, protocol,
     * dstAddress. <p/> All other header fields are set, unless they have been
     * set before. <p/> The following fields are always set (also when set
     * before): version, hdrlength, identification, fragmentOffset, checksum
     * <p/> If the device attribute of the skbuf has been set, the packet will
     * be send to this device, otherwise a suitable route will be searched for
     * in the routing table.
     * 
     * @param hdr
     * @param skbuf
     * @throws NoRouteToHostException No suitable route for this packet was
     *             found
     * @throws NetworkException The packet could not be transmitted.
     */
    public void transmit(IPv4Header hdr, SocketBuffer skbuf)
        throws NoRouteToHostException, NetworkException {

        // Set the network layer header
        skbuf.setNetworkLayerHeader(hdr);

        // The destination address must have been set, check it
        if (hdr.getDestination() == null) {
            throw new NetworkException("The destination address must have been set");
        }
        stat.opackets.inc();

        // The device we will use to transmit the packet
        final Device dev;
        final NetDeviceAPI api;
        // The hardware address we will be sending to
        final HardwareAddress hwDstAddr;

        // Has the destination device been given?
        if (skbuf.getDevice() == null) {
            // The device has not been send, figure out the route ourselves.

            // First lets try to find a route
            final IPv4Route route;
            route = findRoute(hdr, skbuf);
            route.incUseCount();

            // Get the device
            dev = route.getDevice();
            api = route.getDeviceAPI();

            // Get my source address if not already set
            if (hdr.getSource() == null) {
                hdr.setSource(getSourceAddress(route, hdr, skbuf));
            }

            // Get the hardware address for this device
            hwDstAddr = findDstHWAddress(route, hdr, skbuf);
        } else {
            // The device has been given, use it
            dev = skbuf.getDevice();
            try {
                api = dev.getAPI(NetDeviceAPI.class);
            } catch (ApiNotFoundException ex) {
                throw new NetworkException("Device is not a network device", ex);
            }
            // The source address must have been set, check it
            if (hdr.getSource() == null) {
                throw new NetworkException("The source address must have been set");
            }
            // Find the HW destination address
            hwDstAddr = findDstHWAddress(hdr.getDestination(), dev, hdr, skbuf);
        }

        // Set the datalength (if not set)
        if (hdr.getDataLength() == 0) {
            hdr.setDataLength(skbuf.getSize());
        }

        // Set the identification number, if not set before
        if (hdr.getIdentification() == 0) {
            hdr.setIdentification(getNextID());
        }

        // Should we fragment?
        final int mtu = api.getMTU();

        if (hdr.getTotalLength() <= mtu) {
            // We can send the complete packet
            hdr.setMoreFragments(false);
            hdr.setFragmentOffset(0);
            sendPacket(api, hwDstAddr, hdr, skbuf);
        } else if (hdr.isDontFragment()) {
            // This packet cannot be send of this device
            throw new NetworkException("Packet is too large, mtu=" + mtu);
        } else {
            // Fragment the packet and send the fragments
            fragmentPacket(api, hwDstAddr, hdr, skbuf, mtu);
        }
    }

    /**
     * Search for a route for the given buffer
     * 
     * @param skbuf
     * @return
     * @throws NoRouteToHostException
     */
    private IPv4Route findRoute(IPv4Header hdr, SocketBuffer skbuf) throws NoRouteToHostException {
        final IPv4Address destination = hdr.getDestination();
        return rt.search(destination);
    }

    /**
     * Gets the source address to use for a given route.
     * 
     * @param route
     * @param hdr
     * @param skbuf
     * @return
     */
    private IPv4Address getSourceAddress(IPv4Route route, IPv4Header hdr, SocketBuffer skbuf)
        throws NetworkException {
        final Object addrInfo = route.getDeviceAPI().getProtocolAddressInfo(ETH_P_IP);
        if (addrInfo == null) {
            throw new NetworkException("Source IP address not configured for device " +
                    route.getDevice().getId());
        }
        if (!(addrInfo instanceof IPv4ProtocolAddressInfo)) {
            throw new NetworkException("Source IP address not valid class for device " +
                    route.getDevice().getId());
        }
        return (IPv4Address) ((IPv4ProtocolAddressInfo) addrInfo).getDefaultAddress();
    }

    /**
     * Find the hardware address for the destination address of the given route.
     * 
     * @param route
     * @return
     */
    private HardwareAddress findDstHWAddress(IPv4Route route, IPv4Header hdr, SocketBuffer skbuf)
        throws NetworkException {
        final ARPNetworkLayer arp = getARP();
        final IPv4Address dstAddr;
        if (hdr.getDestination().isBroadcast()) {
            return null;
        } else if (route.isGateway()) {
            dstAddr = route.getGateway();
        } else {
            dstAddr = hdr.getDestination();
        }
        try {
            return arp.getHardwareAddress(dstAddr, hdr.getSource(), route.getDevice(), arpTimeout);
        } catch (TimeoutException ex) {
            throw new NetworkException("Cannot find hardware address of " + dstAddr, ex);
        }
    }

    /**
     * Find the hardware address for the destination address of the given route.
     * 
     * @param destination
     * @param device
     * @param hdr
     * @param skbuf
     * @return HardwareAddress
     */
    private HardwareAddress findDstHWAddress(IPv4Address destination, Device device,
            IPv4Header hdr, SocketBuffer skbuf) throws NetworkException {
        final ARPNetworkLayer arp = getARP();
        if (destination.isBroadcast()) {
            return null;
        } else {
            try {
                return arp.getHardwareAddress(destination, hdr.getSource(), device, arpTimeout);
            } catch (TimeoutException ex) {
                throw new NetworkException("Cannot find hardware address of " + destination, ex);
            }
        }
    }

    /**
     * Insert the IP header into the buffer and send it to the device.
     * 
     * @param api
     * @param hdr
     * @param skbuf
     * @throws NetworkException
     */
    private void sendPacket(NetDeviceAPI api, HardwareAddress dstHwAddr, IPv4Header hdr,
            SocketBuffer skbuf) throws NetworkException {
        skbuf.setProtocolID(ETH_P_IP);
        hdr.prefixTo(skbuf);
        api.transmit(skbuf, dstHwAddr);
    }

    /**
     * Fragment the packet and send the to the device
     * 
     * @param api
     * @param hdr
     * @param skbuf
     * @throws NetworkException
     */
    private void fragmentPacket(NetDeviceAPI api, HardwareAddress dstHwAddr, IPv4Header hdr,
            SocketBuffer skbuf, int mtu) throws NetworkException {
        if ((hdr.getLength() + IP_MIN_FRAG_SIZE) > mtu) {
            throw new NetworkException("MTU is too small for IP, mtu=" + mtu);
        }

        // The complete packet
        final byte[] packet = skbuf.toByteArray();
        int length = packet.length;
        int offset = 0;
        // Size of a single fragment
        final int maxFragSize = (mtu - hdr.getLength()) & ~IP_MIN_FRAG_SIZE;

        // Now create the fragmented packets and send them
        while (length > 0) {
            final int fragLen = Math.min(maxFragSize, length);
            final SocketBuffer fBuf = new SocketBuffer(packet, offset, fragLen);
            hdr.setFragmentOffset(offset);
            hdr.setMoreFragments((length - fragLen) > 0);
            hdr.setDataLength(fragLen);
            sendPacket(api, dstHwAddr, hdr, fBuf);
            offset += fragLen;
            length -= fragLen;
        }
    }

    /**
     * Gets the ARP service
     * @return
     */
    private ARPNetworkLayer getARP() throws NetworkException {
        if (arp == null) {
            try {
                arp = (ARPNetworkLayer) NetUtils.getNLM().getNetworkLayer(
                                EthernetConstants.ETH_P_ARP);
            } catch (NoSuchProtocolException ex) {
                throw new NetworkException("Cannot find ARP layer", ex);
            }
        }
        return arp;
    }

    /**
     * Gets a unique identification number
     * @return
     */
    private synchronized int getNextID() {
        lastId++;
        if (lastId > 0xFFFF) {
            lastId = 0;
        }
        return lastId;
    }
}
