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

package org.jnode.net.ipv4.config.impl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.prefs.Preferences;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class NetDeviceConfig {

    /**
     * Initialize this instance.
     */
    public NetDeviceConfig() {
    }

    /**
     * Apply this configuration for the device.
     */
    public final void apply(final Device device) throws NetworkException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws NetworkException {
                    doApply(device);
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw (NetworkException) ex.getException();
        }
    }

    /**
     * Apply this configuration for the device.
     */
    protected abstract void doApply(Device device) throws NetworkException;

    /**
     * Load the data of this configuration from the given preferences.
     * @param prefs
     */
    public abstract void load(Preferences prefs);

    /**
     * Store the data of this configuration into the given preferences.
     * @param prefs
     */
    public abstract void store(Preferences prefs);
}
