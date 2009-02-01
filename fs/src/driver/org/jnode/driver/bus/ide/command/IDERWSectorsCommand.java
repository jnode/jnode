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

    public IDERWSectorsCommand(
        boolean primary,
        boolean master,
        long lbaStart,
        int sectors) {
        super(primary, master);
        this.lbaStart = lbaStart;
        this.sectors = sectors;
        if ((sectors < 1) || (sectors > 256)) {
            throw new IllegalArgumentException("Sectors must be between 1 and 256, not " + sectors);
        }
    }

    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        final int select;
        final int sectors;
        final int lbaLow = (int) (lbaStart & 0xFF);
        final int lbaMid = (int) ((lbaStart >> 8) & 0xFF);
        final int lbaHigh = (int) ((lbaStart >> 16) & 0xFF);
        final int lbaRem = (int) ((lbaStart >> 24) & 0x0F);
        if (master) {
            select = lbaRem | SEL_BLANK | SEL_LBA | SEL_DRIVE_MASTER;
        } else {
            select = lbaRem | SEL_BLANK | SEL_LBA | SEL_DRIVE_SLAVE;
        }
        if (this.sectors == 256) {
            sectors = 0;
        } else {
            sectors = this.sectors;
        }

        io.setSectorCountReg(sectors);
        io.setLbaLowReg(lbaLow);
        io.setLbaMidReg(lbaMid);
        io.setLbaHighReg(lbaHigh);
        io.setSelectReg(select);

    }
}
