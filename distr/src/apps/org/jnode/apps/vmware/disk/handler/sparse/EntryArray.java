package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class EntryArray 
{
	private static final Logger LOG = Logger.getLogger(EntryArray.class);
		
	final private int[] entries;
	
	public EntryArray(int[] entries)
	{
		this.entries = entries;		
	}
	
	public int getSize()
	{
		return entries.length;
	}
	
	public int getEntry(int entryNumber)
	{
		return entries[entryNumber];
	}
	
	public void setEntry(int entryNumber, int value)
	{
		entries[entryNumber] = value;
	}
}
