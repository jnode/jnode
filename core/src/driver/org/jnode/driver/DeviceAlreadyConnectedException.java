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
 
package org.jnode.driver;

/**
 * The device has already been connected to a driver exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceAlreadyConnectedException extends DriverException {

	/**
	 * 
	 */
	public DeviceAlreadyConnectedException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DeviceAlreadyConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DeviceAlreadyConnectedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DeviceAlreadyConnectedException(String s) {
		super(s);
	}
}
