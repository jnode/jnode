/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.net.spi;

import java.util.HashMap;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.LayerHeader;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.SocketBuffer;
import org.jnode.net.util.NetUtils;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * @author epr
 */
public abstract class AbstractNetDriver extends Driver implements NetDeviceAPI, QueueProcessor {

	/** My logger */
	private static final Logger log = Logger.getLogger(AbstractNetDriver.class);
	/** Device prefix for ethernet devices */
	public static final String ETH_DEVICE_PREFIX = "eth";
	/** Device prefix for loopback devices */
	public static final String LOOPBACK_DEVICE_PREFIX = "lo";

	/** Number of received bytes */
	private long rx_count;
	/** Number of transmitted bytes */
	private long tx_count;
	/** Mapping between protocol id and protocol address */
	private final HashMap protocolAddresses = new HashMap();
	/** Queue used to store frames ready for transmission */
	private final Queue txQueue = new Queue();
	/** Thread used to transmit frames */
	private QueueProcessorThread txThread;
		
	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		final Device device = getDevice();
		try {
			final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			dm.rename(device, getDevicePrefix(), true);
		} catch (DeviceAlreadyRegisteredException ex) {
			log.error("Cannot rename device", ex);
		} catch (NameNotFoundException ex) {
			throw new DriverException("Cannot find DeviceManager", ex);
		}
		device.registerAPI(NetDeviceAPI.class, this);
		txThread = new QueueProcessorThread(device.getId() + "-tx", txQueue, this);
		txThread.start();
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		txThread.stopProcessor();
		txThread = null;
		getDevice().unregisterAPI(NetDeviceAPI.class);
	}

	/**
	 * @see org.jnode.driver.net.NetDeviceAPI#transmit(SocketBuffer, HardwareAddress)
	 */
	public final void transmit(SocketBuffer skbuf, HardwareAddress destination) 
	throws NetworkException {
		// Update all layer headers
		int offset = 0;
		offset = updateLayerHeader(skbuf, skbuf.getLinkLayerHeader(), offset);
		offset = updateLayerHeader(skbuf, skbuf.getNetworkLayerHeader(), offset);
		offset = updateLayerHeader(skbuf, skbuf.getTransportLayerHeader(), offset);
		
		//log.debug("Adding to transmit queue");
		txQueue.add(new Object[] { skbuf, destination });
	}
	
	private final int updateLayerHeader(SocketBuffer skbuf, LayerHeader hdr, int offset) {
		if (hdr != null) {
			hdr.finalizeHeader(skbuf, offset);
			return offset + hdr.getLength();
		} else {
			return offset;
		}
	}

	/**
	 * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
	 */
	public void process(Object object) {
		try {
			//log.debug("<transmit dev=" + getDevice().getId() + ">");
			final Object[] data = (Object[])object;
			final SocketBuffer skbuf = (SocketBuffer)data[0];
			final HardwareAddress destination = (HardwareAddress)data[1];
			tx_count += skbuf.getSize(); 
			doTransmit(skbuf, destination);
			//log.debug("</transmit dev=" + getDevice().getId() + ">");
		} catch (NetworkException ex) {
			log.error("Cannot transmit packet", ex);
		}
	}
	
	/**
	 * @see org.jnode.driver.net.NetDeviceAPI#transmit(SocketBuffer, HardwareAddress)
	 */
	protected abstract void doTransmit(SocketBuffer skbuf, HardwareAddress destination) 
	throws NetworkException;
	
	/**
	 * Gets the prefix for the device name
	 * @see #ETH_DEVICE_PREFIX
	 */
	protected abstract String getDevicePrefix();
	
	/**
	 * Pass a received frame onto the PacketTypeManager.
	 * @param skbuf
	 */
	protected void onReceive(SocketBuffer skbuf)
	throws NetworkException {
		skbuf.setDevice(getDevice());
		rx_count += skbuf.getSize();
		NetUtils.sendToPTM(skbuf);
	}

	/**
	 * Gets the protocol address information for a given protocol.
	 * @param protocolID
	 * @return The protocol address information, or null if not found.
	 */
	public ProtocolAddressInfo getProtocolAddressInfo(int protocolID) {
		return (ProtocolAddressInfo)protocolAddresses.get(new Integer(protocolID));
	}

	/**
	 * Sets the protocol address information for a given protocol.
	 * @param protocolID
	 */
	public void setProtocolAddressInfo(int protocolID, ProtocolAddressInfo addressInfo) {
		protocolAddresses.put(new Integer(protocolID), addressInfo);
	}

}
