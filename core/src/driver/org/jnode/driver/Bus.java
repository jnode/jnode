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
 
package org.jnode.driver;

/**
 * A software representation of a hardware bus that has
 * devices connected to it.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @see org.jnode.driver.Device
 */
public abstract class Bus {

    /**
     * My parent, or null if I'm the system bus
     */
    private final Bus parent;
    /**
     * The device that is connected to the parent bus and "provides" this bus. This can be null.
     */
    private final Device parentDevice;

    /**
     * Package protected initializer for the system bus.
     */
    Bus() {
        this.parent = null;
        this.parentDevice = null;
    }

    /**
     * Initialize a new bus.
     *
     * @param parent
     */
    public Bus(Bus parent) {
        this.parent = parent;
        this.parentDevice = null;
    }

    /**
     * Initialize a new bus.
     *
     * @param parent
     */
    public Bus(Device parent) {
        this.parent = parent.getBus();
        this.parentDevice = parent;
    }

    /**
     * Gets the parent of this bus, or null if this is the system bus.
     *
     * @return The parent
     */
    public final Bus getParent() {
        return this.parent;
    }

    /**
     * Gets the device that is connected to the parent bus
     * and "provides" this bus.
     * This can be null.
     *
     * @return The parent device
     */
    public final Device getParentDevice() {
        return this.parentDevice;
    }

}
