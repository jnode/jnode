package org.jnode.apps.jpartition.model;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.command.PartitionHelper;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;

public class DeviceModel extends AbstractModel
					implements DeviceListener
{
	private static final Logger log = Logger.getLogger(DeviceModel.class);
		
	private final List<PartitionModel> partitions = new ArrayList<PartitionModel>();
	
	public DeviceModel()
	{
		try {
			DeviceUtils.getDeviceManager().addListener(this);
		} catch (NameNotFoundException e) {
			log.error(e);
		}
	}
	
	public void setDevice(Object device) {
		propSupport.firePropertyChange("deviceSelected", null, device);
		try {
			addPartitions((IDEDevice) device);
		} catch (Exception e) {
			log.error(e);
		}
	}

	protected void addPartitions(IDEDevice device) throws Exception
	{
		PartitionHelper helper = new PartitionHelper(device);
				
		log.debug("addPartitions");
		if(device.implementsAPI(PartitionableBlockDeviceAPI.class))
		{
			log.debug("implementsAPI");
			partitions.clear();
			PartitionableBlockDeviceAPI<?> api = device.getAPI(PartitionableBlockDeviceAPI.class);
			for(PartitionTableEntry e : api.getPartitionTable())
			{
				log.debug("PartitionTableEntry");
				addPartition(e);
			}			
		}
	}
	
	public void addPartition(PartitionTableEntry e)
	{
		if(e instanceof IBMPartitionTableEntry)
		{
			IBMPartitionTableEntry pte = (IBMPartitionTableEntry) e;
			addPartition(new PartitionModel(pte));
		}
		else
		{
			log.warn("found non-IBMPartitionTableEntry");
		}		
	}
	
	public void addPartition(PartitionModel partition)
	{
		propSupport.fireIndexedPropertyChange("partitionAdded", partitions.size(), null, partition);
		partitions.add(partition);
	}

	public void removePartition(PartitionModel partition)
	{
		int index = partitions.indexOf(partition);
		propSupport.fireIndexedPropertyChange("partitionRemoved", index, null, partition);
		partitions.remove(partition);
	}

	//
	// DeviceListener interface
	//
	public void deviceStarted(Device device) {
		if(device instanceof IDEDevice)
		{
			log.debug("deviceStarted...");
			propSupport.firePropertyChange("deviceStarted", null, device);
		}
	}

	public void deviceStop(Device device) {
		log.debug("deviceStop...");
		propSupport.firePropertyChange("deviceStop", null, device);		
	}
	
	//
	//
	//	
}
