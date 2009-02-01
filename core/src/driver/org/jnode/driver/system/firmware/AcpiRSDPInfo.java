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
 
package org.jnode.driver.system.firmware;

import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AcpiRSDPInfo {

    private final Address rsdpStart;
    private final int version;

    /**
     * @param rsdpStart
     * @param version
     */
    public AcpiRSDPInfo(final Address rsdpStart, final int version) {
        super();
        this.rsdpStart = rsdpStart;
        this.version = version;
    }

    /**
     * @return Returns the rsdpStart.
     */
    public final Address getRsdpStart() {
        return this.rsdpStart;
    }

    /**
     * @return Returns the version.
     */
    public final int getVersion() {
        return this.version;
    }

    public final int getLength() {
        if (version == 2) {
            return 36;
        } else {
            return 20;
        }
    }
}
