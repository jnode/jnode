/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.driver.block.ide.disk;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.MappedBlockDeviceSupport;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author epr
 */
public class IDEDiskPartitionDriver extends Driver implements FSBlockDeviceAPI, IDEConstants {

    /**
     * The device i'm driving
     */
    private IDEDiskPartitionDevice device;
    private PartitionTableEntry pte;
    private MappedBlockDeviceSupport mapping;

    protected void startDevice() throws DriverException {
        try {
            final IDEDiskPartitionDevice dev = this.device;
            this.pte = dev.getPartitionTableEntry();
            final Device parent = dev.getParent();
            final long offset = dev.getStartSector() * SECTOR_SIZE;
            final long length = dev.getSectors() * SECTOR_SIZE;
            this.mapping = new MappedBlockDeviceSupport(parent, offset, length);
            /* Register the FSBlockDevice API */
            device.registerAPI(FSBlockDeviceAPI.class, this);
        } catch (IOException ex) {
            throw new DriverException("Error in MappedBlockDeviceSupport", ex);
        }
    }

    protected void stopDevice() {
        /* for now only unregister an API */
        device.unregisterAPI(FSBlockDeviceAPI.class);
    }

    protected void afterConnect(Device device) {
        this.device = (IDEDiskPartitionDevice) device;
        super.afterConnect(device);
    }

    /**
     * Gets the sector size for this device.
     */
    public int getSectorSize() {
        return SECTOR_SIZE;
    }

    /**
     * Gets the partition table entry specifying this device.
     *
     * @return A PartitionTableEntry or null if no partition table entry exists.
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return pte;
    }

    public void flush() throws IOException {
        mapping.flush();
    }

    public long getLength() {
        return mapping.getLength();
    }

    public void read(long devOffset, ByteBuffer dest) throws IOException {
        mapping.read(devOffset, dest);
    }

    public void write(long devOffset, ByteBuffer src) throws IOException {
        mapping.write(devOffset, src);
    }
}
