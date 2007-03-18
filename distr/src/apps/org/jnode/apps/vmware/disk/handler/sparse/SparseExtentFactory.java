package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseExtentFactory extends ExtentFactory 
{
	private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);
	
	protected FileDescriptor createFileDescriptor(File file, 
									RandomAccessFile raf, ByteBuffer bb,
									boolean isMain) 
				throws IOException, UnsupportedFormatException
	{
		SparseExtentHeader header = SparseExtentHeader.read(bb);			
		Descriptor embeddedDescriptor;
		if (header.getDescriptorOffset() != 0) {
			LOG.debug("isMain="+isMain);
			//embeddedDescriptor = isMain ? null : READER.read(file, bb, this);			
			embeddedDescriptor = READER.read(file, bb, this);
		} else {
			LOG.debug("embeddedDescriptor = null");
			embeddedDescriptor = null;
		}
		return new SparseFileDescriptor(embeddedDescriptor, raf, this, header);
	}
	
	public Extent createMainExtent(Descriptor desc, ExtentDeclaration extentDecl) throws IOException, UnsupportedFormatException
	{
		RandomAccessFile raf = new RandomAccessFile(extentDecl.getExtentFile(), "rw");
		ByteBuffer bb = readHeaderForDiskType(raf);
		
		SparseExtentHeader header = SparseExtentHeader.read(bb);			
		SparseFileDescriptor sfd = new SparseFileDescriptor(desc, raf, this, header);
		return createExtent(sfd, extentDecl);
	}
		
	public SparseExtent createExtent(FileDescriptor fileDescriptor, 
						ExtentDeclaration extentDecl)  
				throws IOException, UnsupportedFormatException	
	{
		SparseFileDescriptor sfd = (SparseFileDescriptor) fileDescriptor;
		LOG.debug("fileDescriptor="+fileDescriptor);
		Descriptor descriptor = (fileDescriptor == null) ? null : 
									fileDescriptor.getDescriptor();
		
		RandomAccessFile raf = fileDescriptor.getRandomAccessFile();
		SparseExtentHeader header = sfd.getHeader();
		
		AllocationTable redundantAllocationTable = new AllocationTable(raf, header);
		AllocationTable allocationTable = new AllocationTable(raf, header);
		
		return new SparseExtent(descriptor, extentDecl,
				header, redundantAllocationTable, 
				allocationTable);
	}

	public IOHandler createIOHandler(FileDescriptor fileDescriptor) throws IOException 
	{
		SparseFileDescriptor sfd = (SparseFileDescriptor) fileDescriptor;
		IOHandler handler = null;

		Descriptor desc = sfd.getDescriptor();
		handler = new SparseIOHandler(desc);
		
		return handler;
	}

}
