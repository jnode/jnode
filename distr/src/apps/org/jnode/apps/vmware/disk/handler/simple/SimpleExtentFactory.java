package org.jnode.apps.vmware.disk.handler.simple;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
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
									RandomAccessFile raf) 
					throws IOException, UnsupportedFormatException
	{
		// read 2 sectors, starting from sector number 0
		Descriptor descriptor = getDescriptorRW().read(file, 0, 2);
		
		return new FileDescriptor(descriptor, raf, this); 
	}

	public SimpleIOHandler createIOHandler(FileDescriptor fileDescriptor) throws IOException, UnsupportedFormatException {
		return new SimpleIOHandler(fileDescriptor);
	}

	@Override
	protected SimpleDescriptorRW getDescriptorRW() {
		return new SimpleDescriptorRW();
	}
}
