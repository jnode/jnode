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
import java.util.NoSuchElementException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.help.argument.IBMPartitionTypeArgument;
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
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.argument.LongArgument;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.SizeArgument;

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
				new OptionArgument.Option("-b", "Switch the bootable flag of a partition")});

	static final OptionArgument ACTION_MODIFY =
		new OptionArgument(
			"action",
			"Action on a specified partition",
			new OptionArgument.Option[] {
				new OptionArgument.Option("-m", "Modify/create a partition")});

	static final IntegerArgument PARTITION = new IntegerArgument("partition number", "Targeted partition");
	static final LongArgument START = new LongArgument("start", "Sector where the partition starts");
	static final SizeArgument SIZE = new SizeArgument("size", "Size of the partition in sectors or in bytes(use prefixes K, M, G, ...)");	
	static final IBMPartitionTypeArgument PARTITION_TYPE = new IBMPartitionTypeArgument(
			"partition type", "partition type code");
	
	static final DeviceArgument ARG_DEVICE =
		new DeviceArgument("device-id", "the device on which you want to change/create the partition");

	static final Parameter PARAM_INITMBR = new Parameter(INITMBR, Parameter.MANDATORY);
	static final Parameter PARAM_ACTION = new Parameter(ACTION, Parameter.MANDATORY);
	static final Parameter PARAM_ACTION_MODIFY = new Parameter(ACTION_MODIFY, Parameter.MANDATORY);
	static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE, Parameter.MANDATORY);
	static final Parameter PARAM_PARTITION = new Parameter(PARTITION, Parameter.MANDATORY);
	static final Parameter PARAM_START = new Parameter(START, Parameter.MANDATORY);
	static final Parameter PARAM_SIZE = new Parameter(SIZE, Parameter.MANDATORY);
	static final Parameter PARAM_PARTITION_TYPE = new Parameter(PARTITION_TYPE, Parameter.MANDATORY);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"fdisk",
			new Syntax[] {
				new Syntax("Lists the available devices"),
				new Syntax("Print the partition table of a device", 
							new Parameter[] { 
								PARAM_DEVICE }),
				new Syntax("Initialize the MBR of a device", 
							new Parameter[] { 
								PARAM_INITMBR, PARAM_DEVICE }),
				new Syntax("Change a partition",
							new Parameter[] { 
								PARAM_ACTION_MODIFY, PARAM_PARTITION, PARAM_START, 
								PARAM_SIZE, PARAM_PARTITION_TYPE, 
								PARAM_DEVICE }),
				new Syntax("Delete a partition / switch bootable flag", 
							new Parameter[] { 
								PARAM_ACTION, PARAM_PARTITION, PARAM_DEVICE }),
	});

	public static void main(String[] args) throws SyntaxErrorException {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		DeviceManager dm;
		try {
			dm = InitialNaming.lookup(DeviceManager.NAME);

			boolean isAction = PARAM_ACTION.isSet(cmdLine);
			boolean isActionModify = PARAM_ACTION_MODIFY.isSet(cmdLine);
			boolean isInitMBR = PARAM_INITMBR.isSet(cmdLine);
			boolean isDevice = PARAM_DEVICE.isSet(cmdLine);

			// no parameters
			if (!isDevice) {
				listAvailableDevice(dm);
				return;
			}

			// only device is set
			if (!isAction && !isActionModify && !isInitMBR && isDevice) {
				printTable(ARG_DEVICE.getValue(cmdLine), dm);
				return;
			}

			final String deviceId = ARG_DEVICE.getValue(cmdLine);
			final PartitionHelper helper = new PartitionHelper(deviceId); 
			
			// initMBR
			if (isInitMBR) {
				helper.initMbr();
				helper.write();
				return;
			}

			int partNumber = getPartitionNumber(helper, cmdLine);

			// modify a partition ?
			if (ACTION_MODIFY.getValue(cmdLine).intern() == "-m") {				
				modifyPartition(helper, partNumber, cmdLine);
				helper.write();
				return;
			}

			// delete a partition ?
			if (ACTION.getValue(cmdLine).intern() == "-d") {
				helper.deletePartition(partNumber);
				helper.write();
				return;
			}

			// toggle boot flag for a partition ?
			if (ACTION.getValue(cmdLine).intern() == "-b") {
				helper.toggleBootable(partNumber);
				helper.write();
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
	
	private static int getPartitionNumber(PartitionHelper helper, ParsedArguments cmdLine)
	{
		int partNumber = PARTITION.getInteger(cmdLine);

		if ((partNumber >= helper.getNbPartitions()) || 
			(partNumber < 0) )
			throw new IllegalArgumentException("Partition number is invalid");
		
		return partNumber;
	}

	private static void modifyPartition(PartitionHelper helper, 
										int id, 
										ParsedArguments cmdLine) 
							throws IOException 
	{
		long start = START.getLong(cmdLine);
		long size = SIZE.getLong(cmdLine);
		IBMPartitionTypes type = PARTITION_TYPE.getArgValue(cmdLine);
			
//		try {
			System.out.println("D");
			System.out.println("Init " + id + " with start = " + start
					+ ", size = " + size + ", fs = "
					+ Integer.toHexString(type.getCode() & 0xff));
			System.out.println("E");			
			boolean sizeUnit = SIZE.hasSizeUnit(cmdLine) ? 
								PartitionHelper.BYTES : PartitionHelper.SECTORS; 
			helper.modifyPartition(id, false, start, size, sizeUnit, type);
			System.out.println("F");
//		} 
//		catch (NumberFormatException nfe) 
//		{
//			System.err.println("not an integer");
//			System.err.println(helpMsg);
//		}
//		catch (NoSuchElementException nsee) 
//		{
//			System.err.println("not enough elements");
//			System.err.println(helpMsg);
//		}
//		catch (IllegalArgumentException iae) 
//		{
//			System.err.println(iae.getMessage());
//			System.err.println(helpMsg);
//		}
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

				System.out.println(
					"Disk : " + current.getId() + ": " + descriptor.getSectorsIn28bitAddressing() * 512 + " bytes");
				System.out.println("Device Boot    Start       End    Blocks   System");

				int i = 0;
				for (IBMPartitionTableEntry entry : partitionTable) {
					//IBMPartitionTableEntry entry = (IBMPartitionTableEntry)partitionTable.getEntry(i);
					IBMPartitionTypes si = entry.getSystemIndicator();
					if (si != IBMPartitionTypes.PARTTYPE_EMPTY)
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
								+ si);
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
									+ si);
							j++;
						}
					}
					i++;	
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
}
