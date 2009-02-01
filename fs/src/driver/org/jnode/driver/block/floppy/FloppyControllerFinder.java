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

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public class FloppyControllerFinder implements DeviceFinder {

    /**
     * @param devMan
     * @param bus
     * @throws DeviceException
     * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager, org.jnode.driver.Bus)
     */
    public void findDevices(DeviceManager devMan, Bus bus)
        throws DeviceException {
        try {
            // Register floppy controller device
            Device fdcDev = new FloppyControllerDevice(bus);
            fdcDev.setDriver(new FloppyControllerDriver());
            devMan.register(fdcDev);
        } catch (DriverException ex) {
            throw new DeviceException(ex);
        }
    }

}
