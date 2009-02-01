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
 
package org.jnode.driver.block.scsi.cdrom;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.driver.block.FSBlockAlignmentSupport;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.bus.scsi.SCSIDevice;
import org.jnode.driver.bus.scsi.SCSIDeviceAPI;
import org.jnode.driver.bus.scsi.SCSIException;
import org.jnode.driver.bus.scsi.cdb.mmc.CDBStartStopUnit;
import org.jnode.driver.bus.scsi.cdb.mmc.CapacityData;
import org.jnode.driver.bus.scsi.cdb.mmc.MMCUtils;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.ByteBufferUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SCSICDROMDriver extends Driver implements FSBlockDeviceAPI,
    RemovableDeviceAPI {

    private boolean locked;
    private final FSBlockAlignmentSupport blockAlignment;

    private CapacityData capacity;

    private boolean changed;

    public SCSICDROMDriver() {
        this.blockAlignment = new FSBlockAlignmentSupport(this, 2048);
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        this.locked = false;
        this.changed = true;
        this.capacity = null;
        this.blockAlignment.setAlignment(2048);
        final SCSIDevice dev = (SCSIDevice) getDevice();
        dev.registerAPI(SCSIDeviceAPI.class, new SCSIDevice.SCSIDeviceAPIImpl(
            dev));
        dev.registerAPI(RemovableDeviceAPI.class, this);
        dev.registerAPI(FSBlockDeviceAPI.class, blockAlignment);
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
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

    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }

    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() throws IOException {
        processChanged();
        return capacity.getBlockLength();
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#flush()
     */
    public void flush() throws IOException {
        // Do nothing
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
     */
    public long getLength() throws IOException {
        processChanged();
        return capacity.getBlockLength() & capacity.getLogicalBlockAddress();
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
     */
    public void read(long devOffset, ByteBuffer destBuf)
        throws IOException {
        //TODO optimize it also to use ByteBuffer at lower level                 
        ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        byte[] dest = destBA.toArray();
        int destOffset = 0;
        int length = dest.length;

        processChanged();
        if (capacity == null) {
            throw new IOException("No medium");
        }
        final int blockLength = capacity.getBlockLength();
        final int lba = (int) (devOffset / blockLength);
        final int nrBlocks = length / blockLength;
        final SCSIDevice dev = (SCSIDevice) getDevice();
        try {
            MMCUtils.readData(dev, lba, nrBlocks, dest, destOffset);
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

        destBA.refreshByteBuffer();
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
     */
    public void write(long devOffset, ByteBuffer src)
        throws IOException {
        throw new IOException("Readonly device");
    }

    /**
     * Can this device be locked.
     *
     * @return
     */
    public boolean canLock() {
        return true;
    }

    /**
     * Can this device be ejected.
     *
     * @return
     */
    public boolean canEject() {
        return true;
    }

    /**
     * Lock the device.
     *
     * @throws IOException
     */
    public synchronized void lock() throws IOException {
        if (!locked) {
            final SCSIDevice dev = (SCSIDevice) getDevice();
            try {
                MMCUtils.setMediaRemoval(dev, true, false);
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
            locked = true;
        }
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

    /**
     * Is this device locked.
     *
     * @see org.jnode.driver.RemovableDeviceAPI#isLocked()
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Eject this device.
     *
     * @throws IOException
     */
    public void eject() throws IOException {
        if (locked) {
            throw new IOException("Device is locked");
        }
        final SCSIDevice dev = (SCSIDevice) getDevice();
        try {
            MMCUtils.startStopUnit(dev, CDBStartStopUnit.Action.EJECT, false);
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
    }

    /**
     * Process the changed flag.
     *
     * @throws IOException
     */
    private void processChanged() throws IOException {
        if (changed) {
            this.capacity = null;
            final SCSIDevice dev = (SCSIDevice) getDevice();
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
}
