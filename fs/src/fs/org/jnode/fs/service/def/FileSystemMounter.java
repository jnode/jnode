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

package org.jnode.fs.service.def;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

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
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.plugin.PluginException;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;

/**
 * A FileSystemMounter listens to the DeviceManager and once a Device that
 * implements the BlockDeviceAPI is started, it tries to mount a FileSystem on
 * that device.
 *
 * @author epr
 */
final class FileSystemMounter implements DeviceListener {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileSystemMounter.class);

    private static final String MOUNT_ROOT = "devices";

    /** The DeviceManager i'm listening to */
    private DeviceManager devMan;

    /** The FileSystemService i'm using */
    private final FileSystemService fileSystemService;

    public FileSystemMounter(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    /**
     * Start the FS mounter.
     *
     * @throws PluginException
     */
    public void start() throws PluginException {
        try {
            // Create the /devices
            fileSystemService.getApi().mkDir(MOUNT_ROOT);

            devMan = InitialNaming.lookup(DeviceManager.NAME);
            devMan.addListener(this);
        } catch (NameNotFoundException ex) {
            throw new PluginException("Cannot find DeviceManager", ex);
        } catch (IOException ex) {
            throw new PluginException("Cannot create devices directory");
        }

    }

    /**
     * Stop the FS mounter.
     */
    public void stop() {
        devMan.removeListener(this);
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public final void deviceStarted(final Device device) {
        if (device.implementsAPI(FSBlockDeviceAPI.class)) {
            // add it to the queue of devices to be mounted only if the action
            // is not already pending
            WorkUtils.add(new Work("Mounting " + device.getId()) {

                public void execute() {
                    asyncDeviceStarted(device);
                }
            });
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public final void deviceStop(Device device) {
        if (device.implementsAPI(FSBlockDeviceAPI.class)) {
            final FileSystem fs = fileSystemService
                    .unregisterFileSystem(device);
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException ex) {
                    log.error("Cannot close filesystem", ex);
                }
            }
        }
    }

    /**
     * Try to mount a filesystem on the given device.
     *
     * @param device
     * @param api
     */
    protected void tryToMount(Device device, FSBlockDeviceAPI api,
            boolean removable, boolean readOnly) {

        if (fileSystemService.getFileSystem(device) != null) {
            log.info("device already mounted...");
            return;
        }

        // if (removable) {
        // log.error("Not mounting removable devices yet...");
        // TODO Implement mounting of removable devices
        // return;
        // }

        log.info("Try to mount " + device.getId());
        // Read the first sector
        try {
            final PartitionTableEntry ptEntry = api.getPartitionTableEntry();
            final ByteBuffer bs = ByteBuffer.allocate(api.getSectorSize());
            final String mountPath = File.separatorChar + MOUNT_ROOT + File.separatorChar;

            api.read(0, bs);
            for (FileSystemType fst : fileSystemService.fileSystemTypes()) {
            	if(fst instanceof BlockDeviceFileSystemType)
            	{
	                if (((BlockDeviceFileSystemType)fst).supports(ptEntry, bs.array(), api)) {
	                    try {
	                        final FileSystem fs = fst.create(device, readOnly);
	                        fileSystemService.registerFileSystem(fs);

	                        final String fullPath = mountPath + device.getId();
	                        log.debug("Mounting " + device.getId() + " on " + fullPath);
	                        fileSystemService.mount(fullPath, fs, null);

	                        log.info("Mounted " + fst.getName() + " on "
	                                + fullPath);
	                        return;
	                    } catch (FileSystemException ex) {
	                        log.error("Cannot mount " + fst.getName()
	                                + " filesystem on " + device.getId(), ex);
	                    } catch (IOException ex) {
	                        log.error("Cannot mount " + fst.getName()
	                                + " filesystem on " + device.getId(), ex);
	                    }
	                }
            	}
            }
            log.info("No filesystem found for " + device.getId());
        } catch (IOException ex) {
            log.error("Cannot read bootsector of " + device.getId());
        }
    }

    /**
     * Mount the filesystem on the given device.
     */
    final void asyncDeviceStarted(Device device) {
        try {
            if (device.isStarted()) {
                final FSBlockDeviceAPI api = device
                        .getAPI(FSBlockDeviceAPI.class);
                final boolean readOnly = false; // TODO: read from config
                if (device.implementsAPI(RemovableDeviceAPI.class)) {
                    tryToMount(device, api, true, readOnly);
                } else {
                    tryToMount(device, api, false, readOnly);
                }
            }
        } catch (ApiNotFoundException ex) {
            // Just ignore this device.
        }
    }
}
