package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
@RunWith(value = Parameterized.class)
public class TestVMWareDisk {
	private static final String DISKS_PATH = "/home/fabien/data/Projets/JNode/jnode/distr/src/apps/org/jnode/apps/vmware/disk/test/disks/";
	private static final String DISK_BASE_NAME = "Menuet32-";
	
	private static final String RESTRICT_TO_FILE_NAME = "Menuet32-0";
	//private static final String RESTRICT_TO_FILE_NAME = null;
	
	@Parameters
	public static List<File[]> data()	 
	{
		File directory = new File(DISKS_PATH);
		File[] files = directory.listFiles(new FilenameFilter()
				{
					public boolean accept(File dir, String name) {
						boolean ok = name.matches(DISK_BASE_NAME + "[0-9]*.vmdk");
						
						if(RESTRICT_TO_FILE_NAME != null)
						{
							ok &= name.startsWith(RESTRICT_TO_FILE_NAME);
						}
						
						return ok;
					}
				});
		List<File[]> list = new ArrayList<File[]>(files.length);
		for(File f : files)
		{
			list.add(new File[]{f});
		}
		
		return list;
	}
	 
	final private File originalDiskFile;
	private File diskFile;
	
	public TestVMWareDisk(File diskFile) throws IOException 
	{
		super();
		this.originalDiskFile = diskFile;
	}
	
	@Before
	public void setUp() throws IOException 
	{
		this.diskFile = Utils.copyDisk(originalDiskFile);
	}

	@After
	public void tearDown() throws IOException 
	{
		Utils.clearTempDir(true);
	}

	@Test
	public void read() throws Exception
	{
		VMWareDisk disk = new VMWareDisk(diskFile);
		
		ByteBuffer data = ByteBuffer.allocate(IOHandler.SECTOR_SIZE * 100);
		disk.read(0, data);
		
		Assert.assertEquals(toString()+": buffer should be filled", 0, data.remaining());
	}	 

	@Test
	public void write() throws Exception
	{
		VMWareDisk disk = new VMWareDisk(diskFile);
		
		ByteBuffer data = ByteBuffer.allocate(IOHandler.SECTOR_SIZE * 100);
		disk.write(0, data);
		
		Assert.assertEquals(toString()+": buffer should be fully copied", 0, data.remaining());
	}	 

	@Test
	public void writeAndRead() throws Exception
	{
		VMWareDisk disk = new VMWareDisk(diskFile);

		// write
		int size = IOHandler.SECTOR_SIZE * 100;
		ByteBuffer expectedData = ByteBuffer.allocate(size);
		for(int i = 0 ; i < (size / 4) ; i++)
		{
			expectedData.putInt(i);
		}
		expectedData.rewind();
		disk.write(0, expectedData);
		disk.flush();

		// read
		VMWareDisk disk2 = new VMWareDisk(diskFile);		
		Assert.assertEquals("disk has different size", disk.getLength(), disk2.getLength());
		Assert.assertEquals("disk has different descriptor", disk.getDescriptor(), disk2.getDescriptor());
		
		expectedData.rewind();
		ByteBuffer actualData = ByteBuffer.allocate(size);
		disk2.read(0, actualData);
		for(int i = 0 ; i < (size / 4) ; i++)
		{
			int actual = actualData.getInt(i);
			int expected = expectedData.getInt();
			Assert.assertEquals("bad data at index "+(i*4), expected, actual);
		}
	}	 

	@Override
	public String toString()
	{
		return diskFile.getName();
	}
}
