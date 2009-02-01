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
 
package org.jnode.driver.system.cmos.def;

import javax.naming.NameNotFoundException;
import org.jnode.driver.system.cmos.CMOSConstants;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;

/**
 * @author epr
 */
final class CMOS implements CMOSConstants {

    /**
     * CMOS I/O ports
     */
    private final IOResource cmosIO;

    /**
     * Create a new instance
     *
     * @param owner
     * @throws ResourceNotFreeException
     */
    public CMOS(ResourceOwner owner)
        throws ResourceNotFreeException {
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            this.cmosIO = rm.claimIOResource(owner, CMOS_FIRST_PORT, CMOS_LAST_PORT - CMOS_FIRST_PORT + 1);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find ResourceManager", ex);
        }
    }

    /**
     * Release all resources
     */
    public void release() {
        cmosIO.release();
    }

    /**
     * Gets a register from the CMOS data
     *
     * @param register [0..63]
     * @return
     */
    public synchronized int getRegister(int register) {
        // We must make sure that bit 7 of the address data is not
        // changed, since it controls the NMI.
        int addr = cmosIO.inPortByte(PRW8_ADDRESS);
        addr &= 0x80; /* Clear lower 7 bits */
        addr |= (register & 0x7F);
        cmosIO.outPortByte(PRW8_ADDRESS, addr);
        return cmosIO.inPortByte(PRW8_DATA);
    }

    /**
     * Sets a register from the CMOS data
     *
     * @param register [0..63]
     * @param value
     */
    public synchronized void setRegister(int register, int value) {
        // We must make sure that bit 7 of the address data is not
        // changed, since it controls the NMI.
        int addr = cmosIO.inPortByte(PRW8_ADDRESS);
        addr &= 0x80; /* Clear lower 7 bits */
        addr |= (register & 0x7F);
        cmosIO.outPortByte(PRW8_ADDRESS, addr);
        cmosIO.outPortByte(PRW8_DATA, value);
    }


}
