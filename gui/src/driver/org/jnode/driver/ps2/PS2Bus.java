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
 
package org.jnode.driver.ps2;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;

/**
 * Provides common functionality shared by the drivers (read/write data/status)
 * 
 * @author qades
 */
public class PS2Bus extends Bus implements IRQHandler, PS2Constants {

    /** My logger */
    private static final Logger log = Logger.getLogger(PS2Bus.class);
    private IOResource ioResData;
    private IOResource ioResCtrl;
    private int activeCount = 0;
    private final PS2ByteChannel kbChannel = new PS2ByteChannel();
    private final PS2ByteChannel mouseChannel = new PS2ByteChannel();

    /**
     * Create a PS2 object.
     */
    PS2Bus(Bus parent) {
        super(parent);
    }

    /**
     * All necessary resources are claimed in this method.
     * 
     * @param owner the driver for which the resources are to be claimed
     * @return the IRQResource for the driver in question
     */
    final synchronized IRQResource claimResources(ResourceOwner owner, int irq)
        throws DriverException {
        try {
            final ResourceManager rm;
            try {
                rm = InitialNaming.lookup(ResourceManager.NAME);
            } catch (NameNotFoundException ex) {
                throw new DriverException("Cannot find ResourceManager: ", ex);
            }
            if (ioResData == null) {
                ioResData = claimPorts(rm, owner, PS2_DATA_PORT, 1);
                ioResCtrl = claimPorts(rm, owner, PS2_CTRL_PORT, 1);
            }
            final IRQResource irqRes = rm.claimIRQ(owner, irq, this, true);
            if (activeCount == 0) {
                flush();
            }
            activeCount++;
            return irqRes;
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim necessairy resources: ", ex);
        }
    }

    /**
     * Release all resource held by this bus, only if all devices that depend on
     * this bus have been stopped.
     */
    final synchronized void releaseResources() {
        activeCount--;
        if (activeCount == 0) {
            ioResData.release();
            ioResCtrl.release();
            ioResData = null;
            ioResCtrl = null;
        }
    }

    /**
     * Handles a PS/2 interrupt
     * 
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public final synchronized void handleInterrupt(int irq) {
        processQueues();
    }

    /**
     * Read the queue until it is empty and process the read data.
     */
    private final void processQueues() {
        int status;
        int loops = 0;
        while (((status = readStatus()) & STAT_OBF) != 0) {
            final int data = readData();
            if (++loops > 1000) {
                log.error("A lot of PS2 data, probably wrong, dropping it");
                continue;
            }

            // determine which driver shall handle the scancode
            final PS2ByteChannel channel;
            if ((status & STAT_MOUSE_OBF) != 0) {
                channel = mouseChannel;
            } else {
                channel = kbChannel;
            }
            // if this driver is not registered, merely exit
            if (channel == null) {
                log.debug("Unhandled scancode 0x" + Integer.toHexString(data) + " status=0x" +
                        Integer.toHexString(status));
            } else {
                // let the driver handle the scancode
                channel.handleScancode(data);
            }
        }
    }

    /**
     * Flush the PS2 output buffer.
     */
    final synchronized void flush() {
        while ((readStatus() & STAT_OBF) != 0) {
            readData();
        }
    }

    /**
     * Get the status of the PS/2 port.
     * 
     * @return the status byte
     */
    final int readStatus() {
        return ioResCtrl.inPortByte(PS2_STAT_PORT) & 0xff;
    }

    /**
     * Write a byte to the control port
     * 
     * @param b
     */
    private void writeController(int b) throws DeviceException {
        waitWrite();
        ioResCtrl.outPortByte(PS2_CTRL_PORT, b);
    }

    /**
     * Write a byte to the data port
     * 
     * @param b
     */
    private final void writeData(int b) throws DeviceException {
        waitWrite();
        ioResData.outPortByte(PS2_DATA_PORT, b);
    }

    /**
     * Wait for a data available in outputbuffer.
     */
    private final boolean waitRead() {
        int count = 0;
        while (count < 1000) {
            if ((readStatus() & STAT_OBF) != 0) {
                return true;
            } else {
                count++;
                Thread.yield();
            }
        }
        return false;
    }

    /**
     * Wait for a non-ready inputbuffer.
     */
    private final void waitWrite() throws DeviceException {
        int count = 0;
        while (count < 1000) {
            if ((readStatus() & STAT_IBF) == 0) {
                return;
            } else {
                count++;
                Thread.yield();
            }
        }
        throw new DeviceException("InputBuffer full");
    }

