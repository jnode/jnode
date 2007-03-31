package org.jnode.apps.vmware.disk.test.readwrite;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.test.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class TestHeader extends BaseReadWriteTest 
{
	private static final Logger LOG = Logger.getLogger(TestHeader.class);
	
	public TestHeader(File diskFile) throws IOException 
	{
		super(diskFile);
	}

	@Test
	public void read() throws Exception
	{
		VMWareDisk disk = new VMWareDisk(diskFile);
		disk.flush();
	}	 

	@Test
	public void write() throws Exception
	{
		VMWareDisk disk = new VMWareDisk(diskFile);
		disk.flush();
	}	 

	@Test		
	public void writeAndRead() throws Exception
	{
		Utils.DO_CLEAR = false;
		
		LOG.info("BEGIN writeAndRead");
		VMWareDisk disk = new VMWareDisk(diskFile);

		// write
		LOG.info("writeAndRead: writing...");
		disk.flush();

		// read
		LOG.info("writeAndRead: reading...");
		VMWareDisk disk2 = new VMWareDisk(diskFile);		
		Assert.assertEquals("disk has different size", disk.getLength(), disk2.getLength());
		Assert.assertEquals("disk has different descriptor", disk.getDescriptor(), disk2.getDescriptor());
		disk2.flush();
		LOG.info("END   writeAndRead");
	}	 
}
