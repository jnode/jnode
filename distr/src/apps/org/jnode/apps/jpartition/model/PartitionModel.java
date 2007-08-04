package org.jnode.apps.jpartition.model;

import it.battlehorse.stamps.annotations.Refreshable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;


public class PartitionModel extends AbstractModel
{
	private boolean empty;
	private boolean bootable;
	private IBMPartitionTypes type;
	private long start;
	private long size;
	
	private IBMPartitionTableEntry pte;
	
	public PartitionModel(IBMPartitionTableEntry pte)
	{
		this.pte = pte;
		
		//TODO remove these fake values 
		this.empty = false;
		this.bootable = true;
		this.type = IBMPartitionTypes.PARTTYPE_WIN95_FAT32;
		this.start = 0;
		this.size = 1024;		
	}
	
	@Refreshable
	public boolean isEmpty() {
		return empty;
	}
	public void setEmpty(boolean empty) {
		propSupport.firePropertyChange("empty", this.empty, empty);
		this.empty = empty;
	}
	
	@Refreshable
	public boolean isBootable() {
		return bootable;
	}
	public void setBootable(boolean bootable) {
		propSupport.firePropertyChange("bootable", this.bootable, bootable);
		this.bootable = bootable;
	}
	
	@Refreshable
	public IBMPartitionTypes getType() {
		return type;
	}
	public void setType(IBMPartitionTypes type) {
		propSupport.firePropertyChange("type", this.type, type);
		this.type = type;
	}

	@Refreshable
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		propSupport.firePropertyChange("start", this.start, start);
		this.start = start;
	}
	
	@Refreshable	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		propSupport.firePropertyChange("size", this.size, size);
		this.size = size;
	}

	
/*	
	private final IBMPartitionTableEntry pte;
	
	public PartitionModel(IBMPartitionTableEntry pte) 
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
*/
}
