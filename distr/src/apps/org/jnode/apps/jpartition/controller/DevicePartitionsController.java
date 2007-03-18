package org.jnode.apps.jpartition.controller;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.model.DevicePartitions;
import org.jnode.apps.jpartition.model.DevicePartitionsList;
import org.jnode.apps.jpartition.view.DevicePartitionsFrame;
import org.jnode.apps.jpartition.view.DevicePartitionsUI;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceUtils;

public class DevicePartitionsController implements DeviceListener 
{
	private static final Logger log = Logger.getLogger(DevicePartitionsController.class);
	
	private DevicePartitionsList model;
	private DevicePartitionsFrame view;
	
	public DevicePartitionsController(DevicePartitionsList model, 
									  DevicePartitionsFrame view)
	{
		this.model = model;
		this.view = view;
		
		try {
			DeviceUtils.getDeviceManager().addListener(this);
		} catch (NameNotFoundException e) {
			log.error(e);
		}
	}
	
	public void deviceStarted(Device device) {
		System.err.println("deviceStarted...");
		DevicePartitions devPart = new DevicePartitions(device);
		model.add(devPart);
		view.getDevPartsUI().deviceAdded(devPart);
	}

	public void deviceStop(Device device) {
		System.err.println("deviceStop...");
		DevicePartitions devPartsToRemove = null;
		for(DevicePartitions devParts : model)
		{
			if(device.equals(devParts.getDevice()))
			{
				devPartsToRemove = devParts;
				break;
			}
		}
		if(devPartsToRemove != null)
		{
			model.remove(devPartsToRemove);
			view.getDevPartsUI().deviceRemoved(devPartsToRemove);
		}
	}
}
