/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.driver.block.usb.storage;

import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBEndPoint;

final class USBStorageDeviceData {
    
	/** */
	private ITransport transport;
	/** */
	private USBDataPipe sendControlPipe;
	/** */
	private USBDataPipe receiveControlPipe;
	/** */
	private USBDataPipe sendBulkPipe;
	/** */
	private USBDataPipe receiveBulkPipe;
	/** */
	private USBEndPoint bulkInEndPoint;
	/** */
	private USBEndPoint bulkOutEndPoint;
	/** */
	private USBEndPoint intrEndPoint;
	/** */
	private byte maxLun;

	
	public USBStorageDeviceData() {
	}
	
	/**
	 * @return Returns the receiveBulkPipe.
	 */
	public USBDataPipe getReceiveBulkPipe() {
		return receiveBulkPipe;
	}

	/**
	 * @param receiveBulkPipe The receiveBulkPipe to set.
	 */
	public void setReceiveBulkPipe(USBDataPipe receiveBulkPipe) {
		this.receiveBulkPipe = receiveBulkPipe;
	}

	/**
	 * @return Returns the receiveControlPipe.
	 */
	public USBDataPipe getReceiveControlPipe() {
		return receiveControlPipe;
	}

	/**
	 * @param receiveControlPipe The receiveControlPipe to set.
	 */
	public void setReceiveControlPipe(USBDataPipe receiveControlPipe) {
		this.receiveControlPipe = receiveControlPipe;
	}

	/**
	 * @return Returns the sendBulkPipe.
	 */
	public USBDataPipe getSendBulkPipe() {
		return sendBulkPipe;
	}

	/**
	 * @param sendBulkPipe The sendBulkPipe to set.
	 */
	public void setSendBulkPipe(USBDataPipe sendBulkPipe) {
		this.sendBulkPipe = sendBulkPipe;
	}

	/**
	 * @return Returns the sendControlPipe.
	 */
	public USBDataPipe getSendControlPipe() {
		return sendControlPipe;
	}

	/**
	 * @param sendControlPipe The sendControlPipe to set.
	 */
	public void setSendControlPipe(USBDataPipe sendControlPipe) {
		this.sendControlPipe = sendControlPipe;
	}

	/**
	 * 
	 * @param dev
	 */
	

	/**
	 * @return Returns the bulkInEndPoint.
	 */
	public USBEndPoint getBulkInEndPoint() {
		return bulkInEndPoint;
	}

	/**
	 * @param bulkInEndPoint The bulkInEndPoint to set.
	 */
	public void setBulkInEndPoint(USBEndPoint bulkInEndPoint) {
		this.bulkInEndPoint = bulkInEndPoint;
	}

	/**
	 * @return Returns the bulkOutEndPoint.
	 */
	public USBEndPoint getBulkOutEndPoint() {
		return bulkOutEndPoint;
	}

	/**
	 * @param bulkOutEndPoint The bulkOutEndPoint to set.
	 */
	public void setBulkOutEndPoint(USBEndPoint bulkOutEndPoint) {
		this.bulkOutEndPoint = bulkOutEndPoint;
	}

	/**
	 * @return Returns the intrEndPoint.
	 */
	public USBEndPoint getIntrEndPoint() {
		return intrEndPoint;
	}

	/**
	 * @param intrEndPoint The intrEndPoint to set.
	 */
	public void setIntrEndPoint(USBEndPoint intrEndPoint) {
		this.intrEndPoint = intrEndPoint;
	}

	/**
	 * @return Returns the maxLun.
	 */
	public byte getMaxLun() {
		return maxLun;
	}

	/**
	 * @param maxLun The maxLun to set.
	 */
	public void setMaxLun(byte maxLun) {
		this.maxLun = maxLun;
	}

	/**
	 * @return Returns the transport.
	 */
	public ITransport getTransport() {
		return transport;
	}

	/**
	 * @param transport The transport to set.
	 */
	public void setTransport(ITransport transport) {
		this.transport = transport;
	}

}
