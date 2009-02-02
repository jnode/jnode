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
 
package org.jnode.driver.video.nvidia;

import org.jnode.driver.video.vgahw.VgaConstants;
import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.driver.video.vgahw.VgaUtils;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class NVidiaVgaIO implements VgaIO, VgaConstants, NVidiaConstants {

    private final MemoryResource mmio;
    private final MemoryResource mem;

    private static final int PFB_OFS = 0x00100000;
    private static final int PFIFO_OFS = 0x00002000;
    private static final int PDIO_OFS = 0x00681000;

    /**
     * Create a new instance
     * 
     * @param mmio
     */
    public NVidiaVgaIO(MemoryResource mmio, MemoryResource mem) {
        this.mmio = mmio;
        this.mem = mem;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getATT(int)
     */
    public int getATT(int index) {
        getSTAT();
        mmio.setByte(NV8_ATTRINDW, (byte) (index | 0x20));
        return mmio.getByte(NV8_ATTRDATR) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getCRT(int)
     */
    public int getCRT(int index) {
        mmio.setByte(NV8_CRTCIND, (byte) index);
        return mmio.getByte(NV8_CRTCDAT) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getDACData()
     */
    public int getDACData() {
        return mmio.getByte(PDIO_OFS + DAC_D) & 0xFF;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getGRAF(int)
     */
    public int getGRAF(int index) {
        mmio.setByte(NV8_GRPHIND, (byte) index);
        return mmio.getByte(NV8_GRPHDAT) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getMISC()
     */
    public int getMISC() {
        return mmio.getByte(NV8_MISCR) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSEQ(int)
     */
    public int getSEQ(int index) {
        mmio.setByte(NV8_SEQIND, (byte) index);
        return mmio.getByte(NV8_SEQDAT) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSTAT()
     */
    public int getSTAT() {
        return mmio.getByte(NV8_INSTAT1) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATT(int, int)
     */
    public void setATT(int index, int val) {
        getSTAT();
        mmio.setByte(NV8_ATTRINDW, (byte) (index | 0x20));
        mmio.setByte(NV8_ATTRDATW, (byte) val);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATTIndex(int)
     */
    public void setATTIndex(int index) {
        getSTAT();
        mmio.setByte(NV8_ATTRINDW, (byte) (index | 0x20));
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setCRT(int, int)
     */
    public void setCRT(int index, int val) {
        mmio.setShort(NV16_CRTCIND, (short) ((index & 0xFF) | ((val & 0xFF) << 8)));
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACData(int)
     */
    public void setDACData(int data) {
        setReg8(NV8_PALDATA, data);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACReadIndex(int)
     */
    public void setDACReadIndex(int index) {
        setReg8(NV8_PALINDR, index);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACWriteIndex(int)
     */
    public void setDACWriteIndex(int index) {
        setReg8(NV8_PALINDW, index);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setGRAF(int, int)
     */
    public void setGRAF(int index, int val) {
        mmio.setShort(NV16_GRPHIND, (short) ((index & 0xFF) | ((val & 0xFF) << 8)));
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setMISC(int)
     */
    public void setMISC(int val) {
        setSEQ(0, 1);
        mmio.setByte(NV8_MISCW, (byte) val);
        setSEQ(0, 3);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setSEQ(int, int)
     */
    public void setSEQ(int index, int val) {
        mmio.setShort(NV16_SEQIND, (short) ((index & 0xFF) | ((val & 0xFF) << 8)));
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getVideoMem()
     */
    public MemoryResource getVideoMem() {
        return mem;
    }

    /**
     * Set a FB value at the given offset
     * 
     * @param offset
     * @param value
     */
    public void setFB(int offset, int value) {
        mmio.setInt(PFB_OFS + offset, value);
    }

    /**
     * Get a FB value at the given offset
     * 
     * @param offset
     */
    public int getFB(int offset) {
        return mmio.getInt(PFB_OFS + offset);
    }

    /**
     * Set a FIFO value at the given offset
     * 
     * @param offset
     * @param value
     */
    public void setFIFO(int offset, int value) {
        mmio.setInt(PFIFO_OFS + offset, value);
    }

    /**
     * Gets an 8-bit MMIO value
     * 
     * @param index
     * @return
     */
    final int getReg8(int index) {
        return mmio.getByte(index) & 0xFF;
    }

    /**
     * Gets an 16-bit MMIO value
     * 
     * @param index
     * @return
     */
    final int getReg16(int index) {
        return mmio.getShort(index) & 0xFFFF;
    }

    /**
     * Gets an 32-bit MMIO value
     * 
     * @param index
     * @return
     */
    final int getReg32(int index) {
        return mmio.getInt(index);
    }

    /**
     * Sets an 8-bit MMIO value
     * 
     * @param index
     */
    final void setReg8(int index, int value) {
        mmio.setByte(index, (byte) value);
    }

    /**
     * Gets an 16-bit MMIO value
     * 
     * @param index
     */
    final void setReg16(int index, int value) {
        mmio.setShort(index, (short) value);
    }

    /**
     * Gets an 32-bit MMIO value
     * 
     * @param index
     */
    final void setReg32(int index, int value) {
        mmio.setInt(index, value);
    }

    /**
     * Disable access to extended registers
     */
    final void lock() {
        lockUnlock(true);
    }

    /**
     * Enable access to extended registers
     */
    final void unlock() {
        lockUnlock(false);
    }

    /**
     * Enable/Disable access to extended registers
     * 
     * @param lock
     */
    private final void lockUnlock(boolean lock) {
        // final int value = (lock) ? 0x99 : 0x57;
        if (lock) {
            // VgaUtils.lockCRTC(this);
        } else {
            setCRT(NVCRTCX_LOCK, 0x57);
            VgaUtils.unlockCRTC(this);
            setCRT(NVCRTCX_LOCK, 0x57);
        }
    }

}
