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

package org.jnode.driver.block.floppy;

/**
 * @author epr
 */
public class FloppyDriveParametersCommand extends FloppyCommand {

    private final FloppyDriveParameters dp;
    private final FloppyParameters fp;

    /**
     * @param drive
     * @param dp
     * @param fp
     */
    public FloppyDriveParametersCommand(int drive, FloppyDriveParameters dp, FloppyParameters fp) {
        super(drive);
        this.dp = dp;
        this.fp = fp;
    }

    /**
     * @param fdc
     * @throws FloppyException
     * @see org.jnode.driver.block.floppy.FloppyCommand#setup(org.jnode.driver.block.floppy.FDC)
     */
    public void setup(FDC fdc) throws FloppyException {
        final int drive = getDrive();

        //final int dtr = fdc.getDTR(drive);
        final int hlt = dp.getHeadLoadTime();

        byte[] cmd = new byte[3];
        cmd[0] = 0x03;
        cmd[1] = (byte) (fp.getSpec1());
        cmd[2] = (byte) (hlt << 1);
        fdc.setDorReg(drive, false, true);
        fdc.sendCommand(cmd, false);
        notifyFinished();
    }

    /**
     * @param fdc
     * @see org.jnode.driver.block.floppy.FloppyCommand#handleIRQ(org.jnode.driver.block.floppy.FDC)
     */
    public void handleIRQ(FDC fdc) {
        // do nothing
    }

}
