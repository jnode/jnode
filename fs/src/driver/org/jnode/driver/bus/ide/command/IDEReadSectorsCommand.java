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

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 *         test version
 */
public class IDEReadSectorsCommand extends IDERWSectorsCommand {
    private final ByteBuffer buf;

    private static final Logger log = Logger.getLogger(IDEReadSectorsCommand.class);

    private int readSectors = 0;

    public IDEReadSectorsCommand(
        boolean primary,
        boolean master,
        boolean is48bit,
        long lbaStart,
        int sectors,
        ByteBuffer dest) {
        super(primary, master, is48bit, lbaStart, sectors);
        buf = dest;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        super.setup(ide, io);
        io.setCommandReg(is48bit ? CMD_READ_EXT : CMD_READ);

        // Read data
        for (int i = 0; i < sectorCount; i++) {
            log.debug("RDSect pw " + i);
            if (!pollWait(io, false))
                return;
            // Read sector
            log.debug("RDSect trf " + i);
            transferOneSector(ide, io);
        }

        // We're done
        notifyFinished();
    }

    /**
     * Transfer exactly one sector of data from the device.
     */
    private void transferOneSector(IDEBus ide, IDEIO io) throws TimeoutException {
        for (int i = 0; i < 256; i++) {
            final int v = io.getDataReg();
            buf.put((byte) (v & 0xFF));
            buf.put((byte) ((v >> 8) & 0xFF));
        }
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) {
        // Do nothing
    }
}
