/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.apps.jpartition.utils.device;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableType;

public abstract class AbstractIDEDevice extends IDEDevice implements
        IDEDeviceAPI<IBMPartitionTableEntry> {
    private PartitionTable<IBMPartitionTableEntry> pt;

    public AbstractIDEDevice(String name, boolean primary, boolean master) throws DriverException,
            NameNotFoundException, IOException {
        super(null, primary, master, name, null, null);

        registerAPI(PartitionableBlockDeviceAPI.class, this);
        registerAPI(IDEDeviceAPI.class, this);

        setDriver(new FileIDEDeviceDriver());
    }

    public final PartitionTable<IBMPartitionTableEntry> getPartitionTable() throws IOException {
        if (pt == null) {
            try {
                pt = buildPartitionTable();
            } catch (NameNotFoundException e) {
                throw new IOException(e);
            } catch (DriverException e) {
                throw new IOException(e);
            }
        }

        return pt;
    }

    public final int getSectorSize() throws IOException {
        return IDEConstants.SECTOR_SIZE;
    }

    private final PartitionTable<IBMPartitionTableEntry> buildPartitionTable()
        throws DriverException, IOException, NameNotFoundException {
        // Read the bootsector
        final byte[] bs = new byte[IDEConstants.SECTOR_SIZE];
        read(0, ByteBuffer.wrap(bs));

        return new IBMPartitionTable(new IBMPartitionTableType(), bs, this);
    }

    public String toString() {
        return getId();
    }
}
