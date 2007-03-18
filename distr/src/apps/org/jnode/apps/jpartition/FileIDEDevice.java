package org.jnode.apps.jpartition;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.driver.bus.ide.IDEDriverUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableType;

public class FileIDEDevice extends IDEDevice implements PartitionableBlockDeviceAPI 
{
	private VMWareDisk vmwareDisk;
	private PartitionTable pt;
	
	public FileIDEDevice(File file, long fileSize, 
				boolean primary, boolean master) 
			throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException 
	{
		super(null, primary, master, file.getName(), null, null);
		registerAPI(PartitionableBlockDeviceAPI.class, this);

		vmwareDisk = new VMWareDisk(file);
		
		setDriver(new FileIDEDeviceDriver());
		
		pt = buildPartitionTable();
	}

	public void flush() throws IOException {
		vmwareDisk.flush();
	}

	public long getLength() throws IOException {
		return vmwareDisk.getLength();
	}

	public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, destBuf.remaining());
        
        vmwareDisk.read(devOffset, destBuf);
	}

	public void write(long devOffset, ByteBuffer srcBuf) throws IOException {		
        BlockDeviceAPIHelper.checkBounds(this, devOffset, srcBuf.remaining());

        vmwareDisk.write(devOffset, srcBuf);
	}
	
	public String toString()
	{
		return getId();
	}

	public PartitionTable getPartitionTable() throws IOException {
		return pt;
	}

	public int getSectorSize() throws IOException {
		return IDEConstants.SECTOR_SIZE;
	}	
	
	protected PartitionTable buildPartitionTable() throws DriverException, IOException, NameNotFoundException
	{
		// Read the bootsector
		final byte[] bs = new byte[IDEConstants.SECTOR_SIZE];
		read(0, ByteBuffer.wrap(bs));

		return new IBMPartitionTable(new IBMPartitionTableType(), bs, this);
	}
}
