/*
 * $Id$
 */
package org.jnode.test.net;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.util.NetUtils;

/**
 * @author epr
 */
public class ARPTest {
	
	/**
	 * Perform an ARP request
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) 
	throws Exception {
		
		final ARPNetworkLayer arp = (ARPNetworkLayer)NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_ARP);
		final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
		final IPv4Address addr = new IPv4Address(args[0]);
		final IPv4Address myAddr = new IPv4Address(args[1]);
		final Device dev = dm.getDevice(args[2]);
		final long timeout = 5000;
		
		final HardwareAddress hwAddr;
		hwAddr = arp.getHardwareAddress(addr, myAddr, dev, timeout);
		
		System.out.println("Found hwaddress:" + hwAddr);		
		
	}

}
