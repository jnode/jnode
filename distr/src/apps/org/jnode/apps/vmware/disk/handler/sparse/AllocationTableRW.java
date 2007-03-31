package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.handler.IOHandler;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class AllocationTableRW {
	private static final Logger LOG = Logger.getLogger(SparseExtentRW.class);
	
	public AllocationTable read(RandomAccessFile raf, SparseExtentHeader header)
						throws IOException
	{
		long nbGrains = header.getCapacity() / header.getGrainSize();
		int nbGrainTables = (int) (nbGrains / header.getNumGTEsPerGT());
		LOG.debug("read: capacity="+header.getCapacity()+" grainSize="+header.getGrainSize()+
				  " NumGTEsPerGT="+header.getNumGTEsPerGT()+" => nbGrainTables="+nbGrainTables);
		
		GrainDirectory grainDirectory = new GrainDirectory(readEntries(raf, nbGrainTables));		
		GrainTable[] grainTables = new GrainTable[nbGrainTables];
		for(int i = 0 ; i < grainTables.length ; i++)
		{
			if(LOG.isDebugEnabled())
			{
				long pos = raf.getChannel().position();
				if((pos % IOHandler.SECTOR_SIZE) != 0)
				{
					LOG.fatal("read: FATAL: pos not begin of a sector");
				}
				
				final long gtOffset = (pos / IOHandler.SECTOR_SIZE);
				final long gde = grainDirectory.getEntry(i); 
				if(gde != gtOffset)
				{
					LOG.fatal("read: FATAL: grainTables["+i+"] (value:"+gtOffset+") doesn't match to GrainDirectoryEntry #"+i+ "(value:"+gde+")");
				}
				
				raf.getChannel().position(gde);
			}
			
			grainTables[i] = new GrainTable(readEntries(raf, header.getNumGTEsPerGT()));
		}
		
		return new AllocationTable(grainDirectory, grainTables); 
	}	
	
	public void write(FileChannel channel, AllocationTable table) throws IOException
	{
		write(channel, table.getGrainDirectory());
		for(int gtNum = 0 ; gtNum < table.getNbGrainTables() ; gtNum++)
		{
			write(channel, table.getGrainTable(gtNum));
		}
	}
	
	protected void write(FileChannel channel, EntryArray ea) throws IOException
	{		
		ByteBuffer b = IOUtils.allocate(ea.getSize() * IOUtils.INT_SIZE);
		IntBuffer ib = b.asIntBuffer();		
		for(int i = 0 ; i < ea.getSize() ; i++)
		{
			ib.put(ea.getEntry(i));
		}
		ib.rewind();
		channel.write(b);
	}
	
	protected int[] readEntries(RandomAccessFile raf, int nbEntries) throws IOException
	{
		IntBuffer bb = IOUtils.getByteBuffer(raf, nbEntries * IOUtils.INT_SIZE).asIntBuffer();
		
		int[] entries = new int[nbEntries];
		for(int entryNumber = 0 ; entryNumber < nbEntries ; entryNumber++)
		{
			int entry = bb.get();
			if(entry > 0)
			{
				LOG.debug("readEntries: entry["+entryNumber+"]="+entry);
			}
			entries[entryNumber] = entry;
		}		
		return entries;
	}
}
