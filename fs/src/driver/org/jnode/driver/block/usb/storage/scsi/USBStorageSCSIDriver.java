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
 
package org.jnode.driver.block.usb.storage.scsi;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.driver.block.FSBlockAlignmentSupport;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.usb.storage.USBStorageConstants;
import org.jnode.driver.block.usb.storage.USBStorageSCSIHostDriver.USBStorageSCSIDevice;
import org.jnode.driver.bus.scsi.SCSIDevice;
import org.jnode.driver.bus.scsi.SCSIDeviceAPI;
import org.jnode.driver.bus.scsi.SCSIException;
import org.jnode.driver.bus.scsi.SCSIHostControllerAPI;
import org.jnode.driver.bus.scsi.cdb.mmc.CapacityData;
import org.jnode.driver.bus.scsi.cdb.mmc.MMCUtils;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.TimeoutException;

public class USBStorageSCSIDriver extends Driver
    implements FSBlockDeviceAPI, RemovableDeviceAPI, SCSIHostControllerAPI, USBPipeListener, USBStorageConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(USBStorageSCSIDriver.class);

    /** */
    private final FSBlockAlignmentSupport blockAlignment;

    /** */
    private boolean locked;

    /** */
    private CapacityData capacity;

    /** */
    private boolean changed;

    /** */
    // private final ResourceManager rm;
    public USBStorageSCSIDriver() {
        this.blockAlignment = new FSBlockAlignmentSupport(this, 2048);
    }

    @Override
    protected void startDevice() throws DriverException {
        final Device dev = getDevice();
        // Rename the device
        try {
            final DeviceManager dm = dev.getManager();
            synchronized (dm) {
                dm.rename(dev, "sg", true);
            }
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException(ex);
        }

        this.locked = false;
        this.changed = true;
        this.capacity = null;
        this.blockAlignment.setAlignment(2048);

        dev.registerAPI(RemovableDeviceAPI.class, this);
        dev.registerAPI(FSBlockDeviceAPI.class, blockAlignment);
    }

    @Override
    protected void stopDevice() throws DriverException {
        try {
            unlock();
        } catch (IOException ex) {
            throw new DriverException(ex);
        } finally {
            final SCSIDevice dev = (SCSIDevice) getDevice();
            dev.unregisterAPI(RemovableDeviceAPI.class);
            dev.unregisterAPI(FSBlockDeviceAPI.class);
            dev.unregisterAPI(SCSIDeviceAPI.class);
        }

    }

    public int getSectorSize() throws IOException {
        processChanged();
        return capacity.getBlockLength();
    }

    public PartitionTableEntry getPartitionTableEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getLength() throws IOException {
        processChanged();
        return capacity.getBlockLength() & capacity.getLogicalBlockAddress();
    }

    public void read(long devOffset, ByteBuffer dest) throws IOException {
        processChanged();
        if (capacity == null) {
            throw new IOException("No medium");
        }

    }

    public void write(long devOffset, ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub

    }

    public void flush() throws IOException {
        // TODO Auto-generated method stub

    }

    public void requestCompleted(USBRequest request) {
        // TODO Auto-generated method stub

    }

    public void requestFailed(USBRequest request) {
        // TODO Auto-generated method stub

    }

    /**
     * Unlock the device.
     *
     * @throws IOException
     */
    public synchronized void unlock() throws IOException {
        if (!locked) {
            final SCSIDevice dev = (SCSIDevice) getDevice();
            try {
                MMCUtils.setMediaRemoval(dev, false, false);
            } catch (SCSIException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } catch (TimeoutException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } catch (InterruptedException ex) {
                throw new InterruptedIOException();
            }
            locked = false;
        }
    }

    private void processChanged() throws IOException {
        if (changed) {
            this.capacity = null;
            final USBStorageSCSIDevice dev = (USBStorageSCSIDevice) getDevice();
            try {
                // Gets the capacity.
                this.capacity = MMCUtils.readCapacity(dev);
                this.blockAlignment.setAlignment(capacity.getBlockLength());
            } catch (SCSIException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } catch (TimeoutException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } catch (InterruptedException ex) {
                throw new InterruptedIOException();
            }
            changed = false;
        }
    }

    public boolean canLock() {
        return true;
    }

    /**
     * It's a removable device.
     */
    public boolean canEject() {
        return true;
    }

    public void lock() throws IOException {
        // TODO Auto-generated method stub

    }

    public boolean isLocked() {
        return locked;
    }

    public void eject() throws IOException {
        // TODO Auto-generated method stub

    }
}
