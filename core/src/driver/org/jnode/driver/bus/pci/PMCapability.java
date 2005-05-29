/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * Power management capability.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PMCapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    PMCapability(PCIDevice device, int offset) {
        super(device, offset);
    }
    
    /**
     * Gets the power management capabilities.
     * @return
     */
    public final int getCapabilities() {
        return readConfigWord(0x02);
    }

    /**
     * Gets the status register.
     * @return
     */
    public final int getStatus() {
        return readConfigWord(0x04);
    }

    /**
     * Gets the data register.
     * @return
     */
    public final int getData() {
        return readConfigByte(0x07);
    }

    /**
     * Gets the bridge support register.
     * @return
     */
    public final int getBridgeSupportExtensions() {
        return readConfigByte(0x06);
    }
}
