/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import nanoxml.XMLElement;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author markhale
 */
public class BOOTPServer {

	private final Logger log = Logger.getLogger(getClass());

	public static final int SERVER_PORT = 67;
	public static final int CLIENT_PORT = 68;

	private DatagramSocket socket;
	private final Map table = new HashMap();

	public static void main(String[] args) {
		BOOTPServer server = new BOOTPServer();
		try {
			server.loadTable();
			server.run();
		} catch(IOException ex) {
			Logger.getLogger(BOOTPServer.class).debug("I/O exception", ex);
		}
	}

	private void loadTable() throws IOException {
		FileReader reader = new FileReader("bootp.xml");
		try {
			XMLElement xml = new XMLElement();
			xml.parseFromReader(reader);
			Vector children = xml.getChildren();
			for(int i=0; i<children.size(); i++) {
				XMLElement child = (XMLElement) children.get(i);
				table.put(child.getAttribute("ethernetAddress"), child.getAttribute("ipAddress"));
			}
		} finally {
			reader.close();
		}
	}

	private void run() throws SocketException {
		System.out.println("JNode BOOTP Server");
		socket = new DatagramSocket(SERVER_PORT);
		try {
			socket.setBroadcast(true);

			final byte[] buffer = new byte[BOOTPMessage.SIZE];
			final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			boolean doShutdown = false;
			while(!doShutdown) {
				try {
					socket.receive(packet);
					processRequest(packet);
				} catch(IOException ex) {
					log.debug("I/O exception", ex);
				}
			}
		} finally {
			socket.close();
		}
	}

	private void processRequest(DatagramPacket packet) throws IOException {
		BOOTPHeader hdr = new BOOTPHeader(packet);
		if (hdr.getOpcode() != BOOTPHeader.BOOTREQUEST) {
			// Not a request
			return;
		}

		log.debug("Got Client IP address  : " + hdr.getClientIPAddress());
		log.debug("Got Your IP address    : " + hdr.getYourIPAddress());
		log.debug("Got Server IP address  : " + hdr.getServerIPAddress());
		log.debug("Got Gateway IP address : " + hdr.getGatewayIPAddress());

		String hostIP = (String) table.get(hdr.getClientHwAddress().toString());
		if(hostIP == null) {
			// no host entry in table
			return;
		}
		Inet4Address yourIP = (Inet4Address) InetAddress.getByName(hostIP);
		hdr = new BOOTPHeader(BOOTPHeader.BOOTREPLY, hdr.getTransactionID(), hdr.getClientIPAddress(), yourIP, (Inet4Address) InetAddress.getLocalHost(), hdr.getClientHwAddress());
		BOOTPMessage msg = new BOOTPMessage(hdr);
		packet = msg.toDatagramPacket();
		packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
		packet.setPort(CLIENT_PORT);
		socket.send(packet);
	}
}
