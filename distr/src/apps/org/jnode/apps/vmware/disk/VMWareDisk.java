package org.jnode.apps.vmware.disk;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.descriptor.DiskDatabase;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.apps.vmware.disk.handler.sparse.SparseIOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class VMWareDisk
{
	private static final Logger LOG = Logger.getLogger(VMWareDisk.class);
		
	private final Descriptor descriptor;
	private final IOHandler handler;
	
	public VMWareDisk(File file) throws IOException, UnsupportedFormatException
	{
		FileDescriptor fd = IOUtils.readFileDescriptor(file, true);
		
		ExtentFactory factory = fd.getExtentFactory();
				
		this.handler = factory.createIOHandler(fd);
		this.descriptor = fd.getDescriptor();
		
		LOG.debug("handler for file "+file.getName()+" : "+handler.getClass().getName());
	}
	
	public void write(long sector, ByteBuffer data) throws IOException
	{
		handler.write(sector, data);
	}

	public void read(long sector, ByteBuffer data) throws IOException
	{
		handler.read(sector, data);
	}
	
	public void flush() throws IOException
	{	
		handler.flush();
	}

	public long getLength() {
		DiskDatabase ddb = descriptor.getDiskDatabase(); 
		return ddb.getCylinders() * ddb.getHeads() * ddb.getSectors() * 
				IOHandler.SECTOR_SIZE;
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	
}
