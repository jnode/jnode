package org.jnode.apps.jpartition.utils.device;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableType;

abstract public class AbstractIDEDevice extends IDEDevice
				implements PartitionableBlockDeviceAPI
{
	protected PartitionTable pt;

	public AbstractIDEDevice(String name,  
			boolean primary, boolean master) 
	{
		super(null, primary, master, name, null, null);		
	}

	public PartitionTable getPartitionTable() throws IOException {
		return pt;
	}

	public int getSectorSize() throws IOException {
		return IDEConstants.SECTOR_SIZE;
	}

	protected PartitionTable buildPartitionTable() throws DriverException,
			IOException, NameNotFoundException 
	{
		// Read the bootsector
		final byte[] bs = new byte[IDEConstants.SECTOR_SIZE];
		read(0, ByteBuffer.wrap(bs));
	
		return new IBMPartitionTable(new IBMPartitionTableType(), bs, this);
	}
	
	public String toString()
	{
		return getId();
	}
}