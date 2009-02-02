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
 
package org.jnode.driver.bus.ide;

import org.jnode.driver.Device;

/**
 * An IDE device is some kind of storage device connected to an IDE controller.
 * An IDE device must support the BlockDeviceAPI interface.
 *
 * @author epr
 */
public class IDEDevice extends Device {

    /**
     * The decscriptor of this device
     */
    private final IDEDriveDescriptor descriptor;
    private final boolean primary;
    private final boolean master;
    private final DefaultIDEControllerDriver controller;

    /**
     * Create a new instance
     *
     * @param bus
     * @param primary
     * @param master
     * @param name
     * @param descriptor
     * @param controller
     */
    public IDEDevice(IDEBus bus, boolean primary, boolean master, String name, IDEDriveDescriptor descriptor,
                     DefaultIDEControllerDriver controller) {
        super(bus, name);
        this.primary = primary;
        this.master = master;
        this.descriptor = descriptor;
        this.controller = controller;
    }

    /**
     * Gets the descriptor of this device
     *
     * @return the descriptor of this device
     */
    public IDEDriveDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Is this device master on the IDE bus?
     *
     * @return if this device is the master on its bus
     */
    public boolean isMaster() {
        return master;
    }

    /**
     * Is this device on the primary channel
     *
     * @return if this device is on the primary channel
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Is this device on the secondary channel
     *
     * @return if this device is on the secondary channel
     */
    public boolean isSecondary() {
        return !primary;
    }

    /**
     * Gets the controller of this device
     *
     * @return the DefaultIDEControllerDriver for this device
     */
    public DefaultIDEControllerDriver getController() {
        return controller;
    }

}
