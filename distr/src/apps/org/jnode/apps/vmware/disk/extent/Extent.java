package org.jnode.apps.vmware.disk.extent;

import java.io.File;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class Extent {
	private static final Logger LOG = Logger.getLogger(Extent.class);
		
	private Descriptor descriptor;
	
	final private Access access;
	final private long sizeInSectors; // a sector is 512 bytes
	final private ExtentType extentType;
	final private String fileName; // relative to the location of the descriptor
	final private File file;
	final private long offset;
	
	public Extent(Descriptor descriptor, ExtentDeclaration extentDecl) 
	{
		this.descriptor = descriptor;
		this.access = extentDecl.getAccess();
		this.sizeInSectors = extentDecl.getSizeInSectors();
		this.extentType = extentDecl.getExtentType();
		this.fileName = extentDecl.getFileName();
		this.file = extentDecl.getExtentFile();
		this.offset = extentDecl.getOffset();
		
		LOG.debug("created extent for file "+file.getAbsolutePath()+
				  " offset="+offset+" fileSize="+file.length());
	}
	
	public Access getAccess() {
		return access;
	}
	
	public long getSizeInSectors() {
		return sizeInSectors;
	}
	
	public ExtentType getExtentType() {
		return extentType;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public long getOffset() {
		return offset;
	}
	
	final public Descriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		if(this.descriptor != null)
		{
			throw new IllegalStateException("descriptor already assigned");
		}
		
		this.descriptor = descriptor;
	}	
	
	
	
	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return "Extent["+fileName+"]";
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(!(obj instanceof Extent))
		{
			return false;
		}
		
		Extent e = (Extent) obj;
		
		return this.access.equals(e.access) &&
				(this.sizeInSectors == e.sizeInSectors) &&
				this.extentType.equals(e.extentType) &&
				this.fileName.equals(e.fileName) &&
				this.file.equals(e.file) &&
				(this.offset == e.offset);
	}
}
