/*
 * $Id$
 */
package org.jnode.fs.initrd;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * Dummy plugin that just mount an initial ramdisk on /Jnode
 * 
 * @author gbin
 */
public class InitRamdisk extends Plugin {
	private static final Logger log = Logger.getLogger(InitRamdisk.class);
	/**
	 * Create a new instance
	 *  
	 */
	public InitRamdisk(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @see org.jnode.plugin.Plugin#startPlugin()
	 */
	protected void startPlugin() throws PluginException {
		final DeviceManager dm;
		try {
			log.info("Create initrd ramdisk on /jnode");
			dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			RamDiskDevice dev = new RamDiskDevice(null, "dummy", 100000);
			dev.setDriver(new RamDiskDriver("jnode"));
			dm.register(dev);

			log.info("Format initrd ramdisk");

			FileSystemService fileSystemService = (FileSystemService)InitialNaming.lookup(FileSystemService.NAME);
			FileSystemType type = fileSystemService.getFileSystemTypeForNameSystemTypes(FatFileSystemType.NAME);
			type.format(dev, new Integer(Fat.FAT16));

			// restart the device
			dev.stop();
			dev.start();

			log.info("/jnode ready.");

			// restart the device
			dev.stop();
			dev.start();
		} catch (NameNotFoundException e) {
			throw new PluginException(e);
		} catch (DriverException e) {
			throw new PluginException(e);
		} catch (DeviceAlreadyRegisteredException e) {
			throw new PluginException(e);
		} catch (FileSystemException e) {
			throw new PluginException(e);
		}

	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() {
		// do nothing for the moment
	}
}
