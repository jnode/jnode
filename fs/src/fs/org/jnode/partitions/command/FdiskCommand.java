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
 
package org.jnode.partitions.command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.fs.fat.BootSector;
import org.jnode.fs.fat.GrubBootSector;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableType;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.StringArgument;
/**
 * @author gbin
 * @author Trickkiste
 */
public class FdiskCommand {

	static final OptionArgument INITMBR =
		new OptionArgument(
			"init. MBR",
			"Type parameter",
			new OptionArgument.Option[] {
				 new OptionArgument.Option("--initmbr", "initialize the Master Boot Record of the device")});

	static final OptionArgument ACTION =
		new OptionArgument(
			"action",
			"Action on a specified partition",
			new OptionArgument.Option[] {
				new OptionArgument.Option("-d", "Delete a partition"),
				new OptionArgument.Option("-b", "Switch the bootable flag of a partition"),
				new OptionArgument.Option("-m", "Modify/create a partition")});

	static final StringArgument PARTITION = new StringArgument("partition number", "Targeted partition");
	static final StringArgument PARTITION_DESCRIPTION = new StringArgument("description", "Partition description" +
			"     \"ID:start:size:filesystem\"" +
			" ID : ID of the partition" +
			"                             start : Sector where the partition starts" +
			"          size : Size of the partition in sectors" +
			"             filesystem : Number of the filesystem type");
	
	static final DeviceArgument ARG_DEVICE =
		new DeviceArgument("device-id", "the device on which you want to change/create the partition");

