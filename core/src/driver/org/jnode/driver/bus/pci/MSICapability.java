/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MSICapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    MSICapability(PCIDevice device, int offset) {
        super(device, offset);
    }

}
