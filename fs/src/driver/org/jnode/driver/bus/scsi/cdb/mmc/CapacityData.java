/*
 * $Id$
 *
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
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.SCSIBuffer;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CapacityData extends SCSIBuffer {

    /**
     * Default length of capacity data byte array
     */
    public static final int DEFAULT_LENGTH = 8;

    public CapacityData(byte[] buffer) {
        super(buffer);
    }

    public final int getLogicalBlockAddress() {
        return getInt32(0);
    }

    public final int getBlockLength() {
        return getInt32(4);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "LBA 0x" + hex8(getLogicalBlockAddress()) +
            ", BlockLength 0x" + hex8(getBlockLength());
    }
}
