package org.jnode.apps.jpartition.model;

import java.util.ArrayList;
import java.util.List;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.driver.bus.ide.IDEDevice;

public class FileDeviceModel extends AbstractModel {
	private List<IDEDevice> fileDevices = new ArrayList<IDEDevice>();
	
	public void addFakeDisk() {
		addDevice(DeviceUtils.createFakeDevice());
	}

	public void addVMWareDisk() {
		addDevice(DeviceUtils.createVMWareDevice());
	}
	
	public void addDevice(IDEDevice device)
	{
		if(device != null)
		{
			fileDevices.add(device);
			propSupport.firePropertyChange("deviceAdded", null, device);
		}		
	}

	public void removeFileDevice(Object device) {
		if(device != null)
		{
			fileDevices.remove(device);
			propSupport.firePropertyChange("deviceRemoved", null, device);
		}		
	}
}
