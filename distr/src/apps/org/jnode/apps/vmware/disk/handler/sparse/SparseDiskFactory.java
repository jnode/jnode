package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.File;

import org.jnode.apps.vmware.disk.tools.DiskFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseDiskFactory extends DiskFactory 
{
	@Override
	protected boolean createDiskImpl(File mainFile, long size) 
	{		
		return true;
	}
}
