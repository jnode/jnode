/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
    protected final int sectors;
    protected final boolean is48bit;

    public IDERWSectorsCommand(
        boolean primary,
        boolean master,
            boolean is48bit,
        long lbaStart,
        int sectors) {
        super(primary, master);
        this.is48bit = is48bit;
        this.lbaStart = lbaStart;
        this.sectors = sectors;
        if (lbaStart < 0L) {
            throw new IllegalArgumentException(String.format("LBA must be between 0 and {0}, not {1}", maxSector() - 1,
                lbaStart));
        }
        if ((sectors < 1) || (sectors > maxSectorCount())) {
            throw new IllegalArgumentException(String.format("Sectors must be between 1 and {0}, not {1}",
                maxSectorCount(), sectors));
        }
        if ((lbaStart + sectors) >= maxSector()) {
            throw new IllegalArgumentException(String.format("The maximum sector must be between 0 and {0}, not {1}",
                maxSector(), lbaStart + sectors));
        }
    }

    protected long maxSector() {
        return is48bit ? MAX_SECTOR_48 : MAX_SECTOR_28;
    }

    protected int maxSectorCount() {
        return is48bit ? MAX_SECTOR_COUNT_48 : MAX_SECTOR_COUNT_28;
    }

    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        int select = SEL_LBA | getSelect();

        final int scCurrent = sectors & 0xFF;
        final int lbaLowCurrent = (int) (lbaStart & 0xFF);
        final int lbaMidCurrent = (int) ((lbaStart >> 8) & 0xFF);
        final int lbaHighCurrent = (int) ((lbaStart >> 16) & 0xFF);

        io.waitUntilNotBusy(IDE_TIMEOUT);
        if (is48bit) {
            final int scPrevious = (sectors & 0xFF00) >> 8;
            final int lbaLowPrevious = (int) ((lbaStart >> 24) & 0xFF);
            final int lbaMidPrevious = (int) ((lbaStart >> 32) & 0xFF);
            final int lbaHighPrevious = (int) ((lbaStart >> 40) & 0xFF);

            io.setSectorCountReg(scPrevious);
            io.setLbaLowReg(lbaLowPrevious);
            io.setLbaMidReg(lbaMidPrevious);
            io.setLbaHighReg(lbaHighPrevious);
        }
        io.setSectorCountReg(scCurrent);
        io.setLbaLowReg(lbaLowCurrent);
        io.setLbaMidReg(lbaMidCurrent);
        io.setLbaHighReg(lbaHighCurrent);
        if (!is48bit) {
            final int lbaRem = (int) ((lbaStart >> 24) & 0xF);

            select |= lbaRem;
        }
        io.setSelectReg(select);
    }
}
