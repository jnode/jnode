/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.util.Ifconfig;
import org.jnode.net.ipv4.util.Route;

/**
 * @author epr
 */
public class BOOTPClient {
	
	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private static final int RECEIVE_TIMEOUT = 10*1000; // 10 seconds
	
	/**
	 * Configure the given device using BOOTP
	 * @param device
	 */
	public void configureDevice(Device device) 
	throws IOException {
		
		// Get the API.
		final NetDeviceAPI api;
		try {
			api = (NetDeviceAPI)device.getAPI(NetDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new NetworkException("Device is not a network device", ex);
		}
		
		// Create the BOOTP header
		final BOOTPHeader hdr;
		final IPv4Address myIp = IPv4Address.ANY;
		final int transactionID = (int)(System.currentTimeMillis() & 0xFFFFFFFF);
		hdr = new BOOTPHeader(1, transactionID, myIp, api.getAddress());

		// Open a socket		
		final MulticastSocket socket = new MulticastSocket(68);
		try {
			// Prepare the socket			
			socket.setBroadcast(true);
			socket.setNetworkInterface(NetworkInterface.getByName(device.getId()));
			socket.setSoTimeout(RECEIVE_TIMEOUT);

			// Send the packet
			final DatagramPacket packet = hdr.asDatagramPacket();
			packet.setAddress(IPv4Address.BROADCAST.toInetAddress());
			packet.setPort(67);
			socket.send(packet);
			
			boolean configured;
			do {
				// Wait for a response
				socket.receive(packet);
			
				// Process the response
				configured = processResponse(device, api, transactionID, packet);
			} while (!configured);
						
		} finally {
			socket.close();
		}
				
	}
	
	/**
	 * Process a BOOTP response
	 * @param packet
	 * @return true if the device has been configured, false otherwise
	 */
	private boolean processResponse(Device device, NetDeviceAPI api, int transactionID, DatagramPacket packet) 
	throws NetworkException {
		
		final BOOTPHeader hdr = new BOOTPHeader(packet);
		if (hdr.getOpcode() != 2) {
			// Not a response
			return false;
		}
		if (hdr.getTransactionID() != transactionID) {
			// Not for me
			return false;
		}
		
		log.info("Got Client IP address  : " + hdr.getClientIPAddress());
		log.info("Got Your IP address    : " + hdr.getYourIPAddress());
		log.info("Got Server IP address  : " + hdr.getServerIPAddress());
		log.info("Got Gateway IP address : " + hdr.getGatewayIPAddress());
		
		Ifconfig.setDefault(device, hdr.getYourIPAddress(), null);
		if (hdr.getGatewayIPAddress().isAny()) {
			Route.addRoute(hdr.getServerIPAddress(), null, device); 
		} else {
			Route.addRoute(hdr.getServerIPAddress(), hdr.getGatewayIPAddress(), device); 
		}

		return true;		
	}

}
