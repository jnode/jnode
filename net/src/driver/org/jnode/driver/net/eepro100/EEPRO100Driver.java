/*
 * Created on 13-Apr-2004
 *  
 */
package org.jnode.driver.net.eepro100;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.BasicEthernetDriver;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;



/**
 * @author flesire
 *  
 */
public class EEPRO100Driver extends BasicEthernetDriver {
    
    /**
     * Create a new instance
     */
    public EEPRO100Driver(ConfigurationElement config) {
        this(new EEPRO100Flags(config));
    }

    public EEPRO100Driver(EEPRO100Flags flags) {
        this.flags = flags;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.driver.net.ethernet.BasicEthernetDriver#newCore(org.jnode.driver.Device,
     *      org.jnode.driver.net.ethernet.Flags)
     */
    protected AbstractDeviceCore newCore(Device device, Flags flags) throws DriverException, ResourceNotFreeException {
        return new EEPRO100Core(this, device, (PCIDevice) device, flags);
    }

}