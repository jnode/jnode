/**
 * 
 */
package org.jnode.apps.vmware.disk.handler.simple;

import java.io.IOException;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.descriptor.DescriptorRW;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
final class SimpleDescriptorRW extends DescriptorRW 
{
	@Override
	public Extent createMainExtent(Descriptor desc, ExtentDeclaration extentDecl)
	{
		return new Extent(desc, extentDecl);
	}
	
	@Override
	public Extent createExtent(FileDescriptor fileDescriptor, ExtentDeclaration extentDecl)
						throws IOException, UnsupportedFormatException 
	{
		Descriptor desc = (fileDescriptor == null) ? null : 
								fileDescriptor.getDescriptor();
		return createMainExtent(desc, extentDecl);
	}
	
}