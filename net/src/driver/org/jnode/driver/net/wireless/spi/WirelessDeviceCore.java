/*
 * $Id$
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

package org.jnode.driver.net.wireless.spi;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.net.wireless.AuthenticationMode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class WirelessDeviceCore extends AbstractDeviceCore {

    /**
     * Start a scan for available networks.
     */
    public abstract void startScan()
        throws DriverException;

    /**
     * Gets the current authentication mode.
     *
     * @return
     */
    protected abstract AuthenticationMode getAuthenticationMode()
        throws DriverException;

    /**
     * Sets the current authentication mode.
     *
     * @param mode
     */
    protected abstract void setAuthenticationMode(AuthenticationMode mode)
        throws DriverException;

    /**
     * Gets the current ESS ID.
     *
     * @return A valid ESSID, or null if not ESSID is present.
     */
    protected abstract String getESSID()
        throws DriverException;

    /**
     * Sets the current ESSID.
     *
     * @param essid A valid ESSID, or null for any ESS.
     */
    protected abstract void setESSID(String essid)
        throws DriverException;
}
