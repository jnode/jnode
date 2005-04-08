/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

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

    /** The memory mapped registers */
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
    final int getReg(int reg) {
        return regs.getShort(reg) & 0xFFFF;
    }

    /**
     * Gets a register value.
     * 
     * @param reg
     */
    final void setReg(int reg, int value) {
        regs.setShort(reg, (short) value);
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
    final int executeCommand(int cmd, int parm0, int parm1, int parm2,
            Prism2CommandResponse response) throws TimeoutException {
        // Wait for the busy bit to clear
        waitUntilNotBusy();

        // Write command
        setReg(REG_PARAM0, parm0);
        setReg(REG_PARAM1, parm1);
        setReg(REG_PARAM2, parm2);
        setReg(REG_CMD, cmd);

        // Wait until command completion
        waitUntilCommandCompleted();

        // Read status and response
        final int status;
        if (response != null) {
            response.initialize(this);
            status = response.getStatus();
        } else {
            status = getReg(REG_STATUS);
        }

        // Acknowledge
        setReg(REG_EVACK, EVACK_CMD);

        // Return the result code.
        return (status & STATUS_RESULT) >> 8;
    }

    /**
     * Wait until the device is no longer busy.
     * 
     * @throws TimeoutException
     */
    private final void waitUntilNotBusy() throws TimeoutException {
        for (int counter = 0; counter < 10; counter++) {
            final int cmd = getReg(REG_CMD);
            if ((cmd & CMD_BUSY) == 0) {
                return;
            } else {
                TimeUtils.sleep(5);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy cmd=0x"
                + NumberUtils.hex(getReg(REG_CMD), 4));
    }

    /**
     * Wait until the command is completed.
     * 
     * @throws TimeoutException
     */
    private final void waitUntilCommandCompleted() throws TimeoutException {
        for (int counter = 0; counter < 200; counter++) {
            final int reg = getReg(REG_EVSTAT);
            if ((reg & EVSTAT_CMD) != 0) {
                return;
            } else {
                TimeUtils.sleep(2);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy evstat=0x"
                + NumberUtils.hex(getReg(REG_EVSTAT), 4));
    }

    /**
     * Wait until the BAP is no longer busy.
     * 
     * @throws TimeoutException
     */
    private final void waitUntilBapNotBusy() throws TimeoutException {
        for (int counter = 0; counter < 100; counter++) {
            final int cmd = getReg(REG_OFFSET0);
            if ((cmd & OFFSET_BUSY) == 0) {
                return;
            } else {
                TimeUtils.sleep(5);
            }
        }
        ;
        throw new TimeoutException("Prism2 still busy offset=0x"
                + NumberUtils.hex(getReg(REG_OFFSET0), 4));
    }

    /**
     * Throw an exception depending on the given result code. No exception is
     * thrown if the result is success.
     * 
     * @param result
     * @throws DriverException
     */
    final void resultToException(int result) throws DriverException {
        switch (result) {
        case RESULT_SUCCESS:
            return;
        case RESULT_CARD_FAIL:
            throw new DriverException("Card failure");
        case RESULT_NO_BUFF:
            throw new DriverException("No buffer");
        case RESULT_CMD_ERR:
            throw new DriverException("Command error");
        default:
            throw new DriverException("Unknown result code 0x"
                    + NumberUtils.hex(result, 2));
        }
    }

    /**
     * Copy from the BAP into the given byte buffer.
     * 
     * @param id
     *            FID or RID, destined for the select register (host order)
     * @param offset
     *            An _even_ offset into the buffer for the given FID/RID
     * @param dst
     *            Destination buffer
     * @param dstOffset
     *            Offset in destination buffer
     * @param len
     *            length of data to transfer in bytes
     * @throws DriverException
     */
    final void copyFromBAP(int id, int offset, byte[] dst, int dstOffset,
            int len) throws DriverException {
        // Prepare the BAP
        prepareBAP(id, offset);

        // Read even(len) buf contents from data reg
        final int maxlen = len & 0xFFFE;
        for (int i = 0; i < maxlen; i += 2) {
            final int v = getReg(REG_DATA0);
            LittleEndian.setInt16(dst, dstOffset + i, v);
        }
        // If len odd, handle last byte
        if ((len % 2) != 0) {
            final int v = getReg(REG_DATA0);
            dst[dstOffset + len - 1] = (byte) (v & 0xFF);
        }
    }

    /**
     * Copy to the BAP from the given byte buffer.
     * 
     * @param id
     *            FID or RID, destined for the select register (host order)
     * @param offset
     *            An _even_ offset into the buffer for the given FID/RID
     * @param src
     *            Source buffer
     * @param srcOffset
     *            Offset in source buffer
     * @param len
     *            length of data to transfer in bytes
     * @throws DriverException
     */
    final void copyToBAP(int id, int offset, byte[] src, int srcOffset,
            int len) throws DriverException {
        // Prepare the BAP
        prepareBAP(id, offset);

        // Write even(len) buf contents to data reg
        final int maxlen = len & 0xFFFE;
        for (int i = 0; i < maxlen; i += 2) {
            final int v = LittleEndian.getInt16(src, srcOffset + i);
            setReg(REG_DATA0, v);
        }
        // If len odd, handle last byte
        if ((len % 2) != 0) {
            int v = getReg(REG_DATA0);
            prepareBAP(id, offset + maxlen);
            v = (v & 0xFF00) | (src[srcOffset + len - 1] & 0xFF);
            setReg(REG_DATA0, v);
        }
    }

    /**
     * Prepare the BAP registers for a transfer.
     * 
     * @param id
     *            FID or RID, destined for the select register (host order)
     * @param offset
     *            An _even_ offset into the buffer for the given FID/RID
     * @throws DriverException
     * @throws IllegalArgumentException
     *             For an invalid offset.
     */
    private final void prepareBAP(int id, int offset) throws DriverException {
        if ((offset > BAP_OFFSET_MAX) || ((offset % 2) != 0)) {
            throw new IllegalArgumentException("Invalid offset " + offset);
        }
        // Write fid/rid and offset
        setReg(REG_SELECT0, id);
        TimeUtils.sleep(1);
        setReg(REG_OFFSET0, offset);

        // Wait for the bap to settle.
        try {
            waitUntilBapNotBusy();
        } catch (TimeoutException ex) {
            throw new DriverException("Cannot setup BAP in time", ex);
        }

        // Test for errors
        if ((getReg(REG_OFFSET0) & OFFSET_ERR) != 0) {
            throw new DriverException("Error in offset");
        }
    }
}
