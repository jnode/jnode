package org.jnode.apps.jpartition.swingview;

import it.battlehorse.stamps.annotations.ModelDependent;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;
import org.jnode.apps.jpartition.model.DeviceModel;

public class DeviceView extends JPanel 
{
	private static final Logger log = Logger.getLogger(DeviceView.class);
	
	final private MainController controller;
	
	//final private Object device;
	
	public DeviceView(MainController controller)
	{
		this.controller = controller;
	}

/*	
	private static final Logger log = Logger.getLogger(DevicePartitionsUI.class);
	
	final private DevicePartitionsList devicePartitions;
	
	final private DefaultComboBoxModel cboDevicesModel = new DefaultComboBoxModel(); 
	final private JComboBox cboDevices = new JComboBox(cboDevicesModel);
	final private JPanel partitionsPanel = new JPanel(new FlowLayout());
	final private PartitionController controller;
	
	public DevicePartitionsUI(DevicePartitionsList devicePartitions, PartitionController controller)
	{
		super(new BorderLayout());
		
		setBorder(BorderFactory.createTitledBorder(""));
		
		this.devicePartitions = devicePartitions;
		this.controller = controller;
		
		JPanel devPanel = new JPanel(new BorderLayout());
		devPanel.add(new JLabel("device : "), BorderLayout.WEST);
		devPanel.add(cboDevices, BorderLayout.CENTER);
		add(devPanel, BorderLayout.NORTH);
		
		add(partitionsPanel, BorderLayout.CENTER);
		
		for(DeviceModel devParts : devicePartitions)
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
					log.debug("itemStateChanged");
					DeviceModel devParts = (DeviceModel) event.getItem();
					partitionsPanel.removeAll();
	
					if(devParts.getPartitions().isEmpty())
					{
						DiskAreaUI disk = new DiskAreaUI(DevicePartitionsUI.this.controller.getCommandProcessor(), devParts.getDevice());
						disk.setLabel("disk not partitioned");
						partitionsPanel.add(disk);
					}
					else
					{
						for(PartitionModel partition : devParts.getPartitions())
						{
							partitionsPanel.add(new PartitionUI(DevicePartitionsUI.this.controller.getCommandProcessor(), devParts.getDevice(), partition));
						}
					}
				}
			}
		});
	}
 
	public void deviceAdded(DeviceModel devParts) {
		log.debug("deviceAdded");
		cboDevicesModel.addElement(devParts);
	}

	public void deviceRemoved(DeviceModel devParts) {
		cboDevicesModel.removeElement(devParts);
	}
*/	
}
