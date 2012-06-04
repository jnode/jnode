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
 
package org.jnode.partitions.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.LongArgument;
import org.jnode.shell.syntax.SizeArgument;

/**
 * @author gbin
 * @author Trickkiste
 * @author crawley@jnode.org
 */
public class FdiskCommand extends AbstractCommand {
    // FIXME ... this is a dangerous command and it needs some extra checking to help
    // avoid catastrophic errors.  At the very least, it needs a mode that shows the
    // user what would happen but does nothing.
    private final FlagArgument FLAG_INIT_MBR =
        new FlagArgument("initMBR", Argument.OPTIONAL,
            "if set, init the device's Master Boot Record");

    private final FlagArgument FLAG_DELETE =
        new FlagArgument("delete", Argument.OPTIONAL, "if set, delete a partition");

    private final FlagArgument FLAG_BOOTABLE =
        new FlagArgument("bootable", Argument.OPTIONAL,
            "if set, toggle the partition's bootable flag");

    private final FlagArgument FLAG_MODIFY =
        new FlagArgument("modify", Argument.OPTIONAL, "if set, modify or create a partition");

    private final IntegerArgument ARG_PARTITION =
        new IntegerArgument("partition", Argument.OPTIONAL, "Target partition number (0..3)");

    private final LongArgument ARG_START =
        new LongArgument("start", Argument.OPTIONAL, "Partition start sector");

    private final LongArgument ARG_SECTORS =
        new LongArgument("sectors", Argument.OPTIONAL, "Partition size in sectors");

    private final SizeArgument ARG_BYTES =
        new SizeArgument("bytes", Argument.OPTIONAL, "Partition size in bytes (300K, 45M, etc)");

    private final IBMPartitionTypeArgument ARG_TYPE =
        new IBMPartitionTypeArgument("type", Argument.OPTIONAL, "IBM partition type code");

    //todo add support for more BlockDeviceAPI types
    private final DeviceArgument ARG_DEVICE =
        new DeviceArgument("deviceId", Argument.OPTIONAL, "Target device", IDEDeviceAPI.class);

    public FdiskCommand() {
        super("perform disk partition management tasks");
        registerArguments(FLAG_BOOTABLE, FLAG_DELETE, FLAG_INIT_MBR, FLAG_MODIFY, ARG_DEVICE,
            ARG_PARTITION, ARG_START, ARG_SECTORS, ARG_BYTES, ARG_TYPE);
    }

    public static void main(String[] args) throws Exception {
        new FdiskCommand().execute(args);
    }

    public void execute() throws Exception {
        final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (!ARG_DEVICE.isSet()) {
            // Show all devices.
            listAvailableDevices(dm, out);
            return;
        }

        Device dev = ARG_DEVICE.getValue();
        // FIXME PartitionHelper assumes that the device is an IDE device !?!
        if (!(dev instanceof IDEDevice)) {
            err.println(dev.getId() + " is not an IDE device");
            exit(1);
        }
        final PartitionHelper helper = new PartitionHelper(dev.getId(), out);
        try {
            helper.checkMBR();
        } catch (IOException ioex) {
            out.println(ioex.getMessage());
            out.println("Create a new MBR with a valid partition table.");
            helper.initMbr();
            helper.write();
        }

        if (FLAG_BOOTABLE.isSet()) {
            helper.toggleBootable(getPartitionNumber(helper));
            helper.write();
        } else if (FLAG_DELETE.isSet()) {
            helper.deletePartition(getPartitionNumber(helper));
            helper.write();
        } else if (FLAG_MODIFY.isSet()) {
            modifyPartition(helper, getPartitionNumber(helper), out);
            helper.write();
        } else if (FLAG_INIT_MBR.isSet()) {
            helper.initMbr();
            helper.write();
        } else {
            printPartitionTable(helper, out);
        }
    }

    private int getPartitionNumber(PartitionHelper helper) {
        int partNumber = ARG_PARTITION.getValue();
        if (partNumber >= helper.getNbPartitions() || partNumber < 0) {
            throw new IllegalArgumentException("Partition number is invalid");
        }
        return partNumber;
    }

    private void modifyPartition(PartitionHelper helper, int id, PrintWriter out)
        throws IOException {
        long start = ARG_START.getValue();
        long size = ARG_SECTORS.isSet() ? ARG_SECTORS.getValue() : ARG_BYTES.getValue();
        IBMPartitionTypes type = ARG_TYPE.getValue();

        out.println("Init " + id + " with start = " + start + ", size = " + size + ", fs = " +
            Integer.toHexString(type.getCode()));
        boolean sizeUnit = ARG_BYTES.isSet() ? PartitionHelper.BYTES : PartitionHelper.SECTORS;
        helper.modifyPartition(id, false, start, size, sizeUnit, type);
    }

    private void printPartitionTable(PartitionHelper helper, PrintWriter out)
        throws DeviceNotFoundException, ApiNotFoundException, IOException {
        IDEDevice ideDev = helper.getDevice();
        IDEDriveDescriptor descriptor = ideDev.getDescriptor();
        int sectorSize = IDEConstants.SECTOR_SIZE;
        if (ideDev != null) {
            out.println("IDE Disk : " + ideDev.getId() + ": " +
                descriptor.getSectorsAddressable() * 512 + " bytes");
        }
        out.println("Device Boot    Start       End    Blocks   System");
        IBMPartitionTable partitionTable = helper.getPartitionTable();
        int i = 0;
        for (IBMPartitionTableEntry entry : partitionTable) {
            IBMPartitionTypes si = entry.getSystemIndicator();
            if (!entry.isEmpty()) {
                long sectors = entry.getNrSectors();

                out.println("ID " + i + " " + (entry.getBootIndicator() ? "Boot" : "No") + "    " +
                    entry.getStartLba() + "    " + (entry.getStartLba() + sectors) + "    " +
                    entry.getNbrBlocks(sectorSize) + (entry.isOdd() ? "" : "+") + "    " + si);
            }
            if (entry.isExtended()) {
                final List<IBMPartitionTableEntry> exPartitions =
                    partitionTable.getExtendedPartitions();
                int j = 0;
                for (IBMPartitionTableEntry exEntry : exPartitions) {
                    si = exEntry.getSystemIndicator();
                    // FIXME ... this needs work
                    out.println("ID " + i + " " + (exEntry.getBootIndicator() ? "Boot" : "No") +
                        "    " + exEntry.getStartLba() + "    " + "-----" + "    " + "-----" +
                        "    " + si);
                    j++;
                }
            }
            i++;
        }
    }

    private void listAvailableDevices(DeviceManager dm, PrintWriter out) {
        final Collection<Device> allDevices = dm.getDevicesByAPI(BlockDeviceAPI.class);
        for (Device dev : allDevices) {
            //out.println("Found device : " + dev.getId() + "[" + dev.getClass() + "]");
            if (dev instanceof IDEDevice) {
                IDEDevice ideDevice = (IDEDevice) dev;
                IDEDriveDescriptor desc = ideDevice.getDescriptor();
                if (desc.isDisk()) {
                    out.println("IDE Disk: " + ideDevice.getId() + "('" + desc.getModel() + "' " +
                        desc.getSectorsAddressable() * IDEConstants.SECTOR_SIZE + " bytes)");
                }
            }
        }
    }
}
