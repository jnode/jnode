package org.jnode.apps.vmware.disk.handler.sparse;

import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.extent.Extent;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseExtent extends Extent 
{
	private final SparseExtentHeader header;	
	private final AllocationTable redundantAllocationTable;
	private final AllocationTable allocationTable;
	
	public SparseExtent(Descriptor descriptor, ExtentDeclaration extentDecl,
			SparseExtentHeader header,
			AllocationTable redundantAllocationTable, 
			AllocationTable allocationTable) 
	{
		super(descriptor, extentDecl);
		
		this.header = header;	
		this.redundantAllocationTable = redundantAllocationTable;
		this.allocationTable = allocationTable;		
	}
	
/* TODO implement it later	
	public void write() throws IOException {
		raf.seek(0L);
		header.write(raf);
		redundantAllocationTable.write(raf);
		allocationTable.write(raf);
	}
*/
	@Override
	public String toString() {
		return "SparseExtent["+getFileName()+"]";
	}	
}
