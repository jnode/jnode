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
 
package org.jnode.driver.ps2;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.util.NumberUtils;

/**
 * PS2 device finder.
 * 
 * @author qades
 */
public class PS2Finder implements DeviceFinder, PS2Constants {

    /** My logger */
    private static final Logger log = Logger.getLogger(PS2Finder.class);

    /**
     * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager,
     *      org.jnode.driver.Bus)
     */
    public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
        try {
            log.debug("Searching for PS2 devices");
            final PS2Bus ps2 = new PS2Bus(bus);

            // register the keyboard device
            PS2Device kbDev = new PS2Device(ps2, PS2_KEYBOARD_DEV);
            kbDev.setDriver(new PS2KeyboardDriver(ps2));
            devMan.register(kbDev);

            // register the keyboard device
            if (ps2.testMouse()) {
                PS2Device pDev = new PS2Device(ps2, PS2_POINTER_DEV);
                pDev.setDriver(new PS2PointerDriver(ps2));
                devMan.register(pDev);
            }
            log.debug("ps2stat 0x" + NumberUtils.hex(ps2.readStatus(), 2));
        } catch (DriverException ex) {
            log.debug("Error searching for PS2 devices: " + ex);
            throw new DeviceException(ex);
        }
    }

}
