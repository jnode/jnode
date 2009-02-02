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
 
package org.jnode.driver.bus.pci;

import java.util.HashMap;
import java.util.Map;

/**
 * @author epr
 */
public class VendorDescriptor {

    private final int id;
    private final String name;
    private final Map<Integer, DeviceDescriptor> devices = new HashMap<Integer, DeviceDescriptor>();

    public VendorDescriptor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the ID of the vendor
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the Name of the vendor
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the descriptor of the device with the given id.
     *
     * @param deviceId
     * @return Unknown device if not found, never null
     */
    public DeviceDescriptor findDevice(int deviceId) {
        DeviceDescriptor result;
        result = (DeviceDescriptor) devices.get(deviceId);
        if (result == null) {
            result = new DeviceDescriptor(deviceId, "? (" + deviceId + ")");
        }
        return result;
    }

    /**
     * Add a device descriptor of a device of this vendor
     *
     * @param device
     */
    protected void addDevice(DeviceDescriptor device) {
        devices.put(device.getId(), device);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName();
    }

}
