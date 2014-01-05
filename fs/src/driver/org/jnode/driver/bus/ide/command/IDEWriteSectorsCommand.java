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
 * This is a simple write command for IDE drives.
 * It uses the old style non DMA command style for the moment.
 *
 * @author gbin
 * @author epr@jnode.org
 */
public class IDEWriteSectorsCommand extends IDERWSectorsCommand {

    private final ByteBuffer buf;
    private static final Logger log = Logger.getLogger(IDEWriteSectorsCommand.class);

    //private int sectorsWritten = 0;
    //private final Semaphore irqSem = new Semaphore(0);

    public IDEWriteSectorsCommand(boolean primary, boolean master,
                                  boolean is48bit, long lbaStart, int sectors, ByteBuffer src) {
        super(primary, master, is48bit, lbaStart, sectors);
        if (sectors * SECTOR_SIZE > src.remaining()) {
            throw new IllegalArgumentException(
                "Buffer underflow (sectors=" + sectors + ", remaining=" + src.remaining() + ")");
        }
        this.buf = src;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io)
        throws TimeoutException {
        log.debug("WRSect Setup");
        super.setup(ide, io);
        io.setCommandReg(is48bit ? CMD_WRITE_EXT : CMD_WRITE);

        // Send data
        for (int i = 0; i < sectorCount; i++) {
            log.debug("WRSect pw " + i);
            if (!pollWait(io, false))
                return;
            // Transfer sector
            log.debug("WRSect trf " + i);
            transferOneSector(ide, io);
        }

        // Flush data
        log.debug("WRSect flush");
        io.setCommandReg(is48bit ? CMD_FLUSH_CACHE_EXT : CMD_FLUSH_CACHE);
        log.debug("WRSect after flush");
        pollWait(io, false);
        log.debug("WRSect end");

        // We're done
        notifyFinished();
    }

    /**
     * Transfer exactly one sector of data towards the device.
     */
    private void transferOneSector(IDEBus ide, IDEIO io) throws TimeoutException {
        /*if (buf.remaining() < SECTOR_SIZE) {
            throw new IllegalArgumentException("Buffer underflow rem=" + buf.remaining() + " sectWritten=" +
            sectorsWritten + " sectors=" + sectorCount);
        }*/
        //sectorsWritten++; // Do this before the actual data transfer for synchronization reasons
        for (int i = 0; i < 256; i++) {
            final int v = ((buf.get() & 0xFF) + ((buf.get() & 0xFF) << 8));
            io.setDataReg(v);
        }
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) throws TimeoutException {
        // Signal an IRQ
        //irqSem.up();
    }
}
