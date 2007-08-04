package org.jnode.apps.vmware.disk.handler.sparse;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.test.readwrite.TestVMWareDisk;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class AllocationTable {
	private static final Logger LOG = Logger.getLogger(TestVMWareDisk.class);
		
	private final GrainDirectory grainDirectory;
	private final GrainTable[] grainTables;
	
	public AllocationTable(GrainDirectory grainDirectory, GrainTable[] grainTables)
	{
		this.grainDirectory = grainDirectory;		
		this.grainTables = grainTables;
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
		if((tableNum < 0) || (tableNum >= grainTables.length))
		{
			//TODO fix the bug
			LOG.fatal("getGrainTable: FATAL: index out of bounds, actual="+tableNum+", max="+(grainTables.length-1)+", using max");
			tableNum = (grainTables.length - 1);
		}				
		return grainTables[tableNum];
	}	
}
