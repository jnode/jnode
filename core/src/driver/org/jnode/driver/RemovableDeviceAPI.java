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
 
package org.jnode.driver;

import java.io.IOException;

/**
 * API that must be implemented by removable devices.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface RemovableDeviceAPI extends DeviceAPI {

    /**
     * Can this device be locked.
     *
     * @return <code>true</code> if this device can be locked, <code>false</code> otherwise
     */
    public boolean canLock();

    /**
     * Can this device be ejected.
     *
     * @return <code>true</code> if this device can be ejected, <code>false</code> otherwise
     */
    public boolean canEject();

    /**
     * Lock the device.
     *
     * @throws IOException
     */
    public void lock()
        throws IOException;

    /**
     * Unlock the device.
     *
     * @throws IOException
     */
    public void unlock()
        throws IOException;

    /**
     * Is this device locked.
     *
     * @return <code>true</code> if this device is locked, <code>false</code> otherwise
     */
    public boolean isLocked();

    /**
     * Eject this device.
     *
     * @throws IOException
     */
    public void eject()
        throws IOException;
}
