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
 * Device with specific ID has not been found exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceNotFoundException extends DeviceException {

	/**
	 * Initialize this instance. 
	 */
	public DeviceNotFoundException() {
		super();
	}

	/**
	 * Initialize this instance. 
	 * @param message
	 * @param cause
	 */
	public DeviceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Initialize this instance. 
	 * @param cause
	 */
	public DeviceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Initialize this instance. 
	 * @param s
	 */
	public DeviceNotFoundException(String s) {
		super(s);
	}
}