	static final Parameter PARAM_INITMBR = new Parameter(INITMBR, Parameter.MANDATORY);
	static final Parameter PARAM_ACTION = new Parameter(ACTION, Parameter.MANDATORY);
	static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE, Parameter.MANDATORY);
	static final Parameter PARAM_PARTITION = new Parameter(PARTITION, Parameter.MANDATORY);
	static final Parameter PARAM_PARTITION_DESCRIPTION = new Parameter(PARTITION_DESCRIPTION, Parameter.MANDATORY);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"fdisk",
			new Syntax[] {
				new Syntax("Lists the available devices"),
				new Syntax("Print the partition table of a device", new Parameter[] { PARAM_DEVICE }),
				new Syntax("Initialize the MBR of a device", new Parameter[] { PARAM_INITMBR, PARAM_DEVICE }),
				new Syntax(
					"Create / Delete / change a partition",
					new Parameter[] { PARAM_ACTION, PARAM_PARTITION, PARAM_DEVICE }),
					new Syntax(
							"Create / Delete / change a partition",
							new Parameter[] { PARAM_ACTION, PARAM_PARTITION_DESCRIPTION, PARAM_DEVICE })
	});

	/*
	 * public static Help.Info HELP_INFO = new Help.Info( "fdisk", "With no
	 * argument, it lists the available devices\n" + "With only the device, it
	 * lists the current partitions on the device\n" + "--initmbr initialize the
	 * Master Boot Record of the device\n" + "-m add or modify the id partition
	 * with first sector at start, size of size sectors and fs id fs\n" + "-d
	 * delete the partition with id id" + "-b switch boot flag on parition id",
	 */

	public static void main(String[] args) throws SyntaxErrorException {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		DeviceManager dm;
		try {
			dm = InitialNaming.lookup(DeviceManager.NAME);

			boolean isAction = PARAM_ACTION.isSet(cmdLine);
			boolean isInitMBR = PARAM_INITMBR.isSet(cmdLine);
			boolean isDevice = PARAM_DEVICE.isSet(cmdLine);

			// no parameters
			if (!isDevice) {
				listAvailableDevice(dm);
				return;
			}

			// only device is set
			if (!isAction && !isInitMBR && isDevice) {
				printTable(ARG_DEVICE.getValue(cmdLine), dm);
				return;
			}

			// initMBR
			if (isInitMBR) {
				initMbr(ARG_DEVICE.getValue(cmdLine), dm);
				return;
			}

			// now it is a change on a specific partition so read the table

			IDEDevice current = (IDEDevice)dm.getDevice(ARG_DEVICE.getValue(cmdLine));
			BlockDeviceAPI api = current.getAPI(BlockDeviceAPI.class);
			ByteBuffer mbr = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
			api.read(0, mbr);
			if (!IBMPartitionTable.containsPartitionTable(mbr.array()))
				throw new IOException("This device doesn't contain a valid MBR, use --initmbr.");

			BootSector bs = new BootSector(mbr.array());

			if (ACTION.getValue(cmdLine).intern() == "-m") {
				modifyPartition(PARTITION.getValue(cmdLine), api, bs, current);
				bs.write(api);
				return;
			}

			// it is not a modify so the PARTITION parameter is only a partition
			// number

			int partNumber;
			try {
				partNumber = Integer.parseInt(PARTITION.getValue(cmdLine));
			} catch (NumberFormatException f) {
				throw new IllegalArgumentException("Partition number is invalid");
			}

			if (partNumber > 3 || partNumber < 0)
				throw new IllegalArgumentException("Partition number is invalid");

			if (ACTION.getValue(cmdLine).intern() == "-d") {
				deletePartition(bs, partNumber);
				bs.write(api);
				return;
			}

			if (ACTION.getValue(cmdLine).intern() == "-b") {
				toggleBootable(bs, partNumber);
				bs.write(api);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (ApiNotFoundException e) {
			e.printStackTrace();
		} catch (DeviceNotFoundException e) {
			e.printStackTrace();
		} 

	}

	private static void toggleBootable(BootSector bs, int partNumber) {
		// save the current state for the targeted partition
		boolean currentStatus = bs.getPartition(partNumber).getBootIndicator();

		// erase all the states
		for (int i = 0; i < 4; i++) {
			bs.getPartition(i).setBootIndicator(false);
		}

		// put back the reversed state for the targeted partition
		bs.getPartition(partNumber).setBootIndicator(!currentStatus);
	}

	private static void deletePartition(BootSector bs, int partNumber) {
		bs.getPartition(partNumber).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
	}

	private static void modifyPartition(String description, BlockDeviceAPI api, BootSector bs, Device dev) throws IOException {
		// arg 1 should be in the form id:start:size:fs
		StringTokenizer st = new StringTokenizer(description, ":");
		int id = Integer.parseInt(st.nextToken());
		//BUG in long
		//long start = Long.parseLong(st.nextToken());
		//long size = Long.parseLong(st.nextToken());
		int start = Integer.parseInt(st.nextToken());
		int size = Integer.parseInt(st.nextToken());
		int fs = Integer.parseInt(st.nextToken(), 16);
		System.out.println(
			"Init " + id + " with start = " + start + ", size = " + size + ", fs = " + Integer.toHexString(fs & 0xff));
		IBMPartitionTableEntry entry = bs.getPartition(id);
		entry.setBootIndicator(false);
		entry.setSystemIndicator(fs);
		entry.setStartLba(start);
		entry.setNrSectors(size);
		bs.write(api);
		
//		 restart the device
	     DeviceManager dm = null;
		 
        try {
			dm = InitialNaming.lookup(DeviceManager.NAME);
			dm.stop(dev);
	        dm.start(dev);
			
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
		} catch (DeviceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		return;
	}

	private static void initMbr(String device, DeviceManager dm)
		throws DeviceNotFoundException, ApiNotFoundException, IOException {
		IDEDevice current = (IDEDevice)dm.getDevice(device);
		BlockDeviceAPI api = current.getAPI(BlockDeviceAPI.class);
		ByteBuffer MBR = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
		api.read(0, MBR);

		System.out.println("Initialize MBR ...");

		GrubBootSector newMBR = new GrubBootSector(PLAIN_MASTER_BOOT_SECTOR);

		if (IBMPartitionTable.containsPartitionTable(MBR.array())) {
			BootSector oldMBR = new BootSector(MBR.array());
			System.out.println("This device already contains a partition table. Copy the already existing partitions.");
			for (int i = 0; i < 4; i++) {
				IBMPartitionTableEntry entry = newMBR.getPartition(i);
				entry.setBootIndicator(oldMBR.getPartition(i).getBootIndicator());
				entry.setStartLba(oldMBR.getPartition(i).getStartLba());
				entry.setNrSectors(oldMBR.getPartition(i).getNrSectors());
				entry.setSystemIndicator(oldMBR.getPartition(i).getSystemIndicator());

			}
		} else {
			newMBR.getPartition(0).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			newMBR.getPartition(1).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			newMBR.getPartition(2).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			newMBR.getPartition(3).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
		}
		newMBR.write(api);
	}

	private static void printTable(String deviceName, DeviceManager dm)
		throws DeviceNotFoundException, ApiNotFoundException, IOException {
		{
			IDEDevice current = (IDEDevice)dm.getDevice(deviceName);
			BlockDeviceAPI api = current.getAPI(BlockDeviceAPI.class);
			IDEDriveDescriptor descriptor = current.getDescriptor();
			ByteBuffer MBR = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
			api.read(0, MBR);
			if (IBMPartitionTable.containsPartitionTable(MBR.array())) {
				IBMPartitionTable partitionTable = new IBMPartitionTable(new IBMPartitionTableType(), MBR.array(), current);
				int nbPartitions = partitionTable.getLength();

				System.out.println(
					"Disk : " + current.getId() + ": " + descriptor.getSectorsIn28bitAddressing() * 512 + " bytes");
				System.out.println("Device Boot    Start       End    Blocks   System");

				for (int i = 0; i < nbPartitions; i++) {
					IBMPartitionTableEntry entry = (IBMPartitionTableEntry)partitionTable.getEntry(i);
					int si = entry.getSystemIndicator();
					if (si != 0)
						System.out.println(
							"ID "
								+ i
								+ " "
								+ (entry.getBootIndicator() ? "Boot" : "No")
								+ "    "
								+ entry.getStartLba()
								+ "    "
								+ (entry.getStartLba() + entry.getNrSectors())
								+ "    "
								+ entry.getNrSectors()
								+ "    "
								+ Integer.toHexString(si));
					if(entry.isExtended()) {
						final List<IBMPartitionTableEntry> exPartitions = partitionTable.getExtendedPartitions();
						int j = 0;
						for (IBMPartitionTableEntry exEntry : exPartitions) {
							si = exEntry.getSystemIndicator();
							System.out.println(
									"ID "
									+ i
									+ " "
									+ (exEntry.getBootIndicator() ? "Boot" : "No")
									+ "    "
									+ exEntry.getStartLba()
									+ "    "
									+ "-----"//(exEntry.getStartLba() + entry.getNrSectors())
									+ "    "
									+ "-----"//exEntry.getNrSectors()
									+ "    "
									+ Integer.toHexString(si));
							j++;
						}
					}
						
				}

			} else {
				System.out.println(" No valid MBR found on this device. Use --initmbr to initialize it.");
			}
		}
	}

	private static void listAvailableDevice(DeviceManager dm) {
		final Collection<Device> allDevices = dm.getDevicesByAPI(BlockDeviceAPI.class);
        for (Device current : allDevices) {
			System.out.println("Found device : " + current.getId() + "[" + current.getClass() + "]");

			if (current instanceof IDEDevice) {
				IDEDevice ideDevice = (IDEDevice)current;
				IDEDriveDescriptor currentDescriptor = ideDevice.getDescriptor();
				if (currentDescriptor.isDisk()) {
					System.out.println(
						"    IDE Disk : "
							+ ideDevice.getId()
							+ "("
							+ currentDescriptor.getModel()
							+ " "
							+ currentDescriptor.getSectorsIn28bitAddressing() * IDEConstants.SECTOR_SIZE
							+ ")");
				}
			}
		}
	}

	private static final byte PLAIN_MASTER_BOOT_SECTOR[] =
		{
			(byte)0xEB,
			(byte)0x48,
			(byte)0x90,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x03,
			(byte)0x02,
			(byte)0xFF,
			(byte)0x00,
			(byte)0x00,
			(byte)0x80,
			(byte)0x01,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x08,
			(byte)0xFA,
			(byte)0xEA,
			(byte)0x50,
			(byte)0x7C,
			(byte)0x00,
			(byte)0x00,
			(byte)0x31,
			(byte)0xC0,
			(byte)0x8E,
			(byte)0xD8,
			(byte)0x8E,
			(byte)0xD0,
			(byte)0xBC,
			(byte)0x00,
			(byte)0x20,
			(byte)0xFB,
			(byte)0xA0,
			(byte)0x40,
			(byte)0x7C,
			(byte)0x3C,
			(byte)0xFF,
			(byte)0x74,
			(byte)0x02,
			(byte)0x88,
			(byte)0xC2,
			(byte)0x52,
			(byte)0xBE,
			(byte)0x76,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0x34,
			(byte)0x01,
			(byte)0xF6,
			(byte)0xC2,
			(byte)0x80,
			(byte)0x74,
			(byte)0x54,
			(byte)0xB4,
			(byte)0x41,
			(byte)0xBB,
			(byte)0xAA,
			(byte)0x55,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x5A,
			(byte)0x52,
			(byte)0x72,
			(byte)0x49,
			(byte)0x81,
			(byte)0xFB,
			(byte)0x55,
			(byte)0xAA,
			(byte)0x75,
			(byte)0x43,
			(byte)0xA0,
			(byte)0x41,
			(byte)0x7C,
			(byte)0x84,
			(byte)0xC0,
			(byte)0x75,
			(byte)0x05,
			(byte)0x83,
			(byte)0xE1,
			(byte)0x01,
			(byte)0x74,
			(byte)0x37,
			(byte)0x66,
			(byte)0x8B,
			(byte)0x4C,
			(byte)0x10,
			(byte)0xBE,
			(byte)0x05,
			(byte)0x7C,
			(byte)0xC6,
			(byte)0x44,
			(byte)0xFF,
			(byte)0x01,
			(byte)0x66,
			(byte)0x8B,
			(byte)0x1E,
			(byte)0x44,
			(byte)0x7C,
			(byte)0xC7,
			(byte)0x04,
			(byte)0x10,
			(byte)0x00,
			(byte)0xC7,
			(byte)0x44,
			(byte)0x02,
			(byte)0x01,
			(byte)0x00,
			(byte)0x66,
			(byte)0x89,
			(byte)0x5C,
			(byte)0x08,
			(byte)0xC7,
			(byte)0x44,
			(byte)0x06,
			(byte)0x00,
			(byte)0x70,
			(byte)0x66,
			(byte)0x31,
			(byte)0xC0,
			(byte)0x89,
			(byte)0x44,
			(byte)0x04,
			(byte)0x66,
			(byte)0x89,
			(byte)0x44,
			(byte)0x0C,
			(byte)0xB4,
			(byte)0x42,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x72,
			(byte)0x05,
			(byte)0xBB,
			(byte)0x00,
			(byte)0x70,
			(byte)0xEB,
			(byte)0x7D,
			(byte)0xB4,
			(byte)0x08,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x73,
			(byte)0x0A,
			(byte)0xF6,
			(byte)0xC2,
			(byte)0x80,
			(byte)0x0F,
			(byte)0x84,
			(byte)0xF3,
			(byte)0x00,
			(byte)0xE9,
			(byte)0x8D,
			(byte)0x00,
			(byte)0xBE,
			(byte)0x05,
			(byte)0x7C,
			(byte)0xC6,
			(byte)0x44,
			(byte)0xFF,
			(byte)0x00,
			(byte)0x66,
			(byte)0x31,
			(byte)0xC0,
			(byte)0x88,
			(byte)0xF0,
			(byte)0x40,
			(byte)0x66,
			(byte)0x89,
			(byte)0x44,
			(byte)0x04,
			(byte)0x31,
			(byte)0xD2,
			(byte)0x88,
			(byte)0xCA,
			(byte)0xC1,
			(byte)0xE2,
			(byte)0x02,
			(byte)0x88,
			(byte)0xE8,
			(byte)0x88,
			(byte)0xF4,
			(byte)0x40,
			(byte)0x89,
			(byte)0x44,
			(byte)0x08,
			(byte)0x31,
			(byte)0xC0,
			(byte)0x88,
			(byte)0xD0,
			(byte)0xC0,
			(byte)0xE8,
			(byte)0x02,
			(byte)0x66,
			(byte)0x89,
			(byte)0x04,
			(byte)0x66,
			(byte)0xA1,
			(byte)0x44,
			(byte)0x7C,
			(byte)0x66,
			(byte)0x31,
			(byte)0xD2,
			(byte)0x66,
			(byte)0xF7,
			(byte)0x34,
			(byte)0x88,
			(byte)0x54,
			(byte)0x0A,
			(byte)0x66,
			(byte)0x31,
			(byte)0xD2,
			(byte)0x66,
			(byte)0xF7,
			(byte)0x74,
			(byte)0x04,
			(byte)0x88,
			(byte)0x54,
			(byte)0x0B,
			(byte)0x89,
			(byte)0x44,
			(byte)0x0C,
			(byte)0x3B,
			(byte)0x44,
			(byte)0x08,
			(byte)0x7D,
			(byte)0x3C,
			(byte)0x8A,
			(byte)0x54,
			(byte)0x0D,
			(byte)0xC0,
			(byte)0xE2,
			(byte)0x06,
			(byte)0x8A,
			(byte)0x4C,
			(byte)0x0A,
			(byte)0xFE,
			(byte)0xC1,
			(byte)0x08,
			(byte)0xD1,
			(byte)0x8A,
			(byte)0x6C,
			(byte)0x0C,
			(byte)0x5A,
			(byte)0x8A,
			(byte)0x74,
			(byte)0x0B,
			(byte)0xBB,
			(byte)0x00,
			(byte)0x70,
			(byte)0x8E,
			(byte)0xC3,
			(byte)0x31,
			(byte)0xDB,
			(byte)0xB8,
			(byte)0x01,
			(byte)0x02,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x72,
			(byte)0x2A,
			(byte)0x8C,
			(byte)0xC3,
			(byte)0x8E,
			(byte)0x06,
			(byte)0x48,
			(byte)0x7C,
			(byte)0x60,
			(byte)0x1E,
			(byte)0xB9,
			(byte)0x00,
			(byte)0x01,
			(byte)0x8E,
			(byte)0xDB,
			(byte)0x31,
			(byte)0xF6,
			(byte)0x31,
			(byte)0xFF,
			(byte)0xFC,
			(byte)0xF3,
			(byte)0xA5,
			(byte)0x1F,
			(byte)0x61,
			(byte)0xFF,
			(byte)0x26,
			(byte)0x42,
			(byte)0x7C,
			(byte)0xBE,
			(byte)0x7C,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0x40,
			(byte)0x00,
			(byte)0xEB,
			(byte)0x0E,
			(byte)0xBE,
			(byte)0x81,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0x38,
			(byte)0x00,
			(byte)0xEB,
			(byte)0x06,
			(byte)0xBE,
			(byte)0x8B,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0x30,
			(byte)0x00,
			(byte)0xBE,
			(byte)0x90,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0x2A,
			(byte)0x00,
			(byte)0xEB,
			(byte)0xFE,
			(byte)0x47,
			(byte)0x52,
			(byte)0x55,
			(byte)0x42,
			(byte)0x20,
			(byte)0x00,
			(byte)0x47,
			(byte)0x65,
			(byte)0x6F,
			(byte)0x6D,
			(byte)0x00,
			(byte)0x48,
			(byte)0x61,
			(byte)0x72,
			(byte)0x64,
			(byte)0x20,
			(byte)0x44,
			(byte)0x69,
			(byte)0x73,
			(byte)0x6B,
			(byte)0x00,
			(byte)0x52,
			(byte)0x65,
			(byte)0x61,
			(byte)0x64,
			(byte)0x00,
			(byte)0x20,
			(byte)0x45,
			(byte)0x72,
			(byte)0x72,
			(byte)0x6F,
			(byte)0x72,
			(byte)0x00,
			(byte)0xBB,
			(byte)0x01,
			(byte)0x00,
			(byte)0xB4,
			(byte)0x0E,
			(byte)0xCD,
			(byte)0x10,
			(byte)0xAC,
			(byte)0x3C,
			(byte)0x00,
			(byte)0x75,
			(byte)0xF4,
			(byte)0xC3,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x24,
			(byte)0x12,
			(byte)0x0F,
			(byte)0x09,
			(byte)0x00,
			(byte)0xBE,
			(byte)0xBD,
			(byte)0x7D,
			(byte)0x31,
			(byte)0xC0,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x46,
			(byte)0x8A,
			(byte)0x0C,
			(byte)0x80,
			(byte)0xF9,
			(byte)0x00,
			(byte)0x75,
			(byte)0x0F,
			(byte)0xBE,
			(byte)0xDA,
			(byte)0x7D,
			(byte)0xE8,
			(byte)0xC6,
			(byte)0xFF,
			(byte)0xEB,
			(byte)0x94,
			(byte)0x46,
			(byte)0x6C,
			(byte)0x6F,
			(byte)0x70,
			(byte)0x70,
			(byte)0x79,
			(byte)0x00,
			(byte)0xBB,
			(byte)0x00,
			(byte)0x70,
			(byte)0xB8,
			(byte)0x01,
			(byte)0x02,
			(byte)0xB5,
			(byte)0x00,
			(byte)0xB6,
			(byte)0x00,
			(byte)0xCD,
			(byte)0x13,
			(byte)0x72,
			(byte)0xD7,
			(byte)0xB6,
			(byte)0x01,
			(byte)0xB5,
			(byte)0x4F,
			(byte)0xE9,
			(byte)0xDD,
			(byte)0xFE,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x00,
			(byte)0x55,
			(byte)0xAA };

}
