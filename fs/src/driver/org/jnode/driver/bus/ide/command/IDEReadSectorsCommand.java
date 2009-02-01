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

import java.nio.ByteBuffer;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 *         test version
 */
public class IDEReadSectorsCommand extends IDERWSectorsCommand {
    private final ByteBuffer buf;

    private int readSectors;
    private static long total = 0;

    public IDEReadSectorsCommand(boolean primary, boolean master, long lbaStart, int sectors, ByteBuffer dest) {
        super(primary, master, lbaStart, sectors);
        buf = dest;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        super.setup(ide, io);
        io.setCommandReg(CMD_READ);
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) {
        final int state = io.getStatusReg();
        if ((state & ST_ERROR) != 0) {
            setError(io.getErrorReg());
        } else {
            if ((state & (ST_BUSY | ST_DEVICE_READY)) == ST_DEVICE_READY) {
                final int offset = readSectors * SECTOR_SIZE;
                for (int i = 0; i < 256; i++) {
                    final int v = io.getDataReg();
                    buf.put((byte) (v & 0xFF));
                    buf.put((byte) ((v >> 8) & 0xFF));
                }
                readSectors++;
                if (readSectors == sectors) {
                    notifyFinished();
                }
            }
        }
    }
}
