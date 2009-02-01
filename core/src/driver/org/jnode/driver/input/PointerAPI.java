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
 
package org.jnode.driver.input;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DriverPermission;

/**
 * Device API implemented by Pointer devices.
 *
 * @author qades
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public interface PointerAPI extends DeviceAPI {
    //todo KeboardAPI.SET_PREFERRED_LISTENER_PERMISSION has a similar role
    //remove duplication

    /**
     * Permission
     */
    public static final DriverPermission SET_PREFERRED_LISTENER_PERMISSION =
        new DriverPermission("setPreferredListener");

    /**
     * Add a pointer listener
     *
     * @param listener the pointer listener to be added
     */
    public void addPointerListener(PointerListener listener);

    /**
     * Remove a pointer listener
     *
     * @param listener the pointer listener to be removed
     */
    public void removePointerListener(PointerListener listener);

    /**
     * Claim to be the preferred listener.
     * The given listener must have been added by addPointerListener.
     * If there is a security manager, this method will call
     * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
     *
     * @param listener the prefered pointer listener
     */
    public abstract void setPreferredListener(PointerListener listener);
}
