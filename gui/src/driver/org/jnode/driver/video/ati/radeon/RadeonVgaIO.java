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
    
    public RadeonVgaIO(MemoryResource mmio) {
        this.mmio = mmio;
    }
    
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getATT(int)
     */
    public int getATT(int index) {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getCRT(int)
     */
    public int getCRT(int index) {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getDACData()
     */
    public int getDACData() {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getGRAF(int)
     */
    public int getGRAF(int index) {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getMISC()
     */
    public int getMISC() {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSEQ(int)
     */
    public int getSEQ(int index) {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getSTAT()
     */
    public int getSTAT() {
        // TODO Auto-generated method stub
        return 0;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#getVideoMem()
     */
    public MemoryResource getVideoMem() {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATT(int, int)
     */
    public void setATT(int index, int val) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setATTIndex(int)
     */
    public void setATTIndex(int index) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setCRT(int, int)
     */
    public void setCRT(int index, int val) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACData(int)
     */
    public void setDACData(int data) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACReadIndex(int)
     */
    public void setDACReadIndex(int index) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setDACWriteIndex(int)
     */
    public void setDACWriteIndex(int index) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setGRAF(int, int)
     */
    public void setGRAF(int index, int val) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setMISC(int)
     */
    public void setMISC(int val) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jnode.driver.video.vgahw.VgaIO#setSEQ(int, int)
     */
    public void setSEQ(int index, int val) {
        // TODO Auto-generated method stub

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
     * @return
     */
    final void setReg32(int addr, int value) {
        mmio.setInt(addr, value);
    }
}
