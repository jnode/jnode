/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import java.util.Iterator;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4Route;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.util.NetUtils;

/**
 * @author epr
 */
final class Route {

	/**
	 * Add a route
	 * @param target
	 * @param gateway
	 * @param device
	 * @throws NetworkException
	 */
	public static void addRoute(IPv4Address target, IPv4Address gateway, Device device) 
	throws NetworkException {
		
		if (device == null) {
			// Find the device ourselve
			final DeviceManager dm;
			try {
				dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			} catch (NameNotFoundException ex) {
				throw new NetworkException("Cannot find DeviceManager", ex);
			}
			device = findDevice(dm, target, target.getDefaultSubnetmask());
		}
		
		final IPv4NetworkLayer ipNL;
		try {
			ipNL = (IPv4NetworkLayer)NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_IP);
		} catch (NoSuchProtocolException ex) {
			throw new NetworkException("Cannot find IPv4 network layer", ex);
		}
		final IPv4RoutingTable rt = ipNL.getRoutingTable();
		rt.add(new IPv4Route(target, null, gateway, device));
	}
	
	/**
	 * Delete a route
	 * @param target
	 * @param gateway
	 * @param device
	 * @throws NetworkException
	 */
	public static void delRoute(IPv4Address target, IPv4Address gateway, Device device) 
	throws NetworkException {
		final IPv4NetworkLayer ipNL;
		try {
			ipNL = (IPv4NetworkLayer)NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_IP);
		} catch (NoSuchProtocolException ex) {
			throw new NetworkException("Cannot find IPv4 network layer", ex);
		}
		final IPv4RoutingTable rt = ipNL.getRoutingTable();
		
		for (Iterator i = rt.entries().iterator(); i.hasNext(); ) {
			final IPv4Route route = (IPv4Route)i.next();
			if (!route.getDestination().equals(target)) {
				continue;
			}
			if (gateway != null) {
				if (!gateway.equals(route.getGateway())) {
					continue;
				}
			}
			if (device != null) {
				if (device != route.getDevice()) {
					continue;
				}
			}
			
			rt.remove(route);
			return;
		}
	}
	
	/**
	 * Search for a suitable device for the given target address.
	 * @param dm
	 * @param target
	 * @param mask
	 * @return
	 * @throws NetworkException
	 */
	private static Device findDevice(DeviceManager dm, IPv4Address target, IPv4Address mask) 
	throws NetworkException {
		for (Iterator i = dm.getDevicesByAPI(NetDeviceAPI.class).iterator(); i.hasNext(); ) {
			final Device dev = (Device)i.next();
			try {
				final NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
				final IPv4ProtocolAddressInfo addrInfo;
				addrInfo = (IPv4ProtocolAddressInfo)api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
				if (addrInfo != null) {
					final IPv4Address devAddr = (IPv4Address)addrInfo.getDefaultAddress();
					if (devAddr.matches(target, mask)) {
						return dev;				
					}
				}
			} catch (ApiNotFoundException ex) {
				// Should not happen, but if it happens anyway, just ignore it.
			}
		}
		throw new NetworkException("No device found for " + target + "/" + mask);
	}
	
}
