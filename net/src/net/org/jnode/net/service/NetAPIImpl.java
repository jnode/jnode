/*
 * $Id$
 */
package org.jnode.net.service;

import java.net.InetAddress;
import java.net.VMNetAPI;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.util.Ifconfig;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetAPIImpl implements VMNetAPI {

	/**
	 * @see java.net.NetAPI#getInetAddresses(org.jnode.driver.Device)
	 */
	public List getInetAddresses(Device netDevice) {
		final Vector list = new Vector(1, 1);
		final SecurityManager sm = System.getSecurityManager();
		try {
			final NetDeviceAPI api = (NetDeviceAPI)netDevice.getAPI(NetDeviceAPI.class);
			final IPv4ProtocolAddressInfo info = (IPv4ProtocolAddressInfo)api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
			
			for (Iterator i = info.addresses().iterator(); i.hasNext(); ) {
				final IPv4Address ipaddr = (IPv4Address)i.next();
				if (sm != null) {
					try {
						sm.checkConnect(ipaddr.toString(), 58000);
						list.add(ipaddr.toInetAddress());
					} catch (SecurityException ex) {
						// Just don't add
					}
				} else {
					list.add(ipaddr.toInetAddress());
				}
			}
		} catch (ApiNotFoundException ex) {
			// Ignore
		}
		return list;
	}
	
	/**
	 * Is the given device a network device.
	 * @param device
	 * @return boolean
	 */
	public boolean isNetDevice(Device device) {
		return device.implementsAPI(NetDeviceAPI.class);
	}
	
	
	/**
	 *  Return a network device by its address
	 *
	 *  @param addr The address of the interface to return
	 *
	 *  @exception SocketException If an error occurs
	 *  @exception NullPointerException If the specified addess is null
	 */
	public Device getByInetAddress(InetAddress addr)
	throws SocketException {
		final IPv4Address ipaddr = new IPv4Address(addr);
		
		for (Iterator i = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class).iterator(); i.hasNext(); ) {
			final Device dev = (Device)i.next();
			try {
				final NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
				final IPv4ProtocolAddressInfo info = (IPv4ProtocolAddressInfo)api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
				if (info.contains(ipaddr)) {
					return dev;
				}
			} catch (ApiNotFoundException ex) {
				// Ignore
			}
		}
		throw new SocketException("no network interface is bound to such an IP address");
	}
	
	/**
	 * Gets all net devices.
	 * @return A list of Device instances.
	 */
	public Collection getNetDevices() {
		return DeviceUtils.getDevicesByAPI(NetDeviceAPI.class);
	}
	
	/**
	 * Gets the default local address.
	 * @return InetAddress
	 */
	public InetAddress getLocalAddress() throws UnknownHostException {
		return Ifconfig.getLocalAddress().toInetAddress();
	}
}
