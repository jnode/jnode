package org.jnode.apps.jpartition.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.naming.NameNotFoundException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.FileIDEDevice;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.partitions.command.PartitionHelper;
import org.jnode.partitions.ibm.IBMPartitionTypes;

public class FileDeviceFrame extends JFrame 
{
	private static final Logger log = Logger.getLogger(FileDeviceFrame.class);
	
	private static final long DEFAULT_FILE_SIZE = 1000;
	
	private DefaultListModel fileDevices = new DefaultListModel(); 
	
	private JList devicesList = new JList(fileDevices);
	private JPanel buttons = new JPanel();
	private JButton addButton = new JButton("add device");
	private JButton removeButton = new JButton("remove device");
	
	public FileDeviceFrame() throws Exception
	{
		setTitle("File devices");
		setLayout(new BorderLayout());
		add(new JScrollPane(devicesList), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
				
		buttons.add(addButton);
		buttons.add(removeButton);
		
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				JFileChooser jfc = new JFileChooser();
				jfc.setDialogType(JFileChooser.SAVE_DIALOG);
				int result = jfc.showSaveDialog(FileDeviceFrame.this);
				if(result == JFileChooser.APPROVE_OPTION)					
				{
					try 
					{
						FileIDEDevice fd = createDevice(jfc.getSelectedFile());
						if(addDevice(fd))
						{
							fileDevices.addElement(fd);
						}
						else
						{
							JOptionPane.showMessageDialog(FileDeviceFrame.this, 
									"failed to add device", "add device", 
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e) {
						log.error(e);
					}
				}
			}
		});
		removeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				if(devicesList.getSelectedIndex() >= 0)
				{
					//TODO
				}
			}
		});
	}
	
	protected FileIDEDevice createDevice(File file) throws Exception 
	{
		//return new FileDevice(file, "rw");
		FileIDEDevice dev = new FileIDEDevice(file, DEFAULT_FILE_SIZE, 
				true, true);
		return dev;
	}

	private boolean addDevice(FileIDEDevice device)
	{
		boolean success = false;
		try 
		{			
			DeviceManager devMan = DeviceUtils.getDeviceManager();			
			devMan.register(device);
			success = true;
			
			PartitionHelper helper = new PartitionHelper(device.getId(), devMan);
			helper.initMbr();
			helper.write();
			helper.modifyPartition(0, true, 0, DEFAULT_FILE_SIZE, 
					false, IBMPartitionTypes.PARTTYPE_WIN95_FAT32);		
		} catch (NameNotFoundException e) {
			log.error(e);
		} catch (DeviceAlreadyRegisteredException e) {
			log.error(e);
		} catch (DriverException e) {
			log.error(e);
		} catch (DeviceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ApiNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return success;
	}
}
