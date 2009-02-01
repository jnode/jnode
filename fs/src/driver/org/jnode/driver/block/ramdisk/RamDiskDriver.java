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
 
package org.jnode.driver.block.ramdisk;

import java.nio.ByteBuffer;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskDriver extends Driver implements FSBlockDeviceAPI {

    private static final String RAMDISK_DEFAULTNAME = "ramdisk";
    /**
     * The device
     */
    private RamDiskDevice device;
    /**
     * The data
     */
    private byte[] data;
    private String devName;

    /**
     * Create a RamDisk Driver
     *
     * @param deviceName null will name it ramdisk with autonumber
     */
    public RamDiskDriver(String deviceName) {
        this.devName = deviceName;
    }

    /**
     * Start the device
     *
     * @throws DriverException
     */
    protected void startDevice() throws DriverException {
        try {
            this.device = (RamDiskDevice) getDevice();
            if (this.devName == null) {
                this.device.getManager().rename(device, RAMDISK_DEFAULTNAME, true);
            } else {
                this.device.getManager().rename(device, devName, false);
            }

            if (this.data == null) {
                this.data = new byte[device.getSize()];
                this.device.registerAPI(BlockDeviceAPI.class, this);
            } else {
                this.device.registerAPI(FSBlockDeviceAPI.class, this);
            }
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * Stop the device
     */
    protected void stopDevice() {
        this.device.unregisterAPI(FSBlockDeviceAPI.class);
        this.device.unregisterAPI(BlockDeviceAPI.class);
        this.device = null;
    }

    /**
     * @return The partition table entry
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }

    /**
     * @return The sector size
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() {
        return 512;
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#flush()
     */
    public void flush() {
        // Do nothing
    }

    /**
     * @return The length
     * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
     */
    public long getLength() {
        return data.length;
    }

    /**
     * @param devOffset
     * @param dest
     * @param destOffset
     * @param length
     * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
     */
    public void read(long devOffset, ByteBuffer dest) {
        //System.arraycopy(this.data, (int)devOffset, dest, destOffset, length);
        dest.put(this.data, (int) devOffset, dest.remaining());
    }

    /**
     * @param devOffset
     * @param src
     * @param srcOffset
     * @param length
     * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
     */
    public void write(long devOffset, ByteBuffer src) {
        //System.arraycopy(src, srcOffset, this.data, (int)devOffset, length);
        src.get(this.data, (int) devOffset, src.remaining());
    }
}
