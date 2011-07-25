/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.driver.block.PartitionableBlockAlignmentSupport;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.driver.bus.ide.IDEDriverUtils;
import org.jnode.driver.bus.ide.command.IDERWSectorsCommand;
import org.jnode.driver.bus.ide.command.IDEReadSectorsCommand;
import org.jnode.driver.bus.ide.command.IDEWriteSectorsCommand;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.util.TimeoutException;

/**
 * Device driver for IDE disks.
 *
 * @author epr
 */
public class IDEDiskDriver extends Driver
    implements IDEDeviceAPI<IBMPartitionTableEntry>, IDEConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(IDEDiskDriver.class);

    /**
     * Number of addressable sectors
     */
    private long maxSector;

    /** Has LBA support? */
    //private boolean lba;

    /** Has DMA support? */
    //private boolean dma;

    /**
     * Support 48-bit addressing?
     */
    private boolean is48bit;
    private IDEDiskBus diskBus;
    private IBMPartitionTable pt;

    protected void startDevice() throws DriverException {
        final IDEDevice dev = (IDEDevice) getDevice();
        diskBus = new IDEDiskBus(dev);
        /* Register the IDEDevice API */
        // FIXME - something is wrong with the typing here I think.  
        dev.registerAPI(PartitionableBlockDeviceAPI.class, new PartitionableBlockAlignmentSupport(this, SECTOR_SIZE));
        /* Get basic configuration */
        final IDEDriveDescriptor descr = dev.getDescriptor();
        //lba = descr.supportsLBA();
        //dma = descr.supportsDMA();
        is48bit = descr.supports48bitAddressing();
        maxSector = descr.getSectorsAddressable();

        // Look for partitions
        try {
            // Find the devicemanager
            DeviceManager devMan = InitialNaming.lookup(DeviceManager.NAME);
            // Read the bootsector
            final byte[] bs = new byte[SECTOR_SIZE];
            read(0, ByteBuffer.wrap(bs));

            IDEDeviceFactory factory;
            try {
                factory = IDEDriverUtils.getIDEDeviceFactory();
            } catch (NamingException ex) {
                throw new DriverException(ex);
            }
            this.pt = factory.createIBMPartitionTable(bs, dev);

            int partIndex = 0;
            int i = 0;
            for (IBMPartitionTableEntry pte : pt) {
                if (pte == null) {
                    BootLogInstance.get().warn("PartitionTableEntry #" + i + " is null");
                } else if (pte.isValid()) {
                    registerPartition(devMan, dev, pte, partIndex);
                }
                partIndex++;
                i++;
            }
            if (!pt.getExtendedPartitions().isEmpty()) {
                // Create partition devices for the extended partition
                log.debug("Extended");
                partIndex = registerExtendedPartition(devMan, dev, partIndex);
            }
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException("Partition device is already known???? Probably a bug", ex);
        } catch (IOException ex) {
            throw new DriverException("Cannot read partition table", ex);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find DeviceManager", ex);
        }
    }

    protected void stopDevice() throws DriverException {
        final IDEDevice dev = (IDEDevice) getDevice();
        // find mounted partitions on this device and unregister them !
        try {
            DeviceManager devMan = InitialNaming.lookup(DeviceManager.NAME);
            Collection<Device> devices = devMan.getDevices();
            final ArrayList<IDEDiskPartitionDevice> toStop = new ArrayList<IDEDiskPartitionDevice>();

            for (Device device : devices) {
                if (device instanceof IDEDiskPartitionDevice) {
                    IDEDiskPartitionDevice partition = (IDEDiskPartitionDevice) device;
                    if (partition.getParent() == dev) {
                        toStop.add(partition);
                    }
                }

            }

            for (IDEDiskPartitionDevice partition : toStop) {
                devMan.unregister(partition);
            }
        } catch (NameNotFoundException e) {
            throw new DriverException("Problem while stopping this IDE device", e);
        }

        dev.unregisterAPI(BlockDeviceAPI.class);
        this.pt = null;
    }

    public void flush() {
        // Nothing to do yet
    }

    public long getLength() {
        return maxSector * SECTOR_SIZE;
    }

    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        transfer(devOffset, destBuf, false);
    }

    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        transfer(devOffset, srcBuf, true);
    }

    protected void transfer(long devOffset, ByteBuffer buf, boolean isWrite) throws IOException {
//        int bufOffset = 0;
        int length = buf.remaining();

        BlockDeviceAPIHelper.checkBounds(this, devOffset, length);
        BlockDeviceAPIHelper.checkAlignment(SECTOR_SIZE, this, devOffset, length);

        final long lbaStart = devOffset / SECTOR_SIZE;
        final int sectors = length / SECTOR_SIZE;

        final String errorSource = isWrite ? "write" : "read";
        if (lbaStart + sectors > this.maxSector) {
            throw new IOException(errorSource + " beyond device sectors");
        }

        final IDEDevice dev = (IDEDevice) getDevice();
        final IDEBus bus = (IDEBus) dev.getBus();
        final int maxSectorCount = is48bit ? MAX_SECTOR_COUNT_48 : MAX_SECTOR_COUNT_28;

        while (length > 0) {
            final long partLbaStart = devOffset / SECTOR_SIZE;
            final int partSectors = Math.min(length / SECTOR_SIZE, maxSectorCount);
            final int partLength = partSectors * SECTOR_SIZE;

            final IDERWSectorsCommand cmd = isWrite ? new IDEWriteSectorsCommand(
                dev.isPrimary(),
                dev.isMaster(),
                is48bit,
                partLbaStart,
                partSectors,
                buf) : new IDEReadSectorsCommand(
                    dev.isPrimary(),
                    dev.isMaster(),
                    is48bit,
                    partLbaStart,
                    partSectors,
                    buf);
            try {
                bus.executeAndWait(cmd, IDE_DATA_XFER_TIMEOUT);
            } catch (InterruptedException ex) {
                throw new IOException("IDE " + errorSource + " interrupted", ex);
            } catch (TimeoutException ex) {
                throw new InterruptedIOException("IDE timeout: " + ex.getMessage());
            }
            if (cmd.hasError()) {
                throw new IOException("IDE " + errorSource + " error:" + cmd.getError());
            }

            length -= partLength;
            devOffset += partLength;
        }
    }

    static class IDEDiskBus extends Bus {

        public IDEDiskBus(IDEDevice parent) {
            super(parent);
        }
    }

    /*
     * Register the given partition entry (maybe an extended partition entry)
     */
    private void registerPartition(DeviceManager devMan, IDEDevice dev,
                                   IBMPartitionTableEntry pte, int partIndex)
        throws DeviceAlreadyRegisteredException, DriverException {
        final String id = dev.getId() + partIndex;
        final IDEDiskPartitionDevice pdev =
            new IDEDiskPartitionDevice(
                diskBus,
                id,
                dev,
                pte,
                pte.getStartLba(),
                pte.getNrSectors());
        pdev.setDriver(new IDEDiskPartitionDriver());
        devMan.register(pdev);
    }

    /**
     * register all the partitions included in the extended partition
     *
     * @param devMan
     * @param dev
     * @param partIndex the first partition index to use
     * @return the next partition index
     * @throws DeviceAlreadyRegisteredException
     *
     * @throws DriverException
     */
    private int registerExtendedPartition(DeviceManager devMan, IDEDevice dev, int partIndex)
        throws DeviceAlreadyRegisteredException, DriverException {
        //now we should have an filled vector in the pt
        final List<IBMPartitionTableEntry> extendedPartitions = pt.getExtendedPartitions();
        log.info("Have " + extendedPartitions.size() + " Extended partitions found");

        for (int iPart = 0; iPart < extendedPartitions.size(); iPart++) {
            IBMPartitionTableEntry pteExt = extendedPartitions.get(iPart);
            registerPartition(devMan, dev, pteExt, partIndex);

            if (iPart < (extendedPartitions.size() - 1)) {
                partIndex++;
            }
        }
        return partIndex;
    }

    public int getSectorSize() throws IOException {
        return SECTOR_SIZE;
    }

    /**
     * Gets the partition table that this block device contains.
     *
     * @return {@code null} if no partition table is found.
     * @throws IOException
     */
    public IBMPartitionTable getPartitionTable() throws IOException {
        return pt;
    }
}
