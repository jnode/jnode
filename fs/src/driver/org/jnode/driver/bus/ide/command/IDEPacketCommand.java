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
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IDEPacketCommand extends IDECommand {

    private final boolean overlay = false;

    private final boolean dma = false;

    private final byte[] commandPacket;

    private final byte[] dataPacket;

    private int dataOffset;

    private int dataTransfered;

    private static final int IR_CD = 0x01; // C/D mask (command/data transfer)

    private static final int IR_IO = 0x02; // I/O mask (input/output direction)

    /**
     * @param primary
     * @param master
     * @throws IllegalArgumentException
     */
    public IDEPacketCommand(boolean primary, boolean master, byte[] commandPacket,
                            byte[] dataPacket, int dataOffset) throws IllegalArgumentException {
        super(primary, master);
        this.commandPacket = commandPacket;
        this.dataPacket = dataPacket;
        this.dataOffset = dataOffset;
    }

    /**
     * @return Returns the dataTransfered.
     */
    public final int getDataTransfered() {
        return this.dataTransfered;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        // Features
        int fReg = 0;
        fReg |= (overlay ? 0x02 : 0);
        fReg |= (dma ? 0x01 : 0);
        io.setFeatureReg(fReg);

        // Sector count
        io.setSectorCountReg(0);

        io.setLbaLowReg(0);

        int cmdLength = commandPacket.length;
        // Make sure length is 12 or 16
        if (cmdLength < 12) {
            cmdLength = 12;
        }
        if (cmdLength > 12) {
            cmdLength = 16;
        }
        io.setLbaMidReg(cmdLength & 0xFF);
        io.setLbaHighReg((cmdLength >> 8) & 0xFF);

        io.setSelectReg(getSelect());
        io.setCommandReg(CMD_PACKETCMD);

        TimeUtils.sleep(1); // Delay 400ns

        io.waitUntilNotBusy(IDE_TIMEOUT);

        // Transfer command packet to device
        ide.writeData(commandPacket, 0, cmdLength);
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) {
        log.debug("IRQ");

        final int st = io.getStatusReg();
        if ((st & ST_ERROR) != 0) {
            final int error = io.getErrorReg();
            if ((error & ERR_ABORT) != 0) {
                // Command aborted
                log.debug("Packet command aborted, error 0x"
                    + NumberUtils.hex(error, 2));
            } else {
                log.debug("Unknown error 0x" + NumberUtils.hex(error, 2));
            }
            setError(error);
            return;
        }

        while (true) {
            final int status = io.getStatusReg();
            if ((status & (ST_BUSY | ST_DATA_REQUEST)) == 0) {
                log.debug("Packet command ready");
                // Command ready
                notifyFinished();
                break;
            }

            final int intReason = io.getSectorCountReg();
            final boolean io2dev = ((intReason & IR_IO) == 0);
            final boolean cmdXfer = ((intReason & IR_CD) != 0);

            if (!cmdXfer) {
                // Transfer of data to or from device (depending on io2dev)

                final int cntLow = io.getLbaMidReg() & 0xFF;
                final int cntHigh = io.getLbaHighReg() & 0xFF;
                final int cnt = cntLow | (cntHigh << 8);

                if (io2dev) {
                    log.debug("Write data " + cnt);
                    ide.writeData(dataPacket, dataOffset, cnt);
                } else {
                    log.debug("Read data " + cnt);
                    ide.readData(dataPacket, dataOffset, cnt);
                }

                dataOffset += cnt;
                dataTransfered += cnt;

                TimeUtils.sleep(1); // Delay 400ns (a bit more)
            } else {
                // Unknown state
                log
                    .error("Unknown state IR=0x"
                        + NumberUtils.hex(intReason, 2));
                break;
            }
        }
    }
}
