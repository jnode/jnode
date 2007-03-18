package org.jnode.apps.jpartition.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;

public class DevicePartitions
{
	private static final Logger log = Logger.getLogger(DevicePartitions.class);
		
	private final Device device;
	private final List<Partition> partitions = new ArrayList<Partition>();
	
	public DevicePartitions(Device device)
	{
		this.device = device;
		try {
			addPartitions(device);
		} catch (Exception e) {
			log.error(e);
		}
	}
		
	public List<Partition> getPartitions()
	{
		return partitions;
	}
	
	protected void addPartitions(Device device) throws Exception
	{
		System.err.println("addPartitions");
		if(device.implementsAPI(PartitionableBlockDeviceAPI.class))
		{
			System.err.println("implementsAPI");
			partitions.clear();
			PartitionableBlockDeviceAPI<?> api = device.getAPI(PartitionableBlockDeviceAPI.class);
			for(PartitionTableEntry e : api.getPartitionTable())
			{
				System.err.println("PartitionTableEntry");
				addPartition(e);
			}			
		}
	}
	
	public void addPartition(PartitionTableEntry e)
	{
		if(e instanceof IBMPartitionTableEntry)
		{
			IBMPartitionTableEntry pte = (IBMPartitionTableEntry) e;
			partitions.add(new Partition(pte));
		}
		else
		{
			log.warn("found non-IBMPartitionTableEntry");
		}		
	}
	
	public String toString()
	{
		return device.getId();
	}

	public Device getDevice() {
		return device;
	}
}
