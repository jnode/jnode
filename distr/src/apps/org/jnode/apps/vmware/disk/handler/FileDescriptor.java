package org.jnode.apps.vmware.disk.handler;

import java.io.RandomAccessFile;

import org.jnode.apps.vmware.disk.descriptor.Descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class FileDescriptor 
{
	final private Descriptor descriptor;
	final private RandomAccessFile raf;
	final private ExtentFactory factory;

	public FileDescriptor(Descriptor descriptor, RandomAccessFile raf,
							ExtentFactory factory) 
	{
		this.descriptor = descriptor;
		this.raf = raf;
		this.factory = factory;
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	public RandomAccessFile getRandomAccessFile()
	{
		return raf;
	}

	public ExtentFactory getExtentFactory() {
		return factory;
	}
}
