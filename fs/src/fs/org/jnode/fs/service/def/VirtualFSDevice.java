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
 
package org.jnode.fs.service.def;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;

/**
 * Device for the VirtualFS.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VirtualFSDevice extends Device {

    public VirtualFSDevice() {
        super(null, "vfs");
        try {
            setDriver(new VirtualFSDriver());
        } catch (DriverException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class VirtualFSDriver extends Driver {

        /**
         * @see org.jnode.driver.Driver#startDevice()
         */
        protected void startDevice() throws DriverException {
            // Nothing
        }

        /**
         * @see org.jnode.driver.Driver#stopDevice()
         */
        protected void stopDevice() throws DriverException {
            // Nothing
        }
    }
}
