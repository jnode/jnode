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
 
package org.jnode.driver.bus.usb.uhci;

import org.jnode.system.IOResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIIO implements UHCIConstants {

    private final IOResource io;
    private final int base;

    /**
     * Create a new instance
     *
     * @param io
     */
    public UHCIIO(IOResource io) {
        this.io = io;
        this.base = io.getStartPort();
    }

    /**
     * Release all resources
     */
    public void release() {
        io.release();
    }

    /**
     * Gets the command register
     */
    protected final int getCommand() {
        return io.inPortWord(base + USBCMD);
    }

    /**
     * Are the given bits in the command register set?
     */
    protected final boolean getCommandBits(int mask) {
        return ((io.inPortWord(base + USBCMD) & mask) == mask);
    }

    /**
     * Sets the command register
     */
    protected final void setCommand(int command) {
        io.outPortWord(base + USBCMD, command);
    }

    /**
     * Sets certain bits of the command register on/off
     */
    protected final void setCommandBits(int mask, boolean on) {
        int cmd = io.inPortWord(base + USBCMD);
        if (on) {
            cmd |= mask;
        } else {
            cmd &= ~mask;
        }
        io.outPortWord(base + USBCMD, cmd);
    }

    /**
     * Gets the status register
     */
    protected final int getStatus() {
        return io.inPortWord(base + USBSTS);
    }

    /**
     * Are the given bits in the status register set?
     */
    protected final boolean getStatusBits(int mask) {
        return ((io.inPortWord(base + USBSTS) & mask) == mask);
    }

    /**
     * Clear the status register
     */
    protected final void clearStatus(int clearMask) {
        io.outPortWord(base + USBSTS, clearMask);
    }

    /**
     * Gets the interrupt enable register
     */
    protected final int getInterruptEnable() {
        return io.inPortWord(base + USBINTR);
    }

    /**
     * Sets the interrupt enable register
     */
    protected final void setInterruptEnable(int mask) {
        io.outPortWord(base + USBINTR, mask);
    }

    /**
     * Gets the frame number register
     */
    protected final int getFrameNumber() {
        return io.inPortWord(base + USBFRNUM);
    }

    /**
     * Sets the frame number register
     */
    protected final void setFrameNumber(int frnum) {
        io.outPortWord(base + USBFRNUM, frnum);
    }

    /**
     * Gets the frame list base address register
     */
    protected final int getFrameListBaseAddress() {
        return io.inPortDword(base + USBFLBASEADD);
    }

    /**
     * Sets the frame list base address register
     */
    protected final void setFrameListBaseAddress(int baseAddress) {
        io.outPortDword(base + USBFLBASEADD, baseAddress);
    }

    /**
     * Gets the start of frame (SOF) modify register
     */
    protected final int getStartOfFrame() {
        return io.inPortByte(base + USBSOF);
    }

    /**
     * Sets the start of frame (SOF) modify register
     */
    protected final void setStartOfFrame(int sof) {
        io.outPortByte(base + USBSOF, sof);
    }

    /**
     * Gets the port status and control register
     */
    protected final int getPortSC(int port) {
        return io.inPortWord(base + USBPORTSC1 + (port << 1));
    }

    /**
     * Are the given bits in the port status and control register set?
     */
    protected final boolean getPortSCBits(int port, int mask) {
        return ((io.inPortWord(base + USBPORTSC1 + (port << 1)) & mask) == mask);
    }

    /**
     * Sets certain bits of the port1 status and control register on/off
     */
    protected final void setPortSCBits(int port, int mask, boolean on) {
        int sc = io.inPortWord(base + USBPORTSC1 + (port << 1));
        sc &= 0xFFF5;
        if (on) {
            sc |= mask;
        } else {
            sc &= ~mask;
        }
        io.outPortWord(base + USBPORTSC1 + (port << 1), sc);
    }

    /**
     * Sets all bits of the port1 status and control register
     */
    protected final void setPortSC(int port, int value) {
        io.outPortWord(base + USBPORTSC1 + (port << 1), value);
    }
}
