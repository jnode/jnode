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

package org.jnode.fs.jifs.def;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceException;
import org.jnode.driver.virtual.VirtualDevice;
import org.jnode.driver.virtual.VirtualDeviceFactory;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.jifs.JIFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

import javax.naming.NameNotFoundException;
import java.io.IOException;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFSPlugin extends Plugin {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(JIFSPlugin.class);
    /**
     * Manager of Extensions
     */
    private JIFSExtension jifsExtension;

    public JIFSPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        log.debug("JIFSPlugin created.");
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        log.info("start jifs");
        try {
            FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
            FileSystemType type = fSS.getFileSystemTypeForNameSystemTypes(JIFileSystemType.NAME);
            try {
                VirtualDevice dev = VirtualDeviceFactory.createDevice(JIFileSystemType.VIRTUAL_DEVICE_NAME);
                log.info(dev.getId() + " registered");
                final FileSystem fs = type.create(dev, true);
                fSS.registerFileSystem(fs);

                final String mountPath = "jifs";
                fSS.mount(mountPath, fs, null);
                log.info("Mounted " + type.getName() + " on "
                        + mountPath);
                final ExtensionPoint infoEP = getDescriptor().getExtensionPoint("info");
                jifsExtension = new JIFSExtension(infoEP);
            } catch (DeviceAlreadyRegisteredException ex) {
                log.error("jifs is currently running.");
                return;
            } catch (FileSystemException ex) {
                log.error("Cannot mount " + type.getName()
                        + " filesystem ", ex);
                return;
            } catch (DeviceException e) {
                log.debug("Got DriverException, maybe jifs is running.");
                return;
            } catch (IOException ex) {
                log.error("Cannot mount jifs", ex);
            }

        } catch (NameNotFoundException e) {
            log.error("filsystemservice / filesystemtype not found");
        } catch (FileSystemException e) {
            log.error(e);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() {
        log.info("stop jifs");
        try {
            FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            VirtualDevice dev = (VirtualDevice) dm.getDevice(JIFileSystemType.VIRTUAL_DEVICE_NAME);
            fSS.unregisterFileSystem(dev);
            log.info("FIFS unmounted");
            dm.unregister(dev);
            log.info("jifs unregistered");
        } catch (NameNotFoundException e) {
            log.error("filsystemservice / filesystemtype not found");
        } catch (DeviceNotFoundException ex) {
            log.info("no jifs present");
        } catch (DriverException ex) {
            log.error(ex);
        }
    }
	
}
