/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.net.ipv4.config.impl;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;

/**
 * Monitor the startup/shutdown of network devices.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class NetDeviceMonitor implements DeviceListener {
    
    /** My logger */
    private static final Logger log = Logger.getLogger(NetDeviceMonitor.class);
    private final ConfigurationProcessor processor;
    private final NetConfigurationData config;
    
    /**
     * @param config
     */
    public NetDeviceMonitor(ConfigurationProcessor processor, NetConfigurationData config) {
        this.processor = processor;
        this.config = config;
    }

    /**
     * Configure all netdevices already started by the given devman.
     * @param devMan
     */
    public void configureDevices(DeviceManager devMan) {
        final Collection<Device> devs = devMan.getDevicesByAPI(NetDeviceAPI.class);
        for (Device dev : devs) {
            if (dev.implementsAPI(NetDeviceAPI.class)) {
                configureDevice(dev);
            }
        }
    }
    
    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void deviceStarted(Device device) {
        if (device.implementsAPI(NetDeviceAPI.class)) {
            configureDevice(device);
        }
    }
    
    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public void deviceStop(Device device) {
        // TODO Auto-generated method stub

    }
    
    private void configureDevice(Device dev) {
        log.info("Configuring " + dev.getId());
        final NetDeviceConfig cfg = config.getConfiguration(dev);
        if (cfg != null) {
            processor.apply(dev, cfg, false);
        }
    }
}
