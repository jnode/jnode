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
 
package org.jnode.driver.bus.usb;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractDescriptor extends USBPacket {

	/**
	 * Initialize this instance
	 * 
	 * @param size
	 */
	public AbstractDescriptor(int size) {
		super(size);
	}

	/**
	 * @param data
	 * @param ofs
	 * @param len
	 */
	public AbstractDescriptor(byte[] data, int ofs, int len) {
		super(data, ofs, len);
	}

	/**
	 * Gets the length of the descriptor
	 */
	public final int getLength() {
		return getByte(0);
	}

	/**
	 * Gets the type of the descriptor
	 */
	public final int getType() {
		return getByte(1);
	}

	/**
	 * Load all strings with the default Language ID.
	 * 
	 * @param dev
	 */
	void loadStrings(USBDevice dev) throws USBException {
		// Do nothing
	}
}
