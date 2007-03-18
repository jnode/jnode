package org.jnode.apps.jpartition.model;

import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

public class Partition 
{
	private final IBMPartitionTableEntry pte;
	
	public Partition(IBMPartitionTableEntry pte) 
	{
		this.pte = pte;		
	}
	
	public boolean isEmpty()
	{
		return pte.isEmpty();
	}
	
	public boolean isBootable()
	{
		return pte.getBootIndicator();
	}
	
	public IBMPartitionTypes getType()
	{
		return pte.getSystemIndicator();
	}
	
	public long getStart()
	{
		return pte.getStartLba();
	}
	
	public long getSize()
	{
		return pte.getNrSectors() * IDEConstants.SECTOR_SIZE;
	}
}
