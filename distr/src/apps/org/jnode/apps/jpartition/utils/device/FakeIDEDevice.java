package org.jnode.apps.jpartition.utils.device;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.naming.NameNotFoundException;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.driver.bus.ide.IDEDriverUtils;

public class FakeIDEDevice extends AbstractIDEDevice implements PartitionableBlockDeviceAPI 
{
	private final long length;
	public FakeIDEDevice(String name,  
				boolean primary, boolean master, 
				long length) 
			throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException 
	{
		super(name, primary, master);
		this.length = length;
		
		registerAPI(PartitionableBlockDeviceAPI.class, this);

		setDriver(new FileIDEDeviceDriver());
		
		pt = buildPartitionTable();		
	}

	public void flush() throws IOException {
	}

	public long getLength() throws IOException {
		return length;
	}

	public void read(long devOffset, ByteBuffer destBuf) throws IOException {
		while(destBuf.remaining() > 0)
		{
			destBuf.put((byte) 0);
		}
	}

	public void write(long devOffset, ByteBuffer srcBuf) throws IOException {		
	}
}
