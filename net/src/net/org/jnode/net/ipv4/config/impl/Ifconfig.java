/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;

/**
 * Utility class for implementing ifconfig.
 * @author epr
 */
final class Ifconfig {
	
	/**
	 * Sets the default IP address of a network device
	 * @param device
	 * @param address
	 * @param netmask
	 */
	public static void setDefault(Device device, IPv4Address address, IPv4Address netmask) 
	throws NetworkException {
		final NetDeviceAPI api;
		try {
			api = (NetDeviceAPI)device.getAPI(NetDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new NetworkException("Device is not a network device", ex);
		}

		if (netmask == null) {
			netmask = address.getDefaultSubnetmask();
		}
		IPv4ProtocolAddressInfo addrInfo = (IPv4ProtocolAddressInfo)api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
		if (addrInfo == null) {
			addrInfo = new IPv4ProtocolAddressInfo(address, netmask);
			api.setProtocolAddressInfo(EthernetConstants.ETH_P_IP, addrInfo);
		} else {
			addrInfo.add(address, netmask);
			addrInfo.setDefaultAddress(address, netmask);
		}
	}
	
	/**
	 * Gets the defauly address of the first configured network device.
	 * @return The local address
	 * @throws UnknownHostException No local address could be found
	 */
	public static IPv4Address getLocalAddress() 
	throws UnknownHostException {
		final Collection devices = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class);
		for (Iterator i = devices.iterator(); i.hasNext(); ) {
			final Device dev = (Device)i.next();
			try {
				final NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
				final IPv4ProtocolAddressInfo addrInfo = (IPv4ProtocolAddressInfo)api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
				if (addrInfo != null) {
					final IPv4Address addr = (IPv4Address)addrInfo.getDefaultAddress();
					if (addr != null) {
						return addr;
					}
				}
			} catch (ApiNotFoundException ex) {
				// Strange, but ignore
			}
		}
		throw new UnknownHostException("No configured address found");
	}

}
