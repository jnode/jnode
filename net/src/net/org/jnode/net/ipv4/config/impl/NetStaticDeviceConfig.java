/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetStaticDeviceConfig extends NetDeviceConfig {

    private IPv4Address address; 
    private IPv4Address netmask;       
    
    /**
     * Initialize this instance.
     * @param address
     * @param netmask
     */
    public NetStaticDeviceConfig(IPv4Address address, IPv4Address netmask) {
        this.address = address;
        this.netmask = netmask;
    }
    
    /**
     * @throws NetworkException
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#apply(Device)
     */
    public void doApply(Device device) throws NetworkException {
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
}
