/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.driver.bus.ide.command;

import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDECommand;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.TimeoutException;

/**
 * IDE Read and Write common initialization code
 *
 * @author gbin
 */
public abstract class IDERWSectorsCommand extends IDECommand {
    protected final long lbaStart;
    protected final int sectorCount;
    protected final boolean is48bit;

    public IDERWSectorsCommand(boolean primary, boolean master,
                               boolean is48bit, long lbaStart, int sectorCount) {
        super(primary, master);
        this.is48bit = is48bit;
        this.lbaStart = lbaStart;
        this.sectorCount = sectorCount;
        if (lbaStart < 0L) {
            throw new IllegalArgumentException(String.format("LBA must be between 0 and {0}, not {1}", maxSector() - 1,
                lbaStart));
        }
        if ((sectorCount < 1) || (sectorCount > maxSectorCount())) {
            throw new IllegalArgumentException(String.format("Sectors must be between 1 and {0}, not {1}",
                maxSectorCount(), sectorCount));
        }
        if ((lbaStart + sectorCount) >= maxSector()) {
            throw new IllegalArgumentException(String.format("The maximum sector must be between 0 and {0}, not {1}",
                maxSector(), lbaStart + sectorCount));
        }
    }

    protected long maxSector() {
        return is48bit ? MAX_SECTOR_48 : MAX_SECTOR_28;
    }

    protected int maxSectorCount() {
        return is48bit ? MAX_SECTOR_COUNT_48 : MAX_SECTOR_COUNT_28;
    }

    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        final int select = SEL_LBA | getSelect();
        io.waitUntilStatus(ST_BUSY, 0, IDE_TIMEOUT, "before selectDevice");
        selectDevice(io);

        if (is48bit) {
            io.setSelectReg(select);
            io.setFeatureReg(0);
            io.setFeatureReg(0); // indeed, twice
            io.setSectorCountReg((sectorCount >> 8) & 0xFF);
            io.setLbaLowReg((int) ((lbaStart >> 24) & 0xFF));
            io.setLbaMidReg((int) ((lbaStart >> 32) & 0xFF));
            io.setLbaHighReg((int) ((lbaStart >> 40) & 0xFF));
            io.setSectorCountReg((sectorCount >> 0) & 0xFF);
            io.setLbaLowReg((int) ((lbaStart >> 0) & 0xFF));
            io.setLbaMidReg((int) ((lbaStart >> 8) & 0xFF));
            io.setLbaHighReg((int) ((lbaStart >> 16) & 0xFF));
        } else {
            // 28-bit addressing
            io.setSelectReg(select | ((int) (lbaStart >> 24) & 0xF));
            io.setFeatureReg(0);
            io.setSectorCountReg((sectorCount >> 0) & 0xFF);
            io.setLbaLowReg((int) ((lbaStart >> 0) & 0xFF));
            io.setLbaMidReg((int) ((lbaStart >> 8) & 0xFF));
            io.setLbaHighReg((int) ((lbaStart >> 16) & 0xFF));
        }
    }

    /**
     * Poll waiting.
     *
     * @return true if waiting succeeded, false in case of an error.
     */
    protected final boolean pollWait(IDEIO io, boolean checkState) throws TimeoutException {
        // Force a 400ns wait
        for (int i = 0; i < 4; i++) {
            io.getAltStatusReg(); // This wastes 100ns
        }
        // Wait for BUSY to be cleared
        io.waitUntilStatus(ST_BUSY, 0, IDE_DATA_XFER_TIMEOUT, "pollWait");
        // Check state
        if (checkState) {
            final int state = io.getStatusReg();
            if ((state & ST_ERROR) != 0) {
                setError(io.getErrorReg());
                return false;
            }
            if ((state & ST_DEVICE_FAULT) != 0) {
                setError(ERR_ABORT);
                return false;
            }
            if ((state & ST_DEVICE_READY) == 0) {
                setError(ERR_ABORT);
                return false;
            }
        }
        return true;
    }
}
