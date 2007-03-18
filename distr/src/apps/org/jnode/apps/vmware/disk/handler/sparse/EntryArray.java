package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class EntryArray 
{
	final private int[] entries;
	
	public EntryArray(RandomAccessFile raf, int nbEntries)
				throws IOException
	{
		entries = new int[nbEntries];
		for(int entryNumber = 0 ; entryNumber < nbEntries ; entryNumber++)
		{
			setEntry(entryNumber, raf.readInt());
		}
	}
	
	public void write(RandomAccessFile raf) throws IOException
	{
		for(int entry : entries)
		{
			raf.writeInt(entry);
		}
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
