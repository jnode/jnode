package org.jnode.apps.jpartition.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;

public class OSFacade {
	private static final OSFacade INSTANCE;
	static
	{
		INSTANCE = new OSFacade();
	}

	private OSListener osListener;

	private OSFacade()
	{
	}

	final static OSFacade getInstance()
	{
		return INSTANCE;
	}

	final void setOSListener(final OSListener listener)
	{
		if(this.osListener != null)
		{
			throw new IllegalStateException("listener already set");
		}

		try {
			DeviceUtils.getDeviceManager().addListener(new DeviceListener(){
				public void deviceStarted(org.jnode.driver.Device device) {
					if(device instanceof IDEDevice)
					{
						Device dev = createDevice(device);
						if(dev != null)
						{
							listener.deviceAdded(dev);
						}
					}
				}

				public void deviceStop(org.jnode.driver.Device device) {
					if(device instanceof IDEDevice)
					{
						Device dev = createDevice(device);
						if(dev != null)
						{
							listener.deviceRemoved(dev);
						}
					}
				}});
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final List<Device> getDevices() throws NameNotFoundException, ApiNotFoundException, IOException
	{
		List<Device> devices = new ArrayList<Device>();
		DeviceManager devMan = org.jnode.driver.DeviceUtils.getDeviceManager();
		for(org.jnode.driver.Device dev : devMan.getDevicesByAPI(IDEDeviceAPI.class))
		{
			Device device = createDevice(dev);
			if(device != null)
			{
				devices.add(device);
			}
		}
		return devices;
	}

	private Device createDevice(org.jnode.driver.Device dev)
	{
		Device device = null;
		try {
			if(dev.implementsAPI(IDEDeviceAPI.class))
			{
				Device tmpDevice = new Device(dev);

				if(dev.implementsAPI(PartitionableBlockDeviceAPI.class))
				{
					PartitionableBlockDeviceAPI<?> api = dev.getAPI(PartitionableBlockDeviceAPI.class);
					for(PartitionTableEntry e : api.getPartitionTable())
					{
						if(e instanceof IBMPartitionTableEntry)
						{
							IBMPartitionTableEntry pte = (IBMPartitionTableEntry) e;
							if(!pte.isEmpty())
							{
								long size = pte.getNrSectors() * IDEConstants.SECTOR_SIZE;
								tmpDevice.addPartition(pte.getStartLba(), size);
							}
						}
					}
				}

				device = tmpDevice;
			}
		} catch (ApiNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}
}
