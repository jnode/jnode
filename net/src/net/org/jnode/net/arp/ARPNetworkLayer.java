/*
 * $Id$
 */
package org.jnode.net.arp;

import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.InvalidLayerException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.util.Statistics;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class ARPNetworkLayer implements NetworkLayer, ARPConstants {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** My statistics */
	private final ARPStatistics stat = new ARPStatistics();
	/** ARP cache */
	private static final ARPCache cache = new ARPCache();
	
	/**
	 * Create a new instance
	 */
	public ARPNetworkLayer() {
	}

	/**
	 * Gets the name of this type
	 */
	public String getName() {
		return "arp";
	}

	/**
	 * Gets the protocol ID this packettype handles
	 */
	public int getProtocolID() {
		return EthernetConstants.ETH_P_ARP;
	}

	/**
	 * Can this packet type process packets received from the given device?
	 */
	public boolean isAllowedForDevice(Device dev) {
		// For all devices
		return true;
	}

	/**
	 * Process a packet that has been received and matches getType()
	 * @param skbuf
	 * @param deviceAPI
	 * @throws SocketException
	 */
	public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI) 
	throws SocketException {
		
		// Update statistics
		stat.ipackets.inc();
		
		final ARPHeader hdr = new ARPHeader(skbuf);
		skbuf.setNetworkLayerHeader(hdr);
		skbuf.pull(hdr.getLength());
		
		// Update the cache
		cache.set(hdr.getSrcHWAddress(), hdr.getSrcPAddress(), true);
		
		// Should we reply?
		switch (hdr.getOperation()) {
			case ARP_REQUEST: 
				processARPRequest(skbuf, hdr, deviceAPI); break;
			case ARP_REPLY:
				processARPReply(skbuf, hdr, deviceAPI); break;
			case RARP_REQUEST: 
				processRARPRequest(skbuf, hdr, deviceAPI); break;
			case RARP_REPLY:
				processRARPReply(skbuf, hdr, deviceAPI); break;
			default: {
				log.debug("Unknown ARP operation " + hdr.getOperation()); 
			}
		}
	}
	
	/**
	 * Process and ARP request.
	 * @param skbuf
	 * @param hdr
	 * @param deviceAPI
	 * @throws NetworkException
	 */
	private void processARPRequest(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI) 
	throws SocketException {
		
		final ProtocolAddressInfo addrInfo = deviceAPI.getProtocolAddressInfo(hdr.getPType());
		if ((addrInfo != null) && (addrInfo.contains(hdr.getTargetPAddress()))) {
			//log.debug("Sending ARP reply");
			stat.arpreply.inc();
			stat.opackets.inc();
			hdr.swapAddresses();
			hdr.setSrcHWAddress(deviceAPI.getAddress());
			hdr.setOperation(ARP_REPLY);
			skbuf.clear();
			skbuf.setProtocolID(getProtocolID());
			hdr.prefixTo(skbuf);
			deviceAPI.transmit(skbuf, hdr.getTargetHWAddress());
		} else {
			//log.debug("ARP request, not my IP-address");				
		}
	}

	/**
	 * Process and ARP reply
	 * @param skbuf
	 * @param hdr
	 * @param deviceAPI
	 * @throws NetworkException
	 */
	private void processARPReply(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI) 
	throws SocketException {
		// Nothing further todo
	}
	
	/**
	 * Process and RARP request
	 * @param skbuf
	 * @param hdr
	 * @param deviceAPI
	 * @throws NetworkException
	 */
	private void processRARPRequest(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI) 
	throws SocketException {
		stat.rarpreq.inc();
		log.debug("GOT RARP Request");
	}
	
	/**
	 * Process and RARP reply
	 * @param skbuf
	 * @param hdr
	 * @param deviceAPI
	 * @throws NetworkException
	 */
	private void processRARPReply(SocketBuffer skbuf, ARPHeader hdr, NetDeviceAPI deviceAPI) 
	throws SocketException {
		log.debug("GOT RARP Reply");
}
	
	/**
	 * Gets the ARP cache.
	 */
	public ARPCache getCache() {
		return cache;
	}

	/**
	 * @see org.jnode.net.NetworkLayer#getStatistics()
	 */
	public Statistics getStatistics() {
		return stat;
	}

	/**
	 * @see org.jnode.net.NetworkLayer#registerTransportLayer(org.jnode.net.TransportLayer)
	 */
	public void registerTransportLayer(TransportLayer layer)
	throws LayerAlreadyRegisteredException, InvalidLayerException {
		throw new InvalidLayerException("ARP cannot register transportlayers");
	}

	/**
	 * @see org.jnode.net.NetworkLayer#unregisterTransportLayer(org.jnode.net.TransportLayer)
	 */
	public void unregisterTransportLayer(TransportLayer layer) {
		// Just ignore
	}
	
	/**
	 * Gets all registered transport-layers
	 */
	public Collection getTransportLayers() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Gets a registered transportlayer by its protocol ID.
	 * @param protocolID
	 */
	public TransportLayer getTransportLayer(int protocolID)
	throws NoSuchProtocolException {
		throw new NoSuchProtocolException("protocol " + protocolID);
	}
	
	/**
	 * Gets the hardware address for a given protocol address.
	 * @param address
	 * @param myAddress
	 * @param device
	 * @param timeout
	 * @throws TimeoutException
	 * @throws NetworkException
	 */
	public HardwareAddress getHardwareAddress(ProtocolAddress address, ProtocolAddress myAddress, Device device, long timeout)
	throws TimeoutException, NetworkException {
		final long start = System.currentTimeMillis();
		long lastReq = 0;
		
		if (address.equals(myAddress)) {
			// This is simple, just return my address
			return getAPI(device).getAddress();
		}
		
		while (true) {
			final HardwareAddress hwAddress = cache.get(address);
			if (hwAddress != null) {
				return hwAddress;
			}
			final long now = System.currentTimeMillis();
			if ((now - start) >= timeout) {
				// Still not correct response
				throw new TimeoutException("Timeout in ARP request");
			}
			// Try to send a request every few seconds
			if ((now - lastReq) >= ARP_REQUEST_DELAY) {
				lastReq = now;
				request(address, myAddress, device);
			} else {
				cache.waitForChanges(Math.min(timeout, ARP_REQUEST_DELAY));
			}
		}
		
	}

	/**
	 * Create and transmit an ARP request
	 * @param address
	 * @param myAddress
	 * @param device
	 */	
	private void request(ProtocolAddress address, ProtocolAddress myAddress, Device device) 
	throws NetworkException {
		// Not found in the cache, make a request
		final NetDeviceAPI api = getAPI(device);
		final HardwareAddress srcHwAddr = api.getAddress();
		final HardwareAddress trgHwAddr = srcHwAddr.getDefaultBroadcastAddress();
		final int op = ARP_REQUEST;
		final int hwtype = srcHwAddr.getType();
		final int ptype = address.getType();
		
		final ARPHeader hdr = new ARPHeader(srcHwAddr, myAddress, trgHwAddr, address, op, hwtype, ptype);
		final SocketBuffer skbuf = new SocketBuffer();
		skbuf.setProtocolID(EthernetConstants.ETH_P_ARP);
		hdr.prefixTo(skbuf);
		
		api.transmit(skbuf, trgHwAddr);
	}
	
	/**
	 * Gets the NetDeviceAPI for a given device
	 * @param device
	 */
	private NetDeviceAPI getAPI(Device device) {
		try {
			return (NetDeviceAPI)device.getAPI(NetDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new IllegalArgumentException("Not a network device " + device.getId());
		}
	}
}
