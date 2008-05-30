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

package org.jnode.driver.net;

import org.jnode.net.wireless.AuthenticationMode;

/**
 * Device API for wireless network devices.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WirelessNetDeviceAPI extends NetDeviceAPI {

    /**
     * Gets the current authentication mode.
     *
     * @return
     */
    public AuthenticationMode getAuthenticationMode();

    /**
     * Sets the current authentication mode.
     *
     * @param mode
     */
    public void setAuthenticationMode(AuthenticationMode mode)
        throws NetworkException;

    /**
     * Gets the current ESS ID.
     *
     * @return A valid ESSID, or null if not ESSID is present.
     */
    public String getESSID();

    /**
     * Sets the current ESS ID.
     *
     * @param essid A valid ESSID, or null for any ESS.
     */
    public void setESSID(String essid)
        throws NetworkException;

}
