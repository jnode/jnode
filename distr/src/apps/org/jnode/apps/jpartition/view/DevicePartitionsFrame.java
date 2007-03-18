package org.jnode.apps.jpartition.view;

import javax.swing.JFrame;

import org.jnode.apps.jpartition.controller.DevicePartitionsController;
import org.jnode.apps.jpartition.model.DevicePartitionsList;

public class DevicePartitionsFrame extends JFrame {	
	private DevicePartitionsUI devPartsUI;
	
	private DevicePartitionsController controller;
	
	public DevicePartitionsFrame(DevicePartitionsList devicePartitionsList)
	{
		setTitle("JPartition");
		
		devPartsUI = new DevicePartitionsUI(devicePartitionsList);
		add(devPartsUI);		
		
		controller = new DevicePartitionsController(devicePartitionsList, this);
	}

	public DevicePartitionsUI getDevPartsUI() {
		return devPartsUI;
	}

	
}
