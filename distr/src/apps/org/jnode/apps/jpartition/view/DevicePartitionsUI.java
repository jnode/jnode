package org.jnode.apps.jpartition.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jnode.apps.jpartition.model.DevicePartitions;
import org.jnode.apps.jpartition.model.DevicePartitionsList;
import org.jnode.apps.jpartition.model.Partition;

public class DevicePartitionsUI extends JPanel {
	private DevicePartitionsList devicePartitions;
	
	private DefaultComboBoxModel cboDevicesModel = new DefaultComboBoxModel(); 
	private JComboBox cboDevices = new JComboBox(cboDevicesModel);
	private JPanel partitionsPanel = new JPanel(new FlowLayout());
	
	public DevicePartitionsUI(DevicePartitionsList devicePartitions)
	{
		super(new BorderLayout());
		this.devicePartitions = devicePartitions;
		
		add(cboDevices, BorderLayout.NORTH);
		add(partitionsPanel, BorderLayout.CENTER);
		
		for(DevicePartitions devParts : devicePartitions)
		{
			cboDevicesModel.addElement(devParts);
		}
		
		if(cboDevicesModel.getSize() > 0)
		{
			cboDevices.setSelectedIndex(0);
		}
		
		cboDevices.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange() == ItemEvent.SELECTED)
				{
					System.err.println("itemStateChanged");
					DevicePartitions devParts = (DevicePartitions) event.getItem();
					partitionsPanel.removeAll();
	
					for(Partition partition : devParts.getPartitions())
					{
						partitionsPanel.add(new PartitionUI(partition));
					}
				}
			}
		});
	}

	public void deviceAdded(DevicePartitions devParts) {
		System.err.println("deviceAdded");
		cboDevicesModel.addElement(devParts);
	}

	public void deviceRemoved(DevicePartitions devParts) {
		cboDevicesModel.removeElement(devParts);
	}
}
