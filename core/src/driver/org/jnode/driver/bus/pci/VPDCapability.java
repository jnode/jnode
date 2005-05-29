/*
 * $Id$
 */
package org.jnode.driver.bus.pci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VPDCapability extends Capability {

    /**
     * @param device
     * @param offset
     */
    VPDCapability(PCIDevice device, int offset) {
        super(device, offset);
    }

}
