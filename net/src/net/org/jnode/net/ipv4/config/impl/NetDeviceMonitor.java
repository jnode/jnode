/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import java.util.Collection;
import java.util.Iterator;

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
    private final Logger log = Logger.getLogger(getClass());
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
        final Collection devs = devMan.getDevicesByAPI(NetDeviceAPI.class);
        for (Iterator i = devs.iterator(); i.hasNext(); ) {
            final Device dev = (Device)i.next();
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
