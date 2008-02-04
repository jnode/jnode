package org.jnode.apps.jpartition.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;

public class OSFacade {
	private static final OSFacade INSTANCE;
	static
	{
		INSTANCE = new OSFacade();
	}

	private OSFacade()
	{
	}

	final static OSFacade getInstance()
	{
		return INSTANCE;
	}

	final void setOSListener(final OSListener listener) throws OSFacadeException
	{
		if(listener == null)
		{
			throw new NullPointerException("listener is null");
		}

		try {
			DeviceUtils.getDeviceManager().addListener(new DeviceListener(){
				public void deviceStarted(org.jnode.driver.Device device) {
					if(device instanceof IDEDevice)
					{
						Device dev = null;
						try {
							dev = createDevice(device);
							if(dev != null)
							{
								listener.deviceAdded(dev);
							}
						} catch (OSFacadeException e) {
							listener.errorHappened(e);
						}
					}
				}

				public void deviceStop(org.jnode.driver.Device device) {
					if(device instanceof IDEDevice)
					{
						Device dev = null;
						try {
							dev = createDevice(device);
							if(dev != null)
							{
								listener.deviceRemoved(dev);
							}
						} catch (OSFacadeException e) {
							listener.errorHappened(e);
						}
					}
				}});
		} catch (NameNotFoundException e) {
			throw new OSFacadeException("error in setOSListener", e);
		}
	}

	final List<Device> getDevices() throws OSFacadeException
	{
		List<Device> devices = new ArrayList<Device>();
		try {
			DeviceManager devMan = org.jnode.driver.DeviceUtils
					.getDeviceManager();
			for (org.jnode.driver.Device dev : devMan
					.getDevicesByAPI(IDEDeviceAPI.class)) {
				Device device = createDevice(dev);
				if (device != null) {
					devices.add(device);
				}
			}
		} catch (NameNotFoundException e) {
			throw new OSFacadeException("error in getDevices", e);
		}
		return devices;
	}

	private Device createDevice(org.jnode.driver.Device dev) throws OSFacadeException
	{
		Device device = null;
		List<IBMPartitionTableEntry> partitions = getPartitions(dev);
		if(partitions != null) // null if not supported
		{
			List<Partition> devPartitions = new ArrayList<Partition>(partitions.size());
			Partition prevPartition = null;

			for(IBMPartitionTableEntry e : partitions)
			{
				IBMPartitionTableEntry pte = (IBMPartitionTableEntry) e;
				long start = pte.getStartLba();
				long size = pte.getNrSectors() * IDEConstants.SECTOR_SIZE;

				if(pte.isEmpty())
				{
					if((prevPartition != null) && !prevPartition.isUsed())
					{
						// current and previous partitions are empty
						prevPartition.mergeWithNextPartition(size);
					}
					else
					{
						// current partition is empty but not the previous one
						devPartitions.add(new Partition(start, size, false));
					}
				}
				else
				{
					// current partition is not empty
					devPartitions.add(new Partition(start, size, true));
				}
			}

			try {
				long devSize = dev.getAPI(IDEDeviceAPI.class).getLength();
				device = new Device(dev.getId(), devSize, dev, devPartitions);
			} catch (ApiNotFoundException e) {
				throw new OSFacadeException("error in createDevice", e);
			} catch (IOException e) {
				throw new OSFacadeException("error in createDevice", e);
			}
		}

		return device;
	}

	private List<IBMPartitionTableEntry> getPartitions(org.jnode.driver.Device dev) throws OSFacadeException
	{
		boolean supported = false;
		List<IBMPartitionTableEntry> partitions = new ArrayList<IBMPartitionTableEntry>();

		try {
			if (dev.implementsAPI(IDEDeviceAPI.class)) {
				if (dev.implementsAPI(PartitionableBlockDeviceAPI.class)) {
					PartitionableBlockDeviceAPI<?> api = dev
							.getAPI(PartitionableBlockDeviceAPI.class);
					boolean supportedPartitions = true;

					for (PartitionTableEntry e : api.getPartitionTable()) {
						if (!(e instanceof IBMPartitionTableEntry)) {
							// non IBM partition tables are not handled for now
							supportedPartitions = false;
							break;
						}

						partitions.add((IBMPartitionTableEntry) e);
					}

					supported = supportedPartitions;
				}
			}
		} catch (ApiNotFoundException e) {
			throw new OSFacadeException("error in getPartitions", e);
		} catch (IOException e) {
			throw new OSFacadeException("error in getPartitions", e);
		}

		return supported ? partitions : null;
	}


	private FileSystem<?> getFileSystem(org.jnode.driver.Device dev, PartitionTableEntry pte)
	{
/*
		DeviceManager devMan = InitialNaming.lookup(DeviceManager.NAME);
		FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
		Collection<Device> devices = devMan.getDevices();
		FileSystem fs = null;

		for (Device device : devices) {
			if (device instanceof IDEDiskPartitionDevice) {
				IDEDiskPartitionDevice partition = (IDEDiskPartitionDevice)device;
				if ((partition.getParent() == dev) && (partition.getPartitionTableEntry() == pte)) {
					//fs = fss.getFileSystem(device).get;
					break;
				}
			}
		}

		return fs;
*/
		return null;//TODO
	}
}
