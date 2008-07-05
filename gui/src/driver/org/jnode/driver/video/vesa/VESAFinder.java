/*
 * $Id: VGAFinder.java,v 1.6 2006/01/01 12:40:42 epr Exp $
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

package org.jnode.driver.video.vesa;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.vm.Unsafe;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESAFinder implements DeviceFinder {

    public VESAFinder() {
        Unsafe.debug("created VESAFinder");
    }

    /**
     * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager,
     *      org.jnode.driver.Bus)
     */
    public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
        try {
            devMan.register(new VESADevice(bus));
        } catch (DriverException ex) {
            Unsafe.debugStackTrace("error in findDevices", ex);
            throw new DeviceException(ex);
        }
    }

    public static class VESADevice extends Device {

        /**
         * @param bus
         */
        public VESADevice(Bus bus) throws DriverException {
            super(bus, "VESA");
            this.setDriver(new VESADriver());
        }
    }
}
