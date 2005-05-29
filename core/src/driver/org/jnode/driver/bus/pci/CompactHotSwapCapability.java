/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class CompactHotSwapCapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    CompactHotSwapCapability(PCIDevice device, int offset) {
        super(device, offset);
    }

}
