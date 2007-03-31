/**
 * 
 */
package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.ExtentIO;
import org.jnode.apps.vmware.disk.handler.IOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseIOHandler extends IOHandler 
{
	public static final Logger LOG = Logger.getLogger(SparseIOHandler.class);
	
	protected SparseIOHandler(Descriptor descriptor) 
					throws IOException 
	{
		super(descriptor);
	}
	
	@Override
	protected SparseExtentIO createExtentIO(RandomAccessFile raf, Extent extent) {
		return new SparseExtentIO(raf, extent);
	}
}