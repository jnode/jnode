package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class AllocationTable {
	private final GrainDirectory grainDirectory;
	private final GrainTable[] grainTables;
	
	public AllocationTable(RandomAccessFile raf, SparseExtentHeader header)
						throws IOException
	{
		long nbGrains = header.getCapacity() / header.getGrainSize();
		int nbGrainTables = (int) (nbGrains / header.getNumGTEsPerGT());
		
		grainDirectory = new GrainDirectory(raf, nbGrainTables);		
		grainTables = new GrainTable[nbGrainTables];
		for(int i = 0 ; i < grainTables.length ; i++)
		{
			grainTables[i] = new GrainTable(raf, header.getNumGTEsPerGT());
		}
	}	
	
	public void write(RandomAccessFile raf) throws IOException
	{
		grainDirectory.write(raf);
		for(GrainTable gt : grainTables)
		{
			gt.write(raf);
		}
	}

	public GrainDirectory getGrainDirectory() {
		return grainDirectory;
	}

	public int getNbGrainTables() 
	{
		return grainTables.length;
	}	

	public GrainTable getGrainTable(int tableNum) 
	{
		return grainTables[tableNum];
	}	
}
