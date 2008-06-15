/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.driver.video.vgahw.VgaState;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonVgaState extends VgaState implements RadeonConstants {

    private boolean calculated = false;

    /* CRTC regs */
    private final CrtcRegs crtc1;
    private final CrtcRegs crtc2;

    /* Common regs */
    private final CommonRegs common;

    /* PLL regs */
    private final PllRegs pll;
    // private final RadeonPllRegs pll2;

    /* Flatpanel regs */
    private final FpRegs fp;

    /**
     * Initialize from a given IO.
     * 
     * @param io
     */
    public RadeonVgaState(int architecture, boolean hasCRTC2, RadeonVgaIO io) {
        this.common = new CommonRegs();
        this.crtc1 = new CrtcRegs(0);
        this.crtc2 = (hasCRTC2 ? new CrtcRegs(1) : null);
        this.fp = new FpRegs();
        this.pll = new PllRegs(architecture, false);
        // this.pll2 = hasCRTC2 ? new RadeonPllRegs(true) : null;
        saveFromVGA(io);
    }

    /**
     * Set this state up for a given configuration;
     * 
     * @param config
     * @param io
     */
    final void calcForConfiguration(RadeonConfiguration config, RadeonVgaIO io, FBInfo fbinfo) {
        saveFromVGA(io);
        PLLInfo pllInfo = fbinfo.getPllInfo();
        common.calcForConfiguration();
        crtc1.calcForConfiguration(config, pllInfo, io, fbinfo);
        if (crtc2 != null) {
            crtc2.calcForConfiguration(config, pllInfo, io, fbinfo);
        }
        fp.calcForConfiguration(config, pllInfo, io, fbinfo, crtc1);
        pll.calcForConfiguration(fbinfo, config.getDisplayMode().getFreq() / 10, pllInfo);
        calculated = true;
    }

    /**
     * Initialize from a given IO.
     * 
     * @param vgaIO
     */
    public void saveFromVGA(VgaIO vgaIO) {
        final RadeonVgaIO io = (RadeonVgaIO) vgaIO;
        super.saveFromVGA(io);
        common.saveFromVGA(io);
        crtc1.saveFromVGA(io);
        if (crtc2 != null) {
            crtc2.saveFromVGA(io);
        }
        fp.saveFromVGA(io);
        pll.savePLL(io);
    }

    /**
     * Save this state to VGA.
     * 
     * @param vgaIO
     */
    public final void restoreToVGA(VgaIO vgaIO) {
        final RadeonVgaIO io = (RadeonVgaIO) vgaIO;

        if (!calculated) {
            super.restoreToVGA(io);
        }
        common.restoreToVGA(io);
        crtc1.restoreToVGA(io);
        if (crtc2 != null) {
            crtc2.restoreToVGA(io);
        }
        pll.restorePLL(io);
        fp.restoreToVGA(io);
        restorePalette(io);
        pll.finalizeRestorePLL(io);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#toString()
     */
    public String toString() {
        return pll.toString() + ", " + crtc1 + ", " + fp;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#getPaletteEntry(org.jnode.driver.video.vgahw.VgaIO,
     *      int, byte[], byte[], byte[])
     */
    protected void getPaletteEntry(VgaIO vgaIO, int colorIndex, byte[] r, byte[] g, byte[] b) {
        final RadeonVgaIO io = (RadeonVgaIO) vgaIO;
        io.getPaletteEntry(colorIndex, r, g, b);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#setPaletteEntry(org.jnode.driver.video.vgahw.VgaIO,
     *      int, int, int, int)
     */
    protected void setPaletteEntry(VgaIO vgaIO, int colorIndex, int r, int g, int b) {
        final RadeonVgaIO io = (RadeonVgaIO) vgaIO;
        io.setPaletteEntry(colorIndex, r, g, b);
    }
}
