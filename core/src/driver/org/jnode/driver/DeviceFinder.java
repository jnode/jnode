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
 * Interface used to discover devices on a given bus.
 *
 * @see org.jnode.driver.Device 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceFinder {

	/**
	 * Find all devices that are connected to the given bus and register 
	 * them with the given device manager.
	 * @param devMan
	 * @param bus
	 * @throws DeviceException
	 */
	public void findDevices(DeviceManager devMan, Bus bus)
	throws DeviceException;

}
