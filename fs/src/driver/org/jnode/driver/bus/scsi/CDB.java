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

package org.jnode.driver.bus.scsi;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class CDB extends SCSIBuffer {

    /**
     * Initialize this instance.
     *
     * @param cdbLength Length (in bytes) of this CDB
     * @param opcode    Opcode of this CDB
     */
    public CDB(int cdbLength, int opcode) {
        super(cdbLength);
        setInt8(0, opcode);
    }

    /**
     * Gets the opcode of this CDB.
     *
     * @return
     */
    public final int getOpcode() {
        return getUInt8(0);
    }

    public abstract int getDataTransfertCount();
}
