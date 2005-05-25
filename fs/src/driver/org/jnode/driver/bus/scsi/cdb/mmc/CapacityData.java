/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.SCSIBuffer;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CapacityData extends SCSIBuffer {

    /** Default length of capacity data byte array */
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
