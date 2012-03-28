/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.driver.net.eepro100;

import org.apache.log4j.Logger;
import org.jnode.system.resource.IOResource;
import org.jnode.util.NumberUtils;

/**
 * @author flesire
 */
public class EEPRO100Registers implements EEPRO100Constants {

    protected static final Logger log = Logger.getLogger(EEPRO100Registers.class);

    /**
     * Start of IO address space
     */
    private final int iobase;
    /**
     * IO address space resource
     */
    private final IOResource io;

    /**
     *
     */
    public EEPRO100Registers(int iobase, IOResource io) {
        this.iobase = iobase;
        this.io = io;
    }

    //  --- REGISTER METHODS

    /**
     * Writes a 8-bit NIC register
     *
     * @param reg
     * @param value
     */

    public void setReg8(int reg, int value) {
        io.outPortByte(iobase + reg, value);
    }

    /**
     * Writes a 16-bit NIC register
     *
     * @param reg
     * @param value
     */

    public void setReg16(int reg, int value) {
        io.outPortWord(iobase + reg, value);
    }

    /**
     * Writes a 32-bit NIC register
     *
     * @param reg
     * @param value
     */

    public void setReg32(int reg, int value) {
        io.outPortDword(iobase + reg, value);
    }

    /**
     * Reads a 16-bit NIC register
     *
     * @param reg
     */
    public int getReg16(int reg) {
        return io.inPortWord(iobase + reg);
    }

    /**
     * Reads a 32-bit NIC register
     *
     * @param reg
     */

    public final int getReg32(int reg) {
        return io.inPortDword(iobase + reg);
    }

    /**
     * Reads a 8-bit NIC register
     *
     * @param reg
     */
    public final int getReg8(int reg) {
        return io.inPortByte(iobase + reg);
    }

    /**
     * Wait for the command unit to accept a command.
     */
    public void waitForCmdDone() {
        int wait = 0;
        int delayed_cmd;
        do {
            if (getReg8(SCBCmd) == 0)
                return;
        } while (++wait <= 100);
        delayed_cmd = getReg8(SCBCmd);
        do {
            if (getReg8(SCBCmd) == 0)
                break;
        } while (++wait <= 10000);
        log.debug("Command " + NumberUtils.hex(delayed_cmd) + " was not immediately accepted, " +
            wait + " ticks!");
    }

}
