/*
 * $Id$
 */
package org.jnode.net.service;

import java.net.VMNetDevice;

import org.jnode.driver.Device;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class NetDeviceImpl extends VMNetDevice {

    private final Device device;
    
    public NetDeviceImpl(Device device) {
        this.device = device;
    }
    
    /**
     * @see java.net.VMNetDevice#getId()
     */
    public String getId() {
        return device.getId();
    }
    
    /**
     * @return Returns the device.
     */
    public final Device getDevice() {
        return this.device;
    }
}
