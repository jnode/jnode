package org.jnode.apps.vmware.disk.tools;

import java.io.File;

import org.jnode.apps.vmware.disk.handler.sparse.SparseDiskFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
abstract public class DiskFactory 
{
	private static final SparseDiskFactory SPARSE_FACTORY = new SparseDiskFactory();
	
	public static File createSparseDisk(File directory, String name, long size)
	{
		return SPARSE_FACTORY.createDisk(directory, name, size);
	}
	
	public File createDisk(File directory, String name, long size)
	{
		if(!directory.isDirectory())
		{
			throw new IllegalArgumentException(directory.getAbsolutePath()+" is not a directory");
		}
		if(!directory.canWrite())
		{
			throw new IllegalArgumentException(directory.getAbsolutePath()+" must be writable");
		}
		
		File mainFile = new File(directory, name);
		if(!createDiskImpl(mainFile, size))
		{
			mainFile = null;
		}
		return mainFile;
	}

	abstract protected boolean createDiskImpl(File mainFile, long size);
}
