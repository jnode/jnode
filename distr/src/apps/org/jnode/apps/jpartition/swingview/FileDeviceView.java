package org.jnode.apps.jpartition.swingview;

import it.battlehorse.stamps.annotations.ModelDependent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;

public class FileDeviceView extends JFrame 
{
	private static final Logger log = Logger.getLogger(FileDeviceView.class);
	
	private JList devicesList = new JList(new DefaultListModel());
	private JPanel buttons = new JPanel();
	private JButton addVMWareDiskButton = new JButton("add VMWare disk");
	private JButton addFakeDiskButton = new JButton("add fake disk");
	private JButton removeButton = new JButton("remove device");
	final private MainController controller;
	
	public FileDeviceView(MainController controller) throws Exception
	{		
		this.controller = controller;
		
		setTitle("File devices");
		setLayout(new BorderLayout());
		add(new JScrollPane(devicesList), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
				
		buttons.add(addVMWareDiskButton);
		buttons.add(addFakeDiskButton);
		buttons.add(removeButton);
		
		addFakeDiskButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				FileDeviceView.this.controller.userAddFakeDisk();
			}
		});
		addVMWareDiskButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				FileDeviceView.this.controller.userAddVMWareDisk();
			}
		});
		removeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				if(devicesList.getSelectedIndex() >= 0)
				{
					Object value = devicesList.getSelectedValue();
					FileDeviceView.this.controller.userRemoveFileDevice(value);
				}
			}
		});
		
		setSize(600, 300);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
    @ModelDependent(modelKey ="FileDeviceModel" , propertyKey = "deviceAdded")
    public void addDevice(Object device) {
        ((DefaultListModel) devicesList.getModel()).addElement(device);
    }
    
    @ModelDependent(modelKey ="FileDeviceModel" , propertyKey = "deviceRemoved")
    public void removeDevice(Object device) {
        ((DefaultListModel) devicesList.getModel()).removeElement(device);
    }	
}
