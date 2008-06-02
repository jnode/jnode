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

package org.jnode.driver.bus.ide;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableType;
import org.jnode.system.ResourceNotFreeException;

/**
 * Implementation of a Factory class to create device, bus and IO objects
 * for the IDE
 */
public class DefaultIDEDeviceFactory implements IDEDeviceFactory {

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.bus.ide.IDEDeviceFactory#createIDEDevice(org.jnode.driver.bus.ide.IDEBus,
     * boolean, boolean, java.lang.String, org.jnode.driver.bus.ide.IDEDriveDescriptor,
     * org.jnode.driver.bus.ide.DefaultIDEControllerDriver)
     */
    public IDEDevice createIDEDevice(IDEBus bus, boolean primary,
                                     boolean master, String name, IDEDriveDescriptor descriptor,
                                     DefaultIDEControllerDriver controller) {
        return new IDEDevice(bus, primary, master, name, descriptor,
            controller);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.bus.ide.IDEDeviceFactory#createIDEBus(org.jnode.driver.Device, boolean)
     */
    public IDEBus createIDEBus(Device parent, boolean primary)
        throws IllegalArgumentException, DriverException, ResourceNotFreeException {
        return new IDEBus(parent, primary);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.bus.ide.IDEDeviceFactory#createIDEIO(org.jnode.driver.Device, boolean)
     */
    public IDEIO createIDEIO(Device parent, boolean primary)
        throws IllegalArgumentException, DriverException, ResourceNotFreeException {
        return new DefaultIDEIO(parent, primary);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.bus.ide.IDEDeviceFactory#createIBMPartitionTable(byte[], org.jnode.driver.Device)
     */
    public IBMPartitionTable createIBMPartitionTable(byte[] bs, Device dev) {
        return new IBMPartitionTable(new IBMPartitionTableType(), bs, dev);
    }
}
