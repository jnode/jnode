/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class AGPCapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    AGPCapability(PCIDevice device, int offset) {
        super(device, offset);
    }

}
