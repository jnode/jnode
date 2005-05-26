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

package org.jnode.driver.bus.ide;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.system.ResourceNotFreeException;

public class DefaultIDEDeviceFactory implements IDEDeviceFactory
{
    public IDEDevice createIDEDevice(IDEBus bus, boolean primary,
            boolean master, String name, IDEDriveDescriptor descriptor,
            DefaultIDEControllerDriver controller)
    {
        return new IDEDevice(bus, primary, master, name, descriptor, 
                controller);
    }
    
    public IDEBus createIDEBus(Device parent, boolean primary) 
        throws IllegalArgumentException, DriverException, 
                ResourceNotFreeException
    {
        return new IDEBus(parent, primary);
    }

    public IDEIO createIDEIO(Device parent, boolean primary) throws IllegalArgumentException, DriverException, ResourceNotFreeException
    {
        return new DefaultIDEIO(parent, primary);
    }

    public IBMPartitionTable createIBMPartitionTable(byte[] bs, Device dev)
    {
        return new IBMPartitionTable(bs, dev);
    }
}
