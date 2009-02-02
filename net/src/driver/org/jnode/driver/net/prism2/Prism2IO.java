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
 
package org.jnode.driver.net.prism2;

import static org.jnode.driver.net.prism2.Prism2Constants.Register.CMD;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.DATA0;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.EVACK;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.EVSTAT;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.OFFSET0;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.PARAM0;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.PARAM1;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.PARAM2;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.SELECT0;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.STATUS;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.system.MemoryResource;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;
import org.jnode.util.TimeoutException;

/**
 * Class responsible for handling the low level I/O to the prism2 device.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2IO implements Prism2Constants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(Prism2IO.class);

    /**
     * The memory mapped registers
     */
    private final MemoryResource regs;

    /**
     * Initialize this instance.
     *
     * @param regs
     */
    public Prism2IO(MemoryResource regs) {
        this.regs = regs;
    }

    /**
     * Release all resources.
     */
    final void release() {
        regs.release();
    }

    /**
     * Gets a register value.
     *
     * @param reg
     */
    final int getReg(Register reg) {
        return regs.getShort(reg.getOffset()) & 0xFFFF;
    }

    /**
     * Gets a register value.
     *
     * @param reg
     */
    final void setReg(Register reg, int value) {
        regs.setShort(reg.getOffset(), (short) value);
    }

    /**
     * Execute a command and wait until it has completed.
     *
     * @param cmd
     * @param parm0
     * @param parm1
     * @param parm2
     * @return the RESULT code.
     */
    final Result executeCommand(Command cmd, int cmdFlags, int parm0, int parm1, int parm2,
            Prism2CommandResponse response) throws TimeoutException {
        // Wait for the busy bit to clear
        waitUntilNotBusy();

        // Write command
        setReg(PARAM0, parm0);
        setReg(PARAM1, parm1);
        setReg(PARAM2, parm2);
        setReg(CMD, cmd.getCode() | cmdFlags);

        // Wait until command completion
        waitUntilCommandCompleted();

        // Read status and response
        final int status;
        if (response != null) {
            response.initialize(this);
            status = response.getStatus();
        } else {
            status = getReg(STATUS);
        }

        // Acknowledge
        setReg(EVACK, EVACK_CMD);

        // Return the result code.
        return Result.getByCode((status & STATUS_RESULT) >> 8);
    }

    /**
     * Wait until a given event mask is reached, or a timeout occurs.
     *
     * @param eventMask
     * @param eventAck
     * @param wait
     * @param timeout
     */
    final int waitForEvent(int eventMask, int eventAck, int wait, int timeout) {
        for (int counter = 0; counter < timeout; counter++) {
            final int reg = getReg(EVSTAT);
            if ((reg & eventMask) != 0) {
                // Acknowledge
                setReg(EVACK, reg & (eventMask | eventAck));
                return reg;
            }
            TimeUtils.sleep(wait);
        }
        log.debug("Timeout in waitForEvent");
        return 0;
    }

    /**
     * Wait until the device is no longer busy.
     *
     * @throws TimeoutException
     */
    private final void waitUntilNotBusy() throws TimeoutException {
        for (int counter = 0; counter < 10; counter++) {
            final int cmd = getReg(CMD);
            if ((cmd & CMD_BUSY) == 0) {
                return;
            } else {
                TimeUtils.sleep(5);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy cmd=0x" + NumberUtils.hex(getReg(CMD), 4));
    }

    /**
     * Wait until the command is completed.
     *
     * @throws TimeoutException
     */
    private final void waitUntilCommandCompleted() throws TimeoutException {
        for (int counter = 0; counter < 200; counter++) {
            final int reg = getReg(EVSTAT);
            if ((reg & EVSTAT_CMD) != 0) {
                return;
            } else {
                TimeUtils.sleep(2);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy evstat=0x" +
                NumberUtils.hex(getReg(EVSTAT), 4));
    }

    /**
     * Wait until the BAP is no longer busy.
     *
     * @throws TimeoutException
     */
    private final void waitUntilBapNotBusy() throws TimeoutException {
        for (int counter = 0; counter < 100; counter++) {
            final int cmd = getReg(OFFSET0);
            if ((cmd & OFFSET_BUSY) == 0) {
                return;
            } else {
                TimeUtils.sleep(5);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy offset=0x" +
                NumberUtils.hex(getReg(OFFSET0), 4));
    }

    /**
     * Throw an exception depending on the given result code. No exception is
     * thrown if the result is success.
     *
     * @param result
     * @throws DriverException
     */
    final void resultToException(Result result) throws DriverException {
        switch (result) {
            case SUCCESS:
                return;
            case CARD_FAIL:
                throw new DriverException("Card failure");
            case NO_BUFF:
                throw new DriverException("No buffer");
            case CMD_ERR:
                throw new DriverException("Command error");
            default:
                throw new DriverException("Unknown result code 0x" +
                        NumberUtils.hex(result.getCode(), 2));
        }
    }

    /**
     * Copy from the BAP into the given byte buffer.
     *
     * @param id        FID or RID, destined for the select register (host order)
     * @param offset    An _even_ offset into the buffer for the given FID/RID
     * @param dst       Destination buffer
     * @param dstOffset Offset in destination buffer
     * @param len       length of data to transfer in bytes
     * @throws DriverException
     */
    final void copyFromBAP(int id, int offset, byte[] dst, int dstOffset, int len)
        throws DriverException {
        // Prepare the BAP
        prepareBAP(id, offset);

        // Read even(len) buf contents from data reg
        final int maxlen = len & 0xFFFE;
        for (int i = 0; i < maxlen; i += 2) {
            final int v = getReg(DATA0);
            LittleEndian.setInt16(dst, dstOffset + i, v);
        }
        // If len odd, handle last byte
        if ((len % 2) != 0) {
            final int v = getReg(DATA0);
            dst[dstOffset + len - 1] = (byte) (v & 0xFF);
        }
    }

    /**
     * Copy to the BAP from the given byte buffer.
     *
     * @param id        FID or RID, destined for the select register (host order)
     * @param offset    An _even_ offset into the buffer for the given FID/RID
     * @param src       Source buffer
     * @param srcOffset Offset in source buffer
     * @param len       length of data to transfer in bytes
     * @throws DriverException
     */
    final void copyToBAP(int id, int offset, byte[] src, int srcOffset, int len)
        throws DriverException {
        // Prepare the BAP
        prepareBAP(id, offset);

        // Write even(len) buf contents to data reg
        final int maxlen = len & 0xFFFE;
        for (int i = 0; i < maxlen; i += 2) {
            final int v = LittleEndian.getInt16(src, srcOffset + i);
            setReg(DATA0, v);
        }
        // If len odd, handle last byte
        if ((len % 2) != 0) {
            int v = getReg(DATA0);
            prepareBAP(id, offset + maxlen);
            v = (v & 0xFF00) | (src[srcOffset + len - 1] & 0xFF);
            setReg(DATA0, v);
        }
    }

    /**
     * Prepare the BAP registers for a transfer.
     *
     * @param id     FID or RID, destined for the select register (host order)
     * @param offset An _even_ offset into the buffer for the given FID/RID
     * @throws DriverException
     * @throws IllegalArgumentException For an invalid offset.
     */
    private final void prepareBAP(int id, int offset) throws DriverException {
        if ((offset > BAP_OFFSET_MAX) || ((offset % 2) != 0)) {
            throw new IllegalArgumentException("Invalid offset " + offset);
        }
        // Write fid/rid and offset
        setReg(SELECT0, id);
        TimeUtils.sleep(1);
        setReg(OFFSET0, offset);

        // Wait for the bap to settle.
        try {
            waitUntilBapNotBusy();
        } catch (TimeoutException ex) {
            throw new DriverException("Cannot setup BAP in time", ex);
        }

        // Test for errors
        if ((getReg(OFFSET0) & OFFSET_ERR) != 0) {
            throw new DriverException("Error in offset");
        }
    }
}
