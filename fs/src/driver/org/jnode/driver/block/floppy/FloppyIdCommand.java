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
 
package org.jnode.driver.block.floppy;

import org.jnode.util.TimeoutException;


/**
 * @author epr
 */
public class FloppyIdCommand extends FloppyCommand {

    /**
     * Resulting state
     */
    private byte[] st;

    /**
     * Create a new instance
     *
     * @param drive
     */
    public FloppyIdCommand(int drive) {
        super(drive);
    }

    /**
     * @param fdc
     * @throws FloppyException
     * @see org.jnode.driver.block.floppy.FloppyCommand#setup(org.jnode.driver.block.floppy.FDC)
     */
    public void setup(FDC fdc)
        throws FloppyException {
        final byte[] cmd = new byte[2];
        cmd[0] = 0x4a;
        cmd[1] = (byte) getDrive();

        fdc.setDorReg(getDrive(), true, true);
        fdc.sendCommand(cmd, false);
    }

    /**
     * @param fdc
     * @throws FloppyException
     * @see org.jnode.driver.block.floppy.FloppyCommand#handleIRQ(org.jnode.driver.block.floppy.FDC)
     */
    public void handleIRQ(FDC fdc)
        throws FloppyException {
        try {
            st = fdc.getCommandState(7);
            final int st0 = st[0] & 0xFF;
            final int cmdState = (st0 & ST0_CMDST_MASK);
            if (cmdState != ST0_CMDST_NORMAL) {
                throw new FloppyException("Id failed [command state=" + cmdState + "]");
            }
            notifyFinished();
        } catch (TimeoutException ex) {
            notifyError(new FloppyException(ex));
        }
    }

    public int getST0() {
        return st[0];
    }

    public int getST1() {
        return st[1];
    }

    public int getST2() {
        return st[2];
    }

    public int getCylinder() {
        return st[3];
    }

    public int getHead() {
        return st[4];
    }

    public int getSector() {
        return st[5];
    }

    public int getSectorSize() {
        return st[6];
    }
}
