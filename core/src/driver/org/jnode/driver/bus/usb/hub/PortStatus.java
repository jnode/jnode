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

import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PortStatus extends USBPacket {

    /**
     * Initialize this instance
     */
    public PortStatus() {
        super(4);
    }

    /**
     * Gets the wPortStatus field.
     */
    public int getPortStatus() {
        return getShort(0);
    }

    /**
     * Are specific bits of the port status set.
     *
     * @param mask
     * @return True if all of the mask bits have been set, false otherwise.
     */
    public boolean getPortStatusBits(int mask) {
        return ((getShort(0) & mask) == mask);
    }

    /**
     * Gets the wPortChange field.
     */
    public int getPortChange() {
        return getShort(2);
    }

    /**
     * Are specific bits of the port change set.
     *
     * @param mask
     * @return True if all of the mask bits have been set, false otherwise.
     */
    public boolean getPortChangeBits(int mask) {
        return ((getShort(2) & mask) == mask);
    }

    public String toString() {
        return "ST:" + NumberUtils.hex(getPortStatus(), 4) + ", CH:" + NumberUtils.hex(getPortChange(), 4);
    }
}
