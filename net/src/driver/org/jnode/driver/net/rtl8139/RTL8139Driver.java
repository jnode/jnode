/*
 * $Id$
 */

package org.jnode.driver.net.rtl8139;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.BasicEthernetDriver;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author Martin Husted Hartvig
 */

public class RTL8139Driver extends BasicEthernetDriver {

    /**
     * Create new driver instance for this device
     */
    public RTL8139Driver(ConfigurationElement config) {
        this(new RTL8139Flags(config));
    }

    /**
     * Create new driver instance for this device
     * 
     * @param flags
     */
    public RTL8139Driver(RTL8139Flags flags) {
        this.flags = flags;
    }

    /**
     * Create a new RTL8139Core instance
     */
    protected AbstractDeviceCore newCore(Device device, Flags flags)
            throws DriverException, ResourceNotFreeException {
        return new RTL8139Core(this, device, (PCIDevice) device, flags);
    }
}
