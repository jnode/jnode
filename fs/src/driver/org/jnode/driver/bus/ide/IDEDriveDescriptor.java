/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.bus.ide;

/**
 * @author epr
 */
public class IDEDriveDescriptor {

	/** Drive identification info */
	private final int[] data;
	/** Is this an atapi drive? */
	private final boolean atapi;
	
	/**
	 * Create a new instance
	 * @param data
	 * @param atapi
	 */
	public IDEDriveDescriptor(int[] data, boolean atapi) {
		if (data.length != 256) {
			throw new IllegalArgumentException("data must be 256 int's long");
		}
		this.atapi = atapi;
		this.data = data;
	}
	
	/**
	 * Gets the serial number
	 * @return the serial number
	 */
	public String getSerialNumber() {
		char[] str = new char[20];
		for (int i = 0; i < 10; i++) {
			int v = data[10+i];
			str[i+2+0] = (char)((v >> 8) & 0xFF);
			str[i*2+1] = (char)(v & 0xFF);			 
		}
		return String.valueOf(str).trim();
	}
	
	/**
	 * Gets the firmware version identifier
	 * @return the firmware version identifier
	 */
	public String getFirmware() {
		char[] str = new char[8];
		for (int i = 0; i < 4; i++) {
			int v = data[23+i];
			str[i+2+0] = (char)((v >> 8) & 0xFF);
			str[i*2+1] = (char)(v & 0xFF);			 
		}
		return String.valueOf(str).trim();
	}
	
	/**
	 * Gets the model identifier
	 * @return the model identifier
	 */
	public String getModel() {
		char[] str = new char[40];
		for (int i = 0; i < 20; i++) {
			int v = data[27+i]; 
			str[i*2+0] = (char)((v >> 8) & 0xFF);
			str[i*2+1] = (char)(v & 0xFF);
		}
		return String.valueOf(str).trim();
	}
	
	/**
	 * Is this a disk device?
	 * @return if this is a disk device
	 */
	public boolean isDisk() {
		// TODO Very ugly check, look in descriptor
		return isAta();
	}
	
	/**
	 * Is this a CDROM device?
	 * @return if this is a CDROM device
	 */
	public boolean isCDROM() {
		// TODO Very ugly check, look in descriptor
		return isAtapi();
	}
	
	/**
	 * Is this a Tape device?
	 * @return if this is a Tape device
	 */
	public boolean isTape() {
		return false;
	}
	
	/**
	 * Convert to a String representation
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(2048);
		for (int i = 0; i < 256; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(data[i]);
		}
		buf.append(", serial=[").append(getSerialNumber()).append(']');
        buf.append(", firmware=[").append(getFirmware()).append(']'); 
        buf.append(", model=[").append(getModel()).append(']');
        
        return buf.toString();
	}
	
	/**
	 * Is this an ATAPI drive?
	 * @return if this is an ATAPI drive
	 */
	public boolean isAtapi() {
		return atapi;
	}
	
	/**
	 * Is this an ATA drive?
	 * @return if this is an ATA drive
	 */
	public boolean isAta() {
		return ((data[0] & 0x8000) == 0);
	}
	
	/**
	 * Is this device removable?
	 * @return if this device is removable
	 */
	public boolean isRemovable() {
		return ((data[0] & 0x80) != 0);
	}
	
	/**
	 * Does this device support LBA?
	 * @return True if this device supports LBA, false otherwise
	 */
	public boolean supportsLBA() {
		return ((data[49] & 0x0200) != 0);
	}
	
	/**
	 * Does this device support DMA?
	 * @return True if this device supports DMA, false otherwise
	 */
	public boolean supportsDMA() {
		return ((data[49] & 0x0100) != 0);
	}

	/**
	 * Does this device support 48-bit addressing mode?
	 * @return True if this device supports 48-bit addressing, false otherwise
	 */	
	public boolean supports48bitAddressing() {
		return ((data[83] & 0x40) != 0);
	}
	
	/**
	 * Gets the number of addressable sectors in 28-addressing. 
	 * @return the number of addressable sectors
	 */
	public long getSectorsIn28bitAddressing() {
		final long h = data[61];
		final long l = data[60];
		return ((h << 16) & 0xFFFF0000) | (l & 0xFFFF);  
	}

	/**
	 * Gets the number of addressable sectors in 48-addressing. 
	 * @return the number of addressable sectors
	 */
	public long getSectorsIn48bitAddressing() {
		final long v3 = data[103] & 0xFFFF;
		final long v2 = data[102] & 0xFFFF;
		final long v1 = data[101] & 0xFFFF;
		final long v0 = data[100] & 0xFFFF;
		return (v3 << 48) | (v2 << 16) | (v1 << 16) | v0;  
	}

}
