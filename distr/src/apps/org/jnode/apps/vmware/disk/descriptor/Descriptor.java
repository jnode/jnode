package org.jnode.apps.vmware.disk.descriptor;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class Descriptor {
	private static final Logger LOG = Logger.getLogger(Descriptor.class);
	
	final private File mainFile;
	final private Header header;
	final private List<Extent> extents;
	final private DiskDatabase diskDatabase;
	
	public Descriptor(File mainFile, Header header, List<Extent> extents,
						DiskDatabase diskDatabase) 
	{
		this.mainFile = mainFile;
		this.header = header;
		this.extents = extents;
		this.diskDatabase = diskDatabase;
	}

	public Header getHeader() {
		return header;
	}

	public List<Extent> getExtents() {
		return extents;
	}

	public DiskDatabase getDiskDatabase() {
		return diskDatabase;
	}

	public File getMainFile() {
		return mainFile;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Descriptor))
		{
			return false;
		}
		
		Descriptor desc = (Descriptor) obj;
		
		String file1 = "";
		String file2 = "";
		try {
			file1 = this.mainFile.getCanonicalPath();
			file2 = desc.mainFile.getCanonicalPath();
		} catch (Exception e) {
			LOG.error("can't compare filenames", e);
		}
		
		return file1.equals(file2) &&
			   this.header.equals(desc.header) &&
			   this.extents.equals(desc.extents) &&
			   this.diskDatabase.equals(desc.diskDatabase);
	}
	
	@Override
	public String toString() {
		String file1 = "";
		try {
			file1 = this.mainFile.getCanonicalPath();
		} catch (Exception e) {
			LOG.error("can't compare filenames", e);
		}
		
		return "Descriptor: file=" + file1 +
				","+header+",extents="+extents+
				","+diskDatabase;
	}
}
