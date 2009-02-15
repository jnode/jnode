/*
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

package org.jnode.driver.video.cirrus;

import org.apache.log4j.Logger;
import org.jnode.driver.video.ddc.DisplayDataChannelAPI;
import org.jnode.system.IOResource;
import org.jnode.system.MemoryResource;

/**
 * @author peda
 */
public final class CirrusMMIO implements CirrusConstants, DisplayDataChannelAPI {

    /** My logger */
    private static final Logger log = Logger.getLogger(CirrusMMIO.class);
    
    private final MemoryResource mmio;

    private final IOResource ports;
    
    /**
     * Create a new instance
     * 
     * @param mmio
     */
    public CirrusMMIO(MemoryResource mmio, IOResource ports) {
        this.mmio = mmio;
        this.ports = ports;
    }

    public void closeDDC1() {
        // nothing todo here
    }

    /**
     * Not implemented by kvm's cirrus emulation!
     */
    public boolean getDDC1Bit() {

        ports.outPortByte(SRX_SEQUENCER_INDEX, SR8_DDC2B);
        int data = ports.inPortByte(SRX_SEQUENCER_DATA) & 0xFF;

        if ((data & 0x40) == 0) {
            log.error("DDC2B/I2C configured in EEPROM mode!");
            return false;
        }

        // Anything else to check?

        return (data & (1 << 7)) != 0;
    }

    public void setupDDC1() {
        ports.outPortByte(SRX_SEQUENCER_INDEX, SR8_DDC2B);
        ports.outPortByte(SRX_SEQUENCER_DATA, 1 << 6); // activate DDC2B Configuration
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getATT(int)
     * 
     * public int getATT(int index) { getSTAT(); mmio.setByte(NV8_ATTRINDW,
     * (byte) (index | 0x20)); return mmio.getByte(NV8_ATTRDATR) & 0xff; }
     */

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getCRT(int)
     */
    public int getCRT(int index) {
        ports.outPortByte(CRX_CRTC_INDEX, index);
        return ports.inPortByte(CRX_CRTC_DATA);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getDACData()
     */
    public int getDACData() {
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        return ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getGRAF(int)
     *
    public int getGRAF(int index) {
        mmio.setByte(NV8_GRPHIND, (byte) index);
        return mmio.getByte(NV8_GRPHDAT) & 0xff;
    }*/

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getMISC()
     */
    public int getMISC() {
        return ports.inPortByte(MISC_MISCELLANEOUS_OUTPUT_READ) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSEQ(int)
     */
    public int getSEQ(int index) {
        ports.outPortByte(SRX_SEQUENCER_INDEX, (byte) index);
        return ports.inPortByte(SRX_SEQUENCER_DATA) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSTAT()
     */
    public int getSTAT() {
        return ports.inPortByte(STAT_INPUT_STATUS_REGISTER1) & 0xff;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATT(int, int)
     *
    public void setATT(int index, int val) {
        getSTAT();
        mmio.setByte(NV8_ATTRINDW, (byte) (index | 0x20));
        mmio.setByte(NV8_ATTRDATW, (byte) val);
    }*/

    /** 
     * @see org.jnode.driver.video.vgahw.VgaIO#setATTIndex(int)
     *
    public void setATTIndex(int index) {
        getSTAT();
        mmio.setByte(NV8_ATTRINDW, (byte) (index | 0x20));
    }*/

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setCRT(int, int)
     */
    public void setCRT(int index, int val) {
        ports.outPortByte(CRX_CRTC_INDEX, index);
        ports.outPortByte(CRX_CRTC_DATA, val);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACData(int)
     */
    public void setDACData(int data) {
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.inPortByte(HDR_HIDDEN_DAC_REGISTER);
        ports.outPortByte(HDR_HIDDEN_DAC_REGISTER, data);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACReadIndex(int)
     *
    public void setDACReadIndex(int index) {
        setReg8(NV8_PALINDR, index);
    }*/

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACWriteIndex(int)
     *
    public void setDACWriteIndex(int index) {
        setReg8(NV8_PALINDW, index);
    }*/

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setGRAF(int, int)
     *
    public void setGRAF(int index, int val) {
        mmio.setShort(NV16_GRPHIND, (short) ((index & 0xFF) | ((val & 0xFF) << 8)));
    }*/

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setMISC(int)
     */
    public void setMISC(int val) {
        setSEQ(SR0_SEQUENCER_RESET, 0);
        ports.outPortByte(MISC_MISCELLANEOUS_OUTPUT_WRITE, val);
        setSEQ(SR0_SEQUENCER_RESET, 3);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setSEQ(int, int)
     */
    public void setSEQ(int index, int val) {
        ports.outPortByte(SRX_SEQUENCER_INDEX, index);
        ports.outPortByte(SRX_SEQUENCER_DATA, val);
    }

    /**
     * Set a FB value at the given offset
     * 
     * @param offset
     * @param value
     *
    public void setFB(int offset, int value) {
        mmio.setInt(PFB_OFS + offset, value);
    }*/

    /**
     * Get a FB value at the given offset
     * 
     * @param offset
     *
    public int getFB(int offset) {
        return mmio.getInt(PFB_OFS + offset);
    }*/

    /**
     * Set a FIFO value at the given offset
     * 
     * @param offset
     * @param value
     *
    public void setFIFO(int offset, int value) {
        mmio.setInt(PFIFO_OFS + offset, value);
    }*/

    /**
     * Gets an 8-bit MMIO value
     * 
     * @param index
     * @return
     *
    final int getReg8(int index) {
        return mmio.getByte(index) & 0xFF;
    }*/

    /**
     * Gets an 16-bit MMIO value
     * 
     * @param index
     * @return
     *
    final int getReg16(int index) {
        return mmio.getShort(index) & 0xFFFF;
    }*/

    /**
     * Gets an 32-bit MMIO value
     * 
     * @param index
     * @return
     *
    final int getReg32(int index) {
        return mmio.getInt(index);
    }*/

    /**
     * Sets an 8-bit MMIO value
     * 
     * @param index
     *
    final void setReg8(int index, int value) {
        mmio.setByte(index, (byte) value);
    }*/

    /**
     * Gets an 16-bit MMIO value
     * 
     * @param index
     *
    final void setReg16(int index, int value) {
        mmio.setShort(index, (short) value);
    }*/

    /**
     * Gets an 32-bit MMIO value
     * 
     * @param index
     *
    final void setReg32(int index, int value) {
        mmio.setInt(index, value);
    }*/
}