    /**
     * Read a byte from the data port
     * 
     * @return
     */
    private final int readData() {
        return ioResData.inPortByte(PS2_DATA_PORT) & 0xff;
    }

    /**
     * Gets the mode register
     * 
     * @return int
     */
    final synchronized int getMode() throws DeviceException {
        try {
            writeController(CCMD_READ_MODE);
            if (waitRead()) {
                return readData();
            } else {
                throw new DeviceException("Not return data from READ_MODE");
            }
        } finally {
            processQueues();
        }
    }

    /**
     * Sets the mode register
     */
    final void setMode(int mode) throws DeviceException {
        writeController(CCMD_WRITE_MODE);
        writeData(mode);
    }

    /**
     * Test for the presence of a connected mouse device
     * 
     * @return true if a mouse is present, false if not
     */
    final synchronized boolean testMouse() throws DeviceException {
        try {
            writeController(CCMD_TEST_MOUSE);
            if (waitRead()) {
                final int status = readStatus();
                final int rc = readData();
                log.debug("testMouse rc=0x" + NumberUtils.hex(rc, 2) + ", status 0x" +
                        NumberUtils.hex(status, 2));
                return (rc != 0xFF);
            } else {
                log.debug("No return from TEST_MOUSE");
                return false;
            }
        } finally {
            processQueues();
        }
    }

    /**
     * Activate/Deactivate the mouse
     */
    final void setMouseEnabled(boolean enable) throws DeviceException {
        writeController(enable ? CCMD_MOUSE_ENABLE : CCMD_MOUSE_DISABLE);
        int mode = getMode();
        if (enable) {
            mode |= MODE_MOUSE_INT;
        } else {
            mode &= ~MODE_MOUSE_INT;
        }
        setMode(mode);
    }

    /**
     * Activate/Deactivate the keyboard
     */
    final void setKeyboardEnabled(boolean enable) throws DeviceException {
        writeController(enable ? CCMD_KB_ENABLE : CCMD_KB_DISABLE);
        int mode = getMode();
        if (enable) {
            mode |= MODE_INT;
        } else {
            mode &= ~MODE_INT;
        }
        setMode(mode);
    }

    /**
     * Read an ACK and cnt bytes. The bytes (except the ACK) are added to the
     * given channel.
     * 
     * @param channel
     * @param cnt
     * @return
     */
    private final boolean readAckAndData(PS2ByteChannel channel, int cnt) {
        if (!waitRead()) {
            return false;
        }
        int data = readData();
        switch (data) {
            case REPLY_ACK:
                break;
            case REPLY_RESEND:
                log.error("Mouse replied with RESEND");
                return false;
            default:
                // Not an ACK, consider it data
                channel.handleScancode(data);
                cnt--;
                break;
        }
        while (cnt > 0) {
            if (!waitRead()) {
                return false;
            }
            data = readData();
            channel.handleScancode(data);
            cnt--;
        }
        return true;
    }

    /**
     * Send a single byte command to the mouse
     * 
     * @param cmd
     * @return Success (true / false)
     */
    private final boolean sendMouseCommand(int cmd, int returnCnt) throws DeviceException {
        // Transmit the command
        writeController(CCMD_WRITE_MOUSE);
        writeData(cmd);
        return readAckAndData(mouseChannel, returnCnt);
    }

    /**
     * Write a series of commands to the mouse
     * 
     * @param cmd The command
     * @param params The command parameters
     * @return
     */
    final synchronized boolean writeMouseCommands(int cmd, int[] params, int returnCnt)
        throws DeviceException {
        // First clear the mouse channel, otherwise we might read
        // old data back
        mouseChannel.clear();

        if (params == null) {
            return sendMouseCommand(cmd, returnCnt);
        } else {
            if (!sendMouseCommand(cmd, 0)) {
                return false;
            }
            final int cnt = params.length;
            for (int i = 0; i < cnt - 1; i++) {
                if (!sendMouseCommand(params[i], 0)) {
                    return false;
                }
            }
            return sendMouseCommand(params[cnt - 1], returnCnt);
        }
    }

    /**
     * @return
     */
    final PS2ByteChannel getKbChannel() {
        return this.kbChannel;
    }

    /**
     * @return
     */
    final PS2ByteChannel getMouseChannel() {
        return this.mouseChannel;
    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner,
            final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }

    }
}
