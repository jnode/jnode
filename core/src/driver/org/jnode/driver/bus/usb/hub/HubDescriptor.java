/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.bus.usb.hub;

import org.jnode.driver.bus.usb.AbstractDescriptor;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HubDescriptor extends AbstractDescriptor implements USBHubConstants {

    /**
     * Initialize this instance.
     */
    public HubDescriptor() {
        super(USB_DT_HUB_NONVAR_SIZE + (256 / 8) * 2);
    }

    /**
     * Gets the number of downstream ports
     */
    public final int getNumPorts() {
        return getByte(2);
    }

    /**
     * Gets the HUB characteristics
     */
    public final int getCharacteristics() {
        return getShort(3);
    }

    /**
     * Gets the logical power switching mode.
     *
     * @return 0=all at once, 1=individual ports, 2=3=reserved
     */
    public final int getLogicalPowerSwitchingMode() {
        return getShort(3) & HUB_CHAR_LPSM;
    }

    /**
     * Gets the number of milliseconds between a power on and a power good situation on a port. The
     * HUB descriptor gives this number in 2ms intervals, the value returned is in ms, which
     * implies that the descriptor value has already been multiplied by 2.
     */
    public final int getPowerOn2PowerGood() {
        return getByte(5) << 1;
    }

    /**
     * Gets the maximum current requirements of the HUB controller in mA.
     */
    public final int getHubControllerCurrent() {
        return getByte(6);
    }

    /**
     * Is the device connected to the given port removable.
     *
     * @param port
     */
    public final boolean isRemovableDevice(int port) {
        return ((getByte(7 + (port + 1) / 8) & (1 << ((port + 1) % 8))) == 0);
    }

    /**
     * Convert to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        final int ports = getNumPorts();
        final int chars = getCharacteristics();
        b.append("HUB[");
        b.append("length:");
        b.append(getLength());
        b.append(", #ports:");
        b.append(ports);
        b.append(", char:0x");
        b.append(NumberUtils.hex(chars, 4));
        b.append(", pon2pg:");
        b.append(getPowerOn2PowerGood());
        b.append("maxCtrlCur:");
        b.append(getHubControllerCurrent());
        if ((chars & HUB_CHAR_COMPOUND) != 0) {
            b.append(", devs:");
            for (int i = 0; i < ports; i++) {
                b.append(isRemovableDevice(i) ? 'R' : 'F');
            }
        }
        return b.toString();
    }
}
