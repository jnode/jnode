/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginException;

/**
 * A FileSystemMounter listens to the DeviceManager and once a Device
 * that implements the BlockDeviceAPI is started, it tries to mount
 * a FileSystem on that device.
 * 
 * @author epr
 */
public class FileSystemMounter implements DeviceListener {
	
	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** The DeviceManager i'm listening to */
	private DeviceManager devMan;
	/** The FileSystemService i'm using */
	private FileSystemService fss;
	/** Mapping between a device and a mounted FileSystem */
	private final HashMap devices2FS = new HashMap();
	
	/**
	 * Start the FS mounter.
	 * @throws ServiceException
	 */
	public void start()
	throws PluginException {
		try {
			devMan = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			devMan.addListener(this);
			fss = (FileSystemService)InitialNaming.lookup(FileSystemService.NAME);
		} catch (NameNotFoundException ex) {
			throw new PluginException("Cannot find DeviceManager", ex);
		}
		
	}

	/**
	 * Stop the FS mounter.
	 * @throws ServiceException
	 */	
	public void stop() 
	throws PluginException {
		devMan.removeListener(this);
	}
	
	/**
	 * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
	 */
	public void deviceStarted(Device device) {
		try {
			FSBlockDeviceAPI api = (FSBlockDeviceAPI)device.getAPI(FSBlockDeviceAPI.class);
			if (device.implementsAPI(RemovableDeviceAPI.class)) {
				tryToMount(device, api, true);
			} else {
				tryToMount(device, api, false);				
			}
		} catch (ApiNotFoundException ex) {
			// Just ignore this device.
		}
	}

	/**
	 * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
	 */
	public void deviceStop(Device device) {
		final FileSystem fs = (FileSystem)devices2FS.get(device);
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException ex) {
				log.error("Cannot close filesystem", ex);
			}
			devices2FS.remove(device);
		}
	}
	
	/**
	 * Try to mount a filesystem on the given device.
	 * @param device
	 * @param api
	 */
	protected void tryToMount(Device device, FSBlockDeviceAPI api, boolean removable) {
		log.info("Try to mount " + device.getId());

		if (removable) {
			log.error("Not mounting removable devices yet...");
			// TODO Implement mounting of removable devices
			return;
		}
		
		// Read the first sector
		try {
			final byte[] bs = new byte[api.getSectorSize()];
			api.read(0, bs, 0, bs.length);
			for (Iterator i = fss.fileSystemTypes().iterator(); i.hasNext(); ) {
				final FileSystemType fst = (FileSystemType)i.next();
				// 
				if (fst.supports(api.getPartitionTableEntry(), bs)) {
					try {
						final FileSystem fs = fst.create(device);
						fss.registerFileSystem(fs);
						log.info("Mounted " + fst.getName() + " on " + device.getId());
						return;
					} catch (FileSystemException ex) {
						log.error("Cannot mount " + fst.getName() + " filesystem on " + device.getId(), ex);
					}
				}
			}
			log.info("No filesystem found for " + device.getId());
		} catch (IOException ex) {
			log.error("Cannot read bootsector of " + device.getId());
		}
	}
}
