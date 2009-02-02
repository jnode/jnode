/*
 * $Id$
 *
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
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.NumberUtils;


/**
 * @author epr
 */
public class IDEIdCommand extends IDECommand {

    /**
     * The result of this command
     */
    private IDEDriveDescriptor result;
    /**
     * Should an ATAP ID command be issued(true) or a normal ID command (false)
     */
    private final boolean atapiID;
    /**
     * Read data
     */
    private final int[] data = new int[256];
    /**
     * Did IDENTIFY DEVICE return a packet response signature?
     */
    private boolean packetResponse;

    /**
     * Create a new instance
     *
     * @param master  Command intended for master (true) or slave (false)
     * @param atapiID Should an ATAP ID command be issued(true) or a normal ID command (false)
     * @throws IllegalArgumentException Invalid argument
     */
    public IDEIdCommand(boolean primary, boolean master, boolean atapiID)
        throws IllegalArgumentException {
        super(primary, master);
        this.atapiID = atapiID;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io) {
        final int command = (atapiID ? CMD_PIDENTIFY : CMD_IDENTIFY);

        io.setSelectReg(getSelect());
        io.setCommandReg(command);
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) {
        final int[] data = this.data;
        final int state = io.getStatusReg();
        if ((state & ST_ERROR) != 0) {
            // Error in ID command.
            final int error = io.getErrorReg();
            if ((error & ERR_ABORT) != 0) {
                final int sectCount = io.getSectorCountReg();
                final int lbaLow = io.getLbaLowReg();
                final int lbaMid = io.getLbaMidReg();
                final int lbaHigh = io.getLbaHighReg();
                io.getSelectReg();
                if ((sectCount == 0x01) && (lbaLow == 0x01) &&
                    (lbaMid == 0x14) && (lbaHigh == 0xEB)) {
                    packetResponse = true;
                } else {
                    log.debug("Reponse st=" + NumberUtils.hex(state, 2) +
                        " lbal=" + NumberUtils.hex(lbaLow, 2) +
                        " lbam=" + NumberUtils.hex(lbaMid, 2) +
                        " lbah=" + NumberUtils.hex(lbaHigh, 2) +
                        " sectc=" + NumberUtils.hex(sectCount, 2));
                    packetResponse = false;
                }
                log.debug("Abort " + (packetResponse ? "packet" : "error"));
            } else {
                log.debug("Error " + NumberUtils.hex(error));
            }

            result = null;
            notifyFinished();
        } else if ((state & ST_BUSY) == 0) {
            if ((state & ST_DATA_REQUEST) != 0) {
                // Data is ready to read
                for (int i = 0; i < 256; i++) {
                    data[i] = io.getDataReg();
                }
                result = new IDEDriveDescriptor(data, atapiID);
            } else {
                // Not busy, but still no data ready??? strange
                //log.debug("irq:!drq");
                result = null;
            }
            notifyFinished();
        } else {
            // Controller is still busy, wait for the following IRQ.
            log.debug("irq:busy st=" + NumberUtils.hex(state) +
                "lbal=" + NumberUtils.hex(io.getLbaLowReg()) +
                "lbam=" + NumberUtils.hex(io.getLbaMidReg()) +
                "lbah=" + NumberUtils.hex(io.getLbaHighReg()) +
                "select=" + NumberUtils.hex(io.getSelectReg()));
        }
    }

    /**
     * The result of this command. Only valid when <code>isFinished</code>
     * returns <code>true</code>.
     */
    public IDEDriveDescriptor getResult() {
        return result;
    }

    /**
     * Did IDENTIFY DEVICE return a packet response signature?
     */
    public boolean isPacketResponse() {
        return packetResponse;
    }
}
