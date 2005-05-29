/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class SlotIDCapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    SlotIDCapability(PCIDevice device, int offset) {
        super(device, offset);
    }

}
