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
 
package org.jnode.driver.block.floppy;

import org.jnode.driver.Device;

/**
 * Device wrapper around a single floppy drive.
 * @author epr
 */
public class FloppyDevice extends Device {

	/** My drive number 0.. */
	private final int drive;
	private final FloppyDriveParameters dp;
	private final FloppyControllerBus bus;
	
	/**
	 * @param bus
	 * @param drive
	 * @param dp
	 */
	public FloppyDevice(FloppyControllerBus bus, int drive, FloppyDriveParameters dp) {
		super(bus, "fd" + drive);
		this.bus = bus;
		this.drive = drive;
		this.dp = dp;
	}

	public FloppyControllerBus getFloppyControllerBus() {
		return bus;
	}

	/**
	 * Gets the drive number of this device
	 * @return drive
	 */
	public int getDrive() {
		return drive;
	}
	
	/**
	 * Gets the drive parameters for this device
	 * @return parametera
	 */
	public FloppyDriveParameters getDriveParams() {
		return dp;
	}

}
