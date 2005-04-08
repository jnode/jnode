/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.wireless.spi.WirelessDeviceCore;
import org.jnode.driver.net.wireless.spi.WirelessEthernetDriver;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

public class Prism2Driver extends WirelessEthernetDriver {
    
    /**
     * Create new driver instance for this device
     */
    public Prism2Driver(ConfigurationElement config) {
        this(new Prism2Flags(config));
    }

    /**
     * Create new driver instance for this device
     * 
     * @param flags
     */
    public Prism2Driver(Prism2Flags flags) {
        this.flags = flags;
    }

    /**
     * Create a new Prism2Core instance
     */
    protected WirelessDeviceCore newWirelessCore(Device device, Flags flags)
            throws DriverException, ResourceNotFreeException {
        return new Prism2Core(this, device, (PCIDevice) device, flags);
    }

}
