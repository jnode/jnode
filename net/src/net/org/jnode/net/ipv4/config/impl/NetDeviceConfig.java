/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.net.ipv4.config.impl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
    public final void apply(final Device device)
    throws NetworkException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws NetworkException {
                    doApply(device);
                    return null;
                    }});
        } catch (PrivilegedActionException ex) {
            throw (NetworkException)ex.getException();
        }
    }

    /**
     * Apply this configuration for the device.
     */
    protected abstract void doApply(Device device)
    throws NetworkException;    
}
