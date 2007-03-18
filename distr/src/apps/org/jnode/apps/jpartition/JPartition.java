package org.jnode.apps.jpartition;

import java.util.Collection;

import javax.naming.NameNotFoundException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.model.DevicePartitions;
import org.jnode.apps.jpartition.model.DevicePartitionsList;
import org.jnode.apps.jpartition.view.DevicePartitionsFrame;
import org.jnode.apps.jpartition.view.FileDeviceFrame;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;

public class JPartition {
	private static final Logger log = Logger.getLogger(JPartition.class);
	
	public static void main(String[] args) throws Exception {
		initJNodeCore();
		
		FileDeviceFrame frm = new FileDeviceFrame();
		frm.setSize(300, 300);
		frm.setVisible(true);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		DevicePartitionsFrame mainUI = new DevicePartitionsFrame(getDevicePartitions()); 
		mainUI.setSize(300, 300);
		mainUI.setVisible(true);
		mainUI.setLocation(frm.getX(), frm.getY() + frm.getHeight());
		mainUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static DevicePartitionsList getDevicePartitions()
	{
		DevicePartitionsList devParts = new DevicePartitionsList();
		try {
			DeviceManager devMan = InitialNaming.lookup(DeviceManager.NAME);
			Collection<Device> devices = devMan.getDevicesByAPI(PartitionableBlockDeviceAPI.class);
			for(Device device : devices)
			{
				devParts.add(new DevicePartitions(device));
			}
		} catch (NameNotFoundException e) {
			log.error(e);
		}
		return devParts;
	}
	
	private static void initJNodeCore() throws Exception {
        NameSpace namespace = new BasicNameSpace();
        InitialNaming.setNameSpace(namespace);
        namespace.bind(DeviceManager.NAME, StubDeviceManager.INSTANCE);
	}	
}
