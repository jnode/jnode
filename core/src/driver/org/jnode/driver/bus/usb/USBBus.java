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
 
package org.jnode.driver.bus.usb;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;

/**
 * @author epr
 */
public class USBBus extends Bus {

    /**
     * Bitmap with in use device id's
     */
    private final boolean devIdsInUse[];
    /**
     * The Host Controller API for this bus
     */
    private final USBHostControllerAPI hcApi;

    /**
     * @param parent
     */
    public USBBus(Device parent, USBHostControllerAPI hcApi) {
        super(parent);
        this.hcApi = hcApi;
        this.devIdsInUse = new boolean[128];
    }

    /**
     * Allocate a new device id.
     */
    final synchronized int allocDeviceID() {
        final int max = devIdsInUse.length;
        for (int i = 1; i < max; i++) {
            if (!devIdsInUse[i]) {
                devIdsInUse[i] = true;
                return i;
            }
        }
        throw new IllegalArgumentException("Too many allocated USB device id's");
    }

    /**
     * Free a given device id.
     */
    final synchronized void freeDeviceID(int devId) {
        devIdsInUse[devId] = false;
    }

    /**
     * @return Returns the hcApi.
     */
    public USBHostControllerAPI getHcApi() {
        return this.hcApi;
    }
}
