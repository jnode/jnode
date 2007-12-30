/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.initrd;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFileSystemFormatter;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.NumberUtils;

/**
 * Dummy plugin that just mount an initial ramdisk on /Jnode
 *
 * @author gbin
 */
public class InitRamdisk extends Plugin {

    private static final Logger log = Logger.getLogger(InitRamdisk.class);

    /**
     * Create a new instance
     */
    public InitRamdisk(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            log.info("Create initrd ramdisk on /jnode");
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            int size = getPreferences().getInt("size", (int) NumberUtils.getSize("100K"));
            final RamDiskDevice dev = new RamDiskDevice(null, "dummy", size);
            dev.setDriver(new RamDiskDriver("jnode"));
            dm.register(dev);

            log.info("Format initrd ramdisk");

            final FatFileSystemFormatter formatter = new FatFileSystemFormatter(Fat.FAT16);
            final FileSystem fs = formatter.format(dev);
            try {
                fs.getRootEntry().getDirectory().addDirectory("tmp");
            } catch (IOException ex) {
                log.error("Cannot create tmp on ramdisk");
            }

            // restart the device
            log.info("Restart initrd ramdisk");
            dm.stop(dev);
            dm.start(dev);

            log.info("/jnode ready.");
        } catch (NameNotFoundException e) {
            throw new PluginException(e);
        } catch (DriverException e) {
            throw new PluginException(e);
        } catch (DeviceAlreadyRegisteredException e) {
            throw new PluginException(e);
        } catch (FileSystemException e) {
            throw new PluginException(e);
        } catch (DeviceNotFoundException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() {
        // do nothing for the moment
    }
}
