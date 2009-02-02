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
 
package org.jnode.driver.block.floppy;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.driver.block.CHS;
import org.jnode.driver.block.FSBlockAlignmentSupport;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.floppy.support.FloppyDeviceFactory;
import org.jnode.driver.block.floppy.support.FloppyDriverUtils;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.ByteBufferUtils;
import org.jnode.util.TimeoutException;

/**
 * Driver for a FloppyDevice (a single floppy drive)
 *
 * @author epr
 */
public class FloppyDriver extends Driver implements FSBlockDeviceAPI, RemovableDeviceAPI, FloppyConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(FloppyDriver.class);
    /**
     * Drive number
     */
    private int drive;
    /**
     * The controller of the floppy drive i'm a driver for
     */
    private FloppyControllerDriver controller;
    private FloppyDriveParameters dp;
    private FloppyParameters fp;
    private int currentSectorSize = -1;
    private FSBlockAlignmentSupport api;

    /**
     * Start the device
     */
    protected void startDevice() {
        final FloppyDevice dev = (FloppyDevice) getDevice();
        controller = dev.getFloppyControllerBus().getController();
        drive = dev.getDrive();
        dp = dev.getDriveParams();
        fp = null;
        api = new FSBlockAlignmentSupport(this, 512);
        dev.registerAPI(RemovableDeviceAPI.class, this);
        dev.registerAPI(FSBlockDeviceAPI.class, api);
    }

    /**
     * Stop the device
     */
    protected void stopDevice() {
        final FloppyDevice dev = (FloppyDevice) getDevice();
        dev.unregisterAPI(RemovableDeviceAPI.class);
        dev.unregisterAPI(FSBlockDeviceAPI.class);
        controller = null;
        drive = -1;
        fp = null;
        dp = null;
    }

    /**
     * @see org.jnode.driver.block.BlockDeviceAPI#flush()
     */
    public void flush() {
        // Nothing to do here
    }

    /**
     * @return The length
     * @throws IOException
     * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
     */
    public long getLength() throws IOException {
        testDiskChange();
        return fp.getGeometry().getTotalSectors() * SECTOR_LENGTH[currentSectorSize];
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.BlockDeviceAPI#read(long, java.nio.ByteBuffer)
     */
    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        //TODO optimize it also to use ByteBuffer at lower level                 
        ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        byte[] dest = destBA.toArray();
        int destOffset = 0;
        int length = dest.length;

        synchronized (controller) {
            testDiskChange();
            BlockDeviceAPIHelper.checkBounds(this, devOffset, length);
            final int sectorLength = SECTOR_LENGTH[currentSectorSize];
            testAlignment(sectorLength, devOffset, length);
            CHS chs = fp.getGeometry().getCHS(devOffset / sectorLength);
            FloppyDeviceFactory factory;
            try {
                factory = FloppyDriverUtils.getFloppyDeviceFactory();
            } catch (NamingException ex) {
                throw (IOException) new IOException().initCause(ex);
            }

            try {
                while (length > 0) {
                    final FloppyReadSectorCommand cmd;
                    cmd =
                        factory.createFloppyReadSectorCommand(
                            drive,
                            fp.getGeometry(),
                            chs,
                            currentSectorSize,
                            false,
                            fp.getGap1Size(),
                            dest,
                            destOffset);
                    controller.executeAndWait(cmd, RW_TIMEOUT);
                    destOffset += sectorLength;
                    length -= sectorLength;

                    if (length > 0) {
                        // go to next sector only more data to read (length>0)                    
                        chs = fp.getGeometry().nextSector(chs);
                    }
                }
            } catch (TimeoutException ex) {
                timeout(ex, "read");
            }
        }

        destBA.refreshByteBuffer();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.BlockDeviceAPI#write(long, java.nio.ByteBuffer)
     */
    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        //TODO optimize it also to use ByteBuffer at lower level                 
        ByteBufferUtils.ByteArray srcBA = ByteBufferUtils.toByteArray(srcBuf);
        byte[] src = srcBA.toArray();
        int srcOffset = 0;
        int length = src.length;

        synchronized (controller) {
            testDiskChange();
            BlockDeviceAPIHelper.checkBounds(this, devOffset, length);
            final int sectorLength = SECTOR_LENGTH[currentSectorSize];
            testAlignment(sectorLength, devOffset, length);
            CHS chs = fp.getGeometry().getCHS(devOffset / sectorLength);
            FloppyDeviceFactory factory;
            try {
                factory = FloppyDriverUtils.getFloppyDeviceFactory();
            } catch (NamingException ex) {
                throw (IOException) new IOException().initCause(ex);
            }

            try {
                while (length > 0) {
                    final FloppyWriteSectorCommand cmd;
                    cmd =
                        factory.createFloppyWriteSectorCommand(
                            drive,
                            fp.getGeometry(),
                            chs,
                            currentSectorSize,
                            false,
                            fp.getGap1Size(),
                            src,
                            srcOffset);
                    controller.executeAndWait(cmd, RW_TIMEOUT);
                    srcOffset += sectorLength;
                    length -= sectorLength;

                    if (length > 0) {
                        // go to next sector only more data to write (length>0)                    
                        chs = fp.getGeometry().nextSector(chs);
                    }
                }
            } catch (TimeoutException ex) {
                timeout(ex, "write");
            }
        }
    }

    /**
     * @return The partition table entry, always null here.
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return null; // A floppy has not partition table
    }

    /**
     * @return int
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() {
        if (currentSectorSize < 0) {
            return 512;
        } else {
            return SECTOR_LENGTH[currentSectorSize];
        }
    }

    /**
     * Can this device be locked.
     *
     * @return if this device can be locked
     */
    public boolean canLock() {
        return false;
    }

    /**
     * Can this device be ejected.
     *
     * @return if this device can be ejected
     */
    public boolean canEject() {
        return false;
    }

    /**
     * Lock the device.
     *
     * @throws IOException
     */
    public void lock()
        throws IOException {
        throw new IOException("Unsupported operation");
    }

    /**
     * Unlock the device.
     *
     * @throws IOException
     */
    public void unlock()
        throws IOException {
        throw new IOException("Unsupported operation");
    }

    /**
     * Is this device locked.
     *
     * @see org.jnode.driver.RemovableDeviceAPI#isLocked()
     */
    public boolean isLocked() {
        return false;
    }

    /**
     * Eject this device.
     *
     * @throws IOException
     */
    public void eject()
        throws IOException {
        throw new IOException("Unsupported operation");
    }

    /**
     * Test if the disk has been changed and/or the format of the loaded floppy
     * is known. If the floppy has been changed or the format of the floppy is
     * not known, try to probe its format.
     *
     * @throws IOException
     */
    private final void testDiskChange() throws IOException {
        if (controller.diskChanged(drive, true)) {
            log.debug("FloppyDisk change detected");
            // Disk has changed, probe for format of new disk
            fp = null;
        }
        if (fp == null) {
            log.debug("Try to probe floppy format...");
            controller.resetFDC();
            log.debug("seek(0)");
            seek(0);
            fp = probeFormat();
            api.setAlignment(SECTOR_LENGTH[currentSectorSize]);
        }
    }

    /**
     * Try to determine the format of the disk in the drive
     *
     * @return The parameters
     * @throws IOException
     */
    private final FloppyParameters probeFormat() throws IOException {
        final FloppyParameters[] fpList = dp.getAutodetectFormats();
        final byte[] buf = new byte[512];
        for (int i = 0; i < fpList.length; i++) {
            final FloppyParameters fp = fpList[i];
            log.debug("Trying format " + fp);

            final Geometry geom = fp.getGeometry();
            final int cyl = geom.getCylinders() - 1;
            final int sect = geom.getSectors();
            final int head = geom.getHeads() - 1;
            final CHS chs0 = new CHS(cyl, head, sect);
            specify(fp);
            seek(cyl);
            readID();

            try {
                final FloppyReadSectorCommand cmd =
                    new FloppyReadSectorCommand(
                        drive,
                        fp.getGeometry(),
                        chs0,
                        currentSectorSize,
                        false,
                        fp.getGap1Size(),
                        buf,
                        0);
                controller.executeAndWait(cmd, RW_TIMEOUT);
                // The read has succeeded, we found the format!
                log.debug("Found floppy format: " + fp);
                return fp;
            } catch (TimeoutException ex) {
                // Read failed, reset the controller and try the next format
                controller.resetFDC();
            }

        }
        throw new IOException("Unknown format");
    }

    /**
     * Seek to a given cylinder
     *
     * @param cylinder
     * @throws IOException
     */
    private final void seek(int cylinder) throws IOException {
        FloppyDeviceFactory factory;
        try {
            factory = FloppyDriverUtils.getFloppyDeviceFactory();
        } catch (NamingException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
        final FloppySeekCommand cmd = factory.createFloppySeekCommand(drive, cylinder);
        try {
            controller.executeAndWait(cmd, SEEK_TIMEOUT);
            if (controller.diskChanged(drive, false)) {
                throw new IOException("Floppy not present");
            }
        } catch (TimeoutException ex) {
            timeout(ex, "seek");
        }
    }

    /**
     * Read the ID of the disk
     *
     * @throws IOException
     */
    private final void readID() throws IOException {
        FloppyDeviceFactory factory;
        try {
            factory = FloppyDriverUtils.getFloppyDeviceFactory();
        } catch (NamingException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
        final FloppyIdCommand cmd = factory.createFloppyIdCommand(drive);
        try {
            controller.executeAndWait(cmd, RW_TIMEOUT);
            if (cmd.hasError()) {
                final IOException ioe = new IOException("Error in Read ID");
                ioe.initCause(cmd.getError());
                throw ioe;
            }
            currentSectorSize = cmd.getSectorSize();
        } catch (TimeoutException ex) {
            timeout(ex, "readID");
        }
    }

    /**
     * Read the ID of the disk
     *
     * @param fp
     * @throws IOException
     */
    private final void specify(FloppyParameters fp) throws IOException {
        FloppyDeviceFactory factory;
        try {
            factory = FloppyDriverUtils.getFloppyDeviceFactory();
        } catch (NamingException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
        final FloppyDriveParametersCommand cmd = factory.createFloppyDriveParametersCommand(drive, dp, fp);

        try {
            controller.executeAndWait(cmd, RW_TIMEOUT);
            if (cmd.hasError()) {
                final IOException ioe = new IOException("Error in specify");
                ioe.initCause(cmd.getError());
                throw ioe;
            }
        } catch (TimeoutException ex) {
            timeout(ex, "specify");
        }
    }

    /**
     * Test the alignment of the given parameters
     *
     * @param sectorLength
     * @param devOffset
     * @param length
     * @throws IOException
     */
    private final void testAlignment(int sectorLength, long devOffset, int length) throws IOException {
        if ((devOffset % sectorLength) != 0) {
            throw new IOException("devOffset is not sector aligned");
        }
        if ((length % sectorLength) != 0) {
            throw new IOException("length is not sector aligned");
        }
    }

    /**
     * Process a timeout during read/write
     *
     * @param ex
     * @param operation
     * @throws IOException
     */
    private final void timeout(TimeoutException ex, String operation) throws IOException {
        controller.resetFDC();
        fp = null;
        final IOException ioe = new IOException("Timeout in " + operation);
        ioe.initCause(ex);
        throw ioe;
    }
}
