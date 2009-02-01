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
 
package org.jnode.driver.block.floppy;

import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class FloppySeekCommand extends FloppyCommand {

    /**
     * The cylinder to seek to
     */
    private final int cylinder;

    /**
     * Create a new instance
     *
     * @param drive
     * @param cylinder
     */
    public FloppySeekCommand(int drive, int cylinder) {
        super(drive);
        this.cylinder = cylinder;
    }

    /**
     * @param fdc
     * @throws FloppyException
     * @see org.jnode.driver.block.floppy.FloppyCommand#setup(org.jnode.driver.block.floppy.FDC)
     */
    public void setup(FDC fdc) throws FloppyException {
        final byte[] cmd = new byte[3];
        cmd[0] = 0x0f;
        cmd[1] = (byte) getDrive();
        cmd[2] = (byte) cylinder;

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
            final int st0 = fdc.getST0();
            final int cmdState = (st0 & ST0_CMDST_MASK);
            if (cmdState != ST0_CMDST_NORMAL) {
                throw new FloppyException("Seek failed [command state=" + cmdState + "]");
            }
            notifyFinished();
        } catch (TimeoutException ex) {
            notifyError(new FloppyException(ex));
        }
    }

}
