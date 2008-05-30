/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
     *
     * @return
     */
    public final int getCapabilities() {
        return readConfigWord(0x02);
    }

    /**
     * Gets the status register.
     *
     * @return
     */
    public final int getStatus() {
        return readConfigWord(0x04);
    }

    /**
     * Gets the data register.
     *
     * @return
     */
    public final int getData() {
        return readConfigByte(0x07);
    }

    /**
     * Gets the bridge support register.
     *
     * @return
     */
    public final int getBridgeSupportExtensions() {
        return readConfigByte(0x06);
    }
}
