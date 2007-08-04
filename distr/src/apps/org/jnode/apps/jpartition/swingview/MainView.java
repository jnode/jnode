package org.jnode.apps.jpartition.swingview;

import it.battlehorse.stamps.annotations.ModelDependent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;

public class MainView extends JFrame {
	private static final Logger log = Logger.getLogger(DeviceView.class);
		
	final private MainController controller;
	final private DefaultComboBoxModel devices;
	final private DeviceView deviceView;
	
	public MainView(MainController controller, 
					JFrame fileDeviceView,
					JComponent cmdProcessorView) throws Exception
	{
		this.controller = controller;
		
		setTitle("JPartition");		
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
		
		add(cmdProcessorView, BorderLayout.SOUTH);
		
		deviceView = new DeviceView(controller);
		add(deviceView, BorderLayout.CENTER);
		
		devices = new DefaultComboBoxModel();
		JComboBox cboDevices = new JComboBox(devices);
		cboDevices.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent event) {
				boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
				if(selected)
				{
					MainView.this.controller.userSelectDevice(event.getItem());
				}
			}
		});
		add(cboDevices, BorderLayout.NORTH);

		setSize(600, 300);
		setVisible(true);
		setLocation(fileDeviceView.getX(), fileDeviceView.getY() + fileDeviceView.getHeight());
	}

    @ModelDependent(modelKey ="DeviceModel" , propertyKey = "deviceStarted")
	public void deviceStarted(Object device) {
    	log.debug("deviceStarted");		
		devices.addElement(device);
	}

    @ModelDependent(modelKey ="DeviceModel" , propertyKey = "deviceStop")
	public void deviceStop(Object device) {
		log.debug("deviceStop");
		devices.removeElement(device);		
	}
    
    @ModelDependent(modelKey ="DeviceModel" , propertyKey = "deviceSelected")
	public void deviceSelected(Object device) {
		log.debug("deviceSelected");
		//TODO
		//deviceView.init(device);
	}
}
