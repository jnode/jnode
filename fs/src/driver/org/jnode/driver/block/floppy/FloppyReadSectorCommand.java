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
 
package org.jnode.driver.block.floppy;

import org.jnode.driver.block.CHS;
import org.jnode.driver.block.Geometry;
import org.jnode.system.resource.DMAResource;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class FloppyReadSectorCommand extends FloppyCommand {

    private final Geometry geometry;
    private final int sector;
    private final int sectorSize;
    private final int length;
    private final int cylinder;
    private final int head;
    private final byte[] data;
    private final int dataOffset;
    private final int gapSize;
    private final boolean multiTrack;
    private byte[] st;

    /**
     * @param drive
     * @param geometry
     * @param chs
     * @param sectorSize
     * @param multiTrack
     * @param gapSize
     * @param dest
     * @param destOffset
     */
    public FloppyReadSectorCommand(int drive,
                                   Geometry geometry,
                                   CHS chs,
                                   int sectorSize,
                                   boolean multiTrack,
                                   int gapSize,
                                   byte[] dest,
                                   int destOffset) {
        super(drive);
        this.geometry = geometry;
        this.sector = chs.getSector();
        this.sectorSize = sectorSize;
        this.multiTrack = multiTrack;
        this.gapSize = gapSize;
        if (multiTrack) {
            this.length = 2 * SECTOR_LENGTH[sectorSize];
        } else {
            this.length = SECTOR_LENGTH[sectorSize];
        }
        this.cylinder = chs.getCylinder();
        this.head = chs.getHead();
        this.data = dest;
        this.dataOffset = destOffset;
    }

    /**
     * @param fdc
     * @throws FloppyException
     * @see org.jnode.driver.block.floppy.FloppyCommand#setup(org.jnode.driver.block.floppy.FDC)
     */
    public void setup(FDC fdc)
        throws FloppyException {
        final byte[] cmd = new byte[9];
        if (multiTrack) {
            cmd[0] = (byte) 0xe6;
        } else {
            cmd[0] = (byte) 0x66;
        }
        cmd[1] = (byte) (getDrive() | ((head & 1) << 2));
        cmd[2] = (byte) cylinder;
        cmd[3] = (byte) head;
        cmd[4] = (byte) sector;
        cmd[5] = (byte) sectorSize;
        cmd[6] = (byte) geometry.getSectors();
        cmd[7] = (byte) gapSize;
        if (sectorSize == 0) {
            cmd[8] = (byte) length;
        } else {
            cmd[8] = (byte) 0xff;
        }

        fdc.setupDMA(length, DMAResource.MODE_READ);
        fdc.setDorReg(getDrive(), true, true);
        fdc.sendCommand(cmd, true);
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
                throw new FloppyException("ReadSector failed [command state=" + cmdState + "]");
            }
            fdc.copyFromDMA(data, dataOffset, length);
            notifyFinished();
        } catch (TimeoutException ex) {
            notifyError(new FloppyException(ex));
        }
    }

    /**
     * Gets the data that has been read
     *
     * @return data
     */
    public byte[] getData() {
        return data;
    }
}
