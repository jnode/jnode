/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.video.vgahw.VgaConstants;
import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.system.MemoryResource;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonVgaIO implements VgaIO, VgaConstants, RadeonConstants {

    private final MemoryResource mmio;
    private MemoryResource videoRam;
    
    public RadeonVgaIO(MemoryResource mmio) {
        this.mmio = mmio;
    }
        
    /**
     * Gets a 32-bit register from a given (byte) offset
     * @param addr
     * @return
     */
    final int getReg8(int addr) {
        return mmio.getByte(addr) & 0xFF;
    }

    /**
     * Sets a 8-bit register at a given (byte) offset
     * @param addr
     */
    final void setReg8(int addr, int value) {
        mmio.setByte(addr, (byte)value);
    }
    
    /**
     * Gets a 32-bit register from a given (byte) offset
     * @param addr
     * @return
     */
    final int getReg32(int addr) {
        return mmio.getInt(addr);
    }

    /**
     * Sets a 32-bit register at a given (byte) offset
     * @param addr
     */
    final void setReg32(int addr, int value) {
        mmio.setInt(addr, value);
    }
    
    /**
     * Sets a 32-bit register at a given (byte) offset
     * @param addr
     */
    final void setRegP32(int addr, int value, int mask) {
        mmio.setInt(addr, (mmio.getInt(addr) & mask) | value);
    }
    
    /**
     * Gets a PLL value from a given address.
     * @param addr
     * @return
     */
    final int getPLL(int addr) {
    	setReg8(CLOCK_CNTL_INDEX, addr & 0x0000003f);
    	return getReg32(CLOCK_CNTL_DATA);
    }
    
    /**
     * Sets a PLL value at a given address.
     * @param addr
     */
    final void setPLL(int addr, int value) {
		setReg8(CLOCK_CNTL_INDEX, (addr & 0x0000003f) | 0x00000080); 
		setReg32(CLOCK_CNTL_DATA, value); 
    }
    
    /**
     * Write "val" to PLL-register "addr" keeping bits "mask"
     * @param addr
     * @param value
     * @param mask
     */
    final void setPLLP(int addr, int value, int mask) {
    	final int tmp = (getPLL(addr) & mask) | value;
    	setPLL(addr, tmp);
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getATT(int)
     */
    public int getATT(int index) {
        setReg8(ATT_I, index);
        return getReg8(ATT_DR);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getCRT(int)
     */
    public int getCRT(int index) {
		setReg8(CRTC_I, (byte) index);
		return getReg8(CRTC_D);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getDACData()
     */
    public int getDACData() {
        return getReg8(DAC_D);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getGRAF(int)
     */
    public int getGRAF(int index) {
        setReg8(GRAF_I, index);
        return getReg8(GRAF_D);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getMISC()
     */
    public int getMISC() {
        return getReg8(MISC_R);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSEQ(int)
     */
    public int getSEQ(int index) {
        setReg8(SEQ_I, index);
        return getReg8(SEQ_D);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSTAT()
     */
    public int getSTAT() {
        return getReg8(STATC);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getVideoMem()
     */
    public MemoryResource getVideoMem() {
        return videoRam;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATT(int, int)
     */
    public void setATT(int index, int val) {
		getSTAT();
		setReg8(ATT_I, (byte) index);
		setReg8(ATT_DW, (byte) val);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATTIndex(int)
     */
    public void setATTIndex(int index) {
		getSTAT();
		setReg8(ATT_I, (byte) index);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setCRT(int, int)
     */
    public void setCRT(int index, int val) {
		setReg8(CRTC_I, (byte) index);
		setReg8(CRTC_D, (byte) val);
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACData(int)
     */
    public void setDACData(int data) {
        setReg8(DAC_D, data);
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACReadIndex(int)
     */
    public void setDACReadIndex(int index) {
        setReg8(DAC_RI, index);
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACWriteIndex(int)
     */
    public void setDACWriteIndex(int index) {
        setReg8(DAC_WI, index);
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setGRAF(int, int)
     */
    public void setGRAF(int index, int val) {
		setReg8(GRAF_I, (byte) index);
		setReg8(GRAF_D, (byte) val);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setMISC(int)
     */
    public void setMISC(int val) {
		setSEQ(0, 1);
		setReg8(MISC_W, (byte) val);
		setSEQ(0, 3);
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setSEQ(int, int)
     */
    public void setSEQ(int index, int val) {
		setReg8(SEQ_I, (byte) index);
		setReg8(SEQ_D, (byte) val);
    }
    
    /**
     * @param videoRam The videoRam to set.
     */
    final void setVideoRam(MemoryResource videoRam) {
        this.videoRam = videoRam;
    }
}
