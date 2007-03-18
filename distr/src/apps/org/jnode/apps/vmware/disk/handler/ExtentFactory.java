package org.jnode.apps.vmware.disk.handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.DescriptorReader;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
abstract public class ExtentFactory 
{
	private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);
		
	protected static final DescriptorReader READER = new DescriptorReader();

	public FileDescriptor createFileDescriptor(File file, boolean isMain) 
					throws IOException, UnsupportedFormatException
	{
		RandomAccessFile raf = null;
		FileDescriptor fileDescriptor = null;
		ByteBuffer bb = null;
		
		try {
			raf = new RandomAccessFile(file, "rw");
			bb = readHeaderForDiskType(raf);
			
			fileDescriptor = createFileDescriptor(file, raf, bb, isMain);
		}
		finally
		{
			if((fileDescriptor == null) && (raf != null))
			{
				// if there was an error but RandomAccessFile has 
				// been created, then close it
				raf.close();
			}
			else if((bb != null) && (raf != null))
			{
				// synchronize the RandomAccessFile with what 
				// has been actually read in the ByteBuffer
				raf.seek(bb.position());				
			}
		}
		
		return fileDescriptor;

	}
	
	protected ByteBuffer readHeaderForDiskType(RandomAccessFile raf) throws IOException
	{
		//TODO optimise the size
		int capacity = Math.min(1024, (int) raf.length());
		if(capacity == 0)
		{
			throw new IOException("empty file");
		}
		
		ByteBuffer bb = ByteBuffer.allocate(capacity);			
		bb.order(ByteOrder.LITTLE_ENDIAN);
		raf.getChannel().read(bb);
		bb.rewind();
		LOG.debug("bb="+bb.toString());
		return bb;
	}
	
	abstract protected FileDescriptor createFileDescriptor(File file, 
									RandomAccessFile raf, ByteBuffer bb,
									boolean isMain) 
				throws IOException, UnsupportedFormatException;

	abstract public Extent createExtent(FileDescriptor fileDescriptor, ExtentDeclaration extentDecl)  
				throws IOException, UnsupportedFormatException;
	
	abstract public Extent createMainExtent(Descriptor desc, ExtentDeclaration extentDecl) throws IOException, UnsupportedFormatException; 
	
	abstract public IOHandler createIOHandler(FileDescriptor fileDescriptor) 
				throws IOException, UnsupportedFormatException;
}
