/*
 * $Id$
 */
package org.jnode.test.net;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;

/**
 * @author epr
 */
public class NetTest {

	public static void main(String[] args) {
		
		String devId = (args.length > 0) ? args[0] : "eth0";
		
		try {
			final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			final Device dev = dm.getDevice(devId);
			final NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
			final EthernetAddress mac = (EthernetAddress)api.getAddress();
			
			SocketBuffer skbuf = new SocketBuffer();
			skbuf.insert(28);
			skbuf.set16(0, 0x0001);	// Hardware type
			skbuf.set16(2, 0x0800); // Protocol type
			skbuf.set(4, 6);		// Hardware address size
			skbuf.set(5, 4);		// Protocol address size
			skbuf.set16(6, 0x01);	// Operation APR request
			mac.writeTo(skbuf, 8);	// Source mac
			skbuf.set32(14, 0xc0a8c853); // Source IP
			skbuf.set32(14, 0xc0a8c801); // Target IP
			
			// Prefix ethernet header
			skbuf.insert(14);
			// Set dest address
			EthernetAddress dst = new EthernetAddress("ff-ff-ff-ff-ff-ff");
			dst.writeTo(skbuf, 0);
			
			// Set packet type
			skbuf.set16(12, 0x0806);
			
			//api.transmit(skbuf);
			
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
	}

}
