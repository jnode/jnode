/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.block.floppy;

import org.jnode.driver.Device;

/**
 * Device wrapper around a single floppy drive.
 *
 * @author epr
 */
public class FloppyDevice extends Device {

    /**
     * My drive number 0..
     */
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

    /**
     * @return the bus for this floppy device
     */
    public FloppyControllerBus getFloppyControllerBus() {
        return bus;
    }

    /**
     * Gets the drive number of this device
     *
     * @return drive
     */
    public int getDrive() {
        return drive;
    }

    /**
     * Gets the drive parameters for this device
     *
     * @return parametera
     */
    public FloppyDriveParameters getDriveParams() {
        return dp;
    }

}
