package org.jnode.apps.vmware.disk;

import java.io.File;
import org.jnode.apps.vmware.disk.extent.Access;
import org.jnode.apps.vmware.disk.extent.ExtentType;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class ExtentDeclaration {
	private final Access access;
	private final long sizeInSectors;	
	private final ExtentType extentType;
	private final String fileName;
	private final File extentFile;
	private final long offset;
	private final boolean isMainExtent;
	
	public ExtentDeclaration(Access access, long sizeInSectors,
			ExtentType extentType, String fileName, File extentFile,
			long offset, boolean isMainExtent) 
	{
		this.access = access;
		this.sizeInSectors = sizeInSectors;	
		this.extentType = extentType;
		this.fileName = fileName;
		this.extentFile = extentFile;
		this.offset = offset;
		this.isMainExtent = isMainExtent;
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

	public File getExtentFile() {
		return extentFile;
	}

	public boolean isMainExtent() {
		return isMainExtent;
	}

	public long getOffset() {
		return offset;
	}

	
}
