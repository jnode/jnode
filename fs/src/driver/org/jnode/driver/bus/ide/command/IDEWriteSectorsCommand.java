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

import java.nio.ByteBuffer;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.TimeoutException;

/**
 * This is a simple write command for IDE drives.
 * It uses the old style non DMA command style for the moment.
 *
 * @author gbin
 */
public class IDEWriteSectorsCommand extends IDERWSectorsCommand {

    private final ByteBuffer buf;

    private int readSectors = 0;

    public IDEWriteSectorsCommand(
        boolean primary,
        boolean master,
            boolean is48bit,
        long lbaStart,
        int sectors,
            ByteBuffer src) {
        super(primary, master, is48bit, lbaStart, sectors);
        this.buf = src;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io)
        throws TimeoutException {
        super.setup(ide, io);
        io.setCommandReg(is48bit ? CMD_WRITE_EXT : CMD_WRITE);
        io.waitUntilNotBusy(IDE_TIMEOUT);
        transferASector(ide, io);
    }

    private void transferASector(IDEBus ide, IDEIO io) throws TimeoutException {
//        io.waitUntilNotBusy(IDE_TIMEOUT);
        for (int i = 0; i < 256; i++) {
            int v = ((buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8));
            io.setDataReg(v);
        }
        readSectors++;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) throws TimeoutException {
        final int state = io.getStatusReg();
        if ((state & ST_ERROR) != 0) {
            setError(io.getErrorReg());
        } else {
            if ((state & (ST_BUSY | ST_DEVICE_READY)) == ST_DEVICE_READY) {
                if (readSectors < sectors) {
                    transferASector(ide, io);
                } else {
                    notifyFinished();
                }
            }
        }
    }

}
