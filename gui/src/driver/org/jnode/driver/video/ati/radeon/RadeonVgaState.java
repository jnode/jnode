/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.driver.video.vgahw.VgaState;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonVgaState extends VgaState implements RadeonConstants {

    /* CRTC regs */
    private final RadeonCrtcRegs crtc;

    /* Common regs */
    private final RadeonCommonRegs common;

    /* PLL regs */
    private final RadeonPllRegs pll;
    private final RadeonPllRegs pll2;
    
    /* Flatpanel regs */
    private final RadeonFpRegs fp;

    /**
     * Initialize from a given IO.
     * 
     * @param io
     */
    public RadeonVgaState(boolean hasCRTC2, RadeonVgaIO io) {
        this.common = new RadeonCommonRegs();
        this.crtc = new RadeonCrtcRegs();
        this.fp = new RadeonFpRegs();
        this.pll = new RadeonPllRegs(false);
        this.pll2 = hasCRTC2 ? new RadeonPllRegs(true) : null;
        saveFromVGA(io);
    }

    /**
     * Set this state up for a given configuration;
     * @param config
     * @param io
     */
    final void calcForConfiguration(RadeonConfiguration config, RadeonPLLInfo pllInfo, RadeonVgaIO io) {
        crtc.calcForConfiguration(config, pllInfo, io);
        pll.calcForConfiguration(config.getDisplayMode().getFreq() / 10, pllInfo);
    }
        
    /**
     * Initialize from a given IO.
     * 
     * @param vgaIO
     */
    public void saveFromVGA(VgaIO vgaIO) {
        final RadeonVgaIO io = (RadeonVgaIO)vgaIO;        
        super.saveFromVGA(io);
        common.saveFromVGA(io);
        crtc.saveFromVGA(io);
        fp.saveFromVGA(io);
        savePLL(io);
    }

    /**
     * Save this state to VGA.
     * @param vgaIO
     */
    public final void restoreToVGA(VgaIO vgaIO) {
        final RadeonVgaIO io = (RadeonVgaIO)vgaIO;
        super.restoreToVGA(io);
        common.restoreToVGA(io);
        crtc.restoreToVGA(io);
        fp.saveFromVGA(io);
        restorePLL(io);
    }

    
    /**
     * @see org.jnode.driver.video.vgahw.VgaState#toString()
     */
    public String toString() {
        return pll.toString();
    }
    
    /**
     * Save the PLL registers.
     * @param io
     */
    private final void savePLL(RadeonVgaIO io) {
        pll.savePLL(io);
    }

    /**
     * Restore the PLL registers
     * @param io
     */
    private final void restorePLL(RadeonVgaIO io) {
        pll.restorePLL(io);
    }
}