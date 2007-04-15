package org.jnode.apps.jpartition.utils.device;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.apps.vmware.disk.tools.DiskFactory;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.driver.bus.ide.IDEDriverUtils;
import org.jnode.naming.InitialNaming;

public class FileIDEDevice extends AbstractIDEDevice implements PartitionableBlockDeviceAPI 
{
	private VMWareDisk vmwareDisk;
	
	public FileIDEDevice(String name,  
				boolean primary, boolean master, 
				VMWareDisk vmwareDisk) 
			throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException 
	{
		super(name, primary, master);
		registerAPI(PartitionableBlockDeviceAPI.class, this);

		this.vmwareDisk = vmwareDisk;
		
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
}
