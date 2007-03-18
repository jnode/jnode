package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class GrainTable extends EntryArray 
{
	public GrainTable(RandomAccessFile raf, int nbEntries) throws IOException 
	{
		super(raf, nbEntries);
	}
}
