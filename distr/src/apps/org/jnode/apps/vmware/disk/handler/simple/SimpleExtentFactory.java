package org.jnode.apps.vmware.disk.handler.simple;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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
public class SimpleExtentFactory extends ExtentFactory 
{
	protected FileDescriptor createFileDescriptor(File file, 
									RandomAccessFile raf, ByteBuffer bb,
									boolean isMain) 
					throws IOException, UnsupportedFormatException
	{
		Descriptor descriptor = READER.read(file, bb, this);
		return new FileDescriptor(descriptor, raf, this); 
	}

	public Extent createMainExtent(Descriptor desc, ExtentDeclaration extentDecl)
	{
		return new Extent(desc, extentDecl);
	}
	
	public Extent createExtent(FileDescriptor fileDescriptor, ExtentDeclaration extentDecl)
						throws IOException, UnsupportedFormatException 
	{
		Descriptor desc = (fileDescriptor == null) ? null : 
								fileDescriptor.getDescriptor();
		return createMainExtent(desc, extentDecl);
	}

	public IOHandler createIOHandler(FileDescriptor fileDescriptor) throws IOException, UnsupportedFormatException {
		return new SimpleIOHandler(fileDescriptor);
	}
}
