/*
 * $Id$
 *
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
 
package org.jnode.driver.video.vga;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public class VGAFinder implements DeviceFinder {

    /**
     * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager,
     *      org.jnode.driver.Bus)
     */
    public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
        /*
         * try { devMan.register(new VGADevice(bus)); } catch (DriverException
         * ex) { throw new DeviceException(ex); }
         */
    }

    public static class VGADevice extends Device {

        /**
         * @param bus
         */
        public VGADevice(Bus bus) throws DriverException {
            super(bus, "Standard VGA");
            this.setDriver(new VGADriver());
        }
    }
}
