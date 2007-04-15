package org.jnode.partitions.command;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.fs.fat.BootSector;
import org.jnode.fs.fat.GrubBootSector;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class PartitionHelper 
{	
	public static final boolean BYTES = true;
	public static final boolean SECTORS = false;
	
	private final IDEDevice current;
	private final BlockDeviceAPI api;
	
	private ByteBuffer MBR;	
	private BootSector bs;

	public PartitionHelper(String deviceId) 
			throws DeviceNotFoundException, ApiNotFoundException, IOException, NameNotFoundException
	{
		this((IDEDevice)DeviceUtils.getDeviceManager().getDevice(deviceId));
	}

	public PartitionHelper(IDEDevice device) 
				throws DeviceNotFoundException, ApiNotFoundException, IOException
	{
		this.current = device;
		this.api = current.getAPI(BlockDeviceAPI.class);

		reloadMBR();
	}

	
	public void initMbr() throws DeviceNotFoundException, ApiNotFoundException, 
									IOException 
	{
		System.out.println("Initialize MBR ...");
	
		BootSector oldMBR = bs;
		bs = new GrubBootSector(PLAIN_MASTER_BOOT_SECTOR);
	
		if (IBMPartitionTable.containsPartitionTable(MBR.array())) {
			System.out.println("This device already contains a partition table. Copy the already existing partitions.");
			
			for (int i = 0; i < 4; i++) {
				final IBMPartitionTableEntry oldEntry = oldMBR.getPartition(i);
				modifyPartition(i, oldEntry.getBootIndicator(), oldEntry.getStartLba(), 
								oldEntry.getNrSectors(), SECTORS, 
								oldEntry.getSystemIndicator());
			}
		} else {
			bs.getPartition(0).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			bs.getPartition(1).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			bs.getPartition(2).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
			bs.getPartition(3).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
		}
		
		//reloadMBR();
	}
	
	public void write() throws IOException
	{
		bs.write(api);
		
		reloadMBR();
		
		// restart the device
	    try {
	    	DeviceManager devMan = DeviceUtils.getDeviceManager();
	    	devMan.stop(current);
	    	devMan.start(current);
		} catch (DeviceNotFoundException e) {
			e.printStackTrace();
		} catch (DriverException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean hasValidMBR()
	{
		return IBMPartitionTable.containsPartitionTable(MBR.array());	
	}
	
	private void reloadMBR() throws IOException
	{
		this.MBR = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
		api.read(0, MBR);	
		bs = new BootSector(MBR.array());
	}
	
	private void checkMBR() throws IOException
	{
		if (!hasValidMBR())
			throw new IOException("This device doesn't contain a valid MBR, use --initmbr.");	
	}

	public void modifyPartition(int id, boolean bootIndicator, 
								long start, long size, 
								boolean sizeUnit,
								IBMPartitionTypes fs) 
			throws IOException 
	{
		checkMBR();
		
		long nbSectors = size;
		if(sizeUnit == BYTES)
		{
			nbSectors = size / IDEConstants.SECTOR_SIZE;
			if((size % IDEConstants.SECTOR_SIZE) > 0)
			{
				nbSectors++;
			}
		}
		
		IBMPartitionTableEntry entry = bs.getPartition(id);
		entry.setBootIndicator(bootIndicator);
		entry.setSystemIndicator(fs);
		entry.setStartLba(start);
		entry.setNrSectors(nbSectors);
	}
	
	public int getNbPartitions()
	{
		return bs.getNbPartitions();
	}
	
	public void deletePartition(int partNumber) throws IOException 
	{
		checkMBR();
		bs.getPartition(partNumber).setSystemIndicator(IBMPartitionTypes.PARTTYPE_EMPTY);
	}
	
	public void toggleBootable(int partNumber) throws IOException 
	{
		checkMBR();
		
		// save the current state for the targeted partition
		boolean currentStatus = bs.getPartition(partNumber).getBootIndicator();

		// erase all the states
		for (int i = 0; i < 4; i++) {
			bs.getPartition(i).setBootIndicator(false);
		}

		// put back the reversed state for the targeted partition
		bs.getPartition(partNumber).setBootIndicator(!currentStatus);
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
