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

import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.driver.video.vgahw.VgaState;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaVgaState extends VgaState implements NVidiaConstants {

    private int repaint0;
    private int repaint1;
    private int screen;
    private int pixel;
    private int horiz;
    private int cursor0;
    private int cursor1;
    private int cursor2;
    private int config;
    private int pwupctrl;
    private int general;
    private int vpll;
    private int pllsel;
    private int arb0;
    private int arb1;
    private int offset;
    private int pitch;
    private int palmask;
    private boolean loaded = false;

    /**
     * Create a new instance
     */
    public NVidiaVgaState() {
        super();
    }

    /**
     * Create a new instance
     * 
     * @param seq
     * @param crt
     * @param gra
     * @param att
     * @param misc
     */
    public NVidiaVgaState(int[] seq, int[] crt, int[] gra, int[] att, int misc) {
        super(seq, crt, gra, att, misc, null);
    }

    /**
     * Calculate the contenst of this state for a given configuration
     * 
     * @param cfg
     * @param architecture
     * @param io
     */
    public void calcForConfiguration(NVidiaConfiguration cfg, int architecture, NVidiaVgaIO io) {
        final int width = cfg.getScreenWidth();
        final int bpp = cfg.getBitsPerPixel();
        final int pixelDepth = (bpp + 1) / 8;
        switch (architecture) {
            case NV04A:
                this.cursor0 = 0x00;
                this.cursor1 = 0xfc;
                this.cursor2 = 0x00000000;
                this.pllsel = 0x10000700;
                this.config = 0x00001114;
                this.general = (bpp == 16) ? 0x00101100 : 0x00100100;
                this.repaint1 = (width < 1280) ? 0x04 : 0x00;
                break;
            case NV10A:
                this.cursor0 = 0x80;
                this.cursor1 = 0x00;
                this.cursor2 = 0x00000000;
                this.pllsel = 0x10000700;
                this.config = io.getFB(0x200);
                this.general = (bpp == 16) ? 0x00101100 : 0x00100100;
                this.repaint1 = (width < 1280) ? 0x04 : 0x00;
                break;
            default:
                throw new IllegalArgumentException("Unknown architecture " + architecture);
        }
        if ((bpp != 8) && (architecture >= NV04A)) {
            // Directcolor
            general |= 0x30;
        }
        this.pwupctrl = 0x13111111;
        this.vpll = cfg.getVpll();
        this.screen = cfg.getScreen();
        // state->horiz = hTotal < 260 ? 0x00 : 0x01;
        this.horiz = (cfg.getMode().getHTotal() < 260) ? 0x00 : 0x01;
        this.repaint0 = (((width / 8) * pixelDepth) & 0x700) >> 3;
        this.pixel = (pixelDepth > 2) ? 3 : pixelDepth;
        this.arb0 = cfg.getArb0();
        this.arb1 = cfg.getArb1();
        this.offset = 0;
        this.pitch = pixelDepth * width;
        this.palmask = 0xff;

        loaded = true;
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#restoreCRT(org.jnode.driver.video.vgahw.VgaIO)
     */
    protected void restoreCRT(VgaIO io) {
        super.restoreCRT(io);
        if (loaded) {
            io.setCRT(NVCRTCX_REPAINT0, repaint0);
            io.setCRT(NVCRTCX_REPAINT1, repaint1);
            io.setCRT(NVCRTCX_LSR, screen);
            io.setCRT(NVCRTCX_PIXEL, pixel);
            io.setCRT(NVCRTCX_HEB, horiz);
            io.setCRT(NVCRTCX_CURCTL0, cursor0);
            io.setCRT(NVCRTCX_CURCTL1, cursor1);
            io.setCRT(NVCRTCX_ARBITRATION0, arb0);
            io.setCRT(NVCRTCX_ARBITRATION1, arb1);
        }
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#saveCRT(org.jnode.driver.video.vgahw.VgaIO)
     */
    protected void saveCRT(VgaIO io) {
        super.saveCRT(io);
        repaint0 = io.getCRT(NVCRTCX_REPAINT0);
        repaint1 = io.getCRT(NVCRTCX_REPAINT1);
        screen = io.getCRT(NVCRTCX_LSR);
        pixel = io.getCRT(NVCRTCX_PIXEL);
        horiz = io.getCRT(NVCRTCX_HEB);
        cursor0 = io.getCRT(NVCRTCX_CURCTL0);
        cursor1 = io.getCRT(NVCRTCX_CURCTL1);
        arb0 = io.getCRT(NVCRTCX_ARBITRATION0);
        arb1 = io.getCRT(NVCRTCX_ARBITRATION1);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#restoreToVGA(org.jnode.driver.video.vgahw.VgaIO)
     */
    public void restoreToVGA(VgaIO vgaIO) {
        final NVidiaVgaIO io = (NVidiaVgaIO) vgaIO;
        io.unlock();
        super.restoreToVGA(io);
        io.unlock();
        if (loaded) {
            io.setReg32(NV32_PWRUPCTRL, pwupctrl);
            io.setFB(0x200, config);
            io.setReg32(NVDAC_CURPOS, cursor2);
            io.setReg32(NVDAC_PIXPLLC, vpll);
            io.setReg32(NVDAC_PLLSEL, pllsel);
            io.setReg32(NVDAC_GENCTRL, general);
            io.setReg32(NV_PGRAPH_BOFFSET0, offset);
            io.setReg32(NV_PGRAPH_BOFFSET1, offset);
            io.setReg32(NV_PGRAPH_BOFFSET2, offset);
            io.setReg32(NV_PGRAPH_BOFFSET3, offset);
            io.setReg32(NV_PGRAPH_BPITCH0, pitch);
            io.setReg32(NV_PGRAPH_BPITCH1, pitch);
            io.setReg32(NV_PGRAPH_BPITCH2, pitch);
            io.setReg32(NV_PGRAPH_BPITCH3, pitch);
        }
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#saveFromVGA(org.jnode.driver.video.vgahw.VgaIO)
     */
    public void saveFromVGA(VgaIO vgaIO) {
        final NVidiaVgaIO io = (NVidiaVgaIO) vgaIO;
        super.saveFromVGA(io);
        pwupctrl = io.getReg32(NV32_PWRUPCTRL);
        cursor2 = io.getReg32(NVDAC_CURPOS);
        vpll = io.getReg32(NVDAC_PIXPLLC);
        pllsel = io.getReg32(NVDAC_PLLSEL);
        general = io.getReg32(NVDAC_GENCTRL);
        config = io.getFB(0x200);
        offset = io.getReg32(NV_PGRAPH_BOFFSET0);
        pitch = io.getReg32(NV_PGRAPH_BPITCH0);
        loaded = true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + ", arb:" + NumberUtils.hex(arb0, 2) + "," +
                NumberUtils.hex(arb1, 2) + ", cursor:" + NumberUtils.hex(cursor0, 2) + "," +
                NumberUtils.hex(cursor1, 2) + "," + NumberUtils.hex(cursor2) + ", repaint:" +
                NumberUtils.hex(repaint0, 2) + "," + NumberUtils.hex(repaint1, 2) + ", screen:" +
                NumberUtils.hex(screen, 2) + ", horiz:" + NumberUtils.hex(horiz, 2) + ", pixel:" +
                NumberUtils.hex(pixel, 2) + ", pll:" + NumberUtils.hex(pllsel) + "," +
                NumberUtils.hex(vpll) + ", genctrl:" + NumberUtils.hex(general) + ", config:" +
                NumberUtils.hex(config) + ", pwupctrl:" + NumberUtils.hex(pwupctrl) + ", palmask:" +
                NumberUtils.hex(palmask, 2);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#restorePalette(org.jnode.driver.video.vgahw.VgaIO)
     */
    protected void restorePalette(VgaIO vgaIO) {
        final NVidiaVgaIO io = (NVidiaVgaIO) vgaIO;
        io.setReg8(NV8_PALMASK, palmask);
        super.restorePalette(io);
    }

    /**
     * @see org.jnode.driver.video.vgahw.VgaState#savePalette(org.jnode.driver.video.vgahw.VgaIO)
     */
    protected void savePalette(VgaIO vgaIO) {
        final NVidiaVgaIO io = (NVidiaVgaIO) vgaIO;
        palmask = io.getReg8(NV8_PALMASK);
        super.savePalette(io);
    }

    protected void getPaletteEntry(VgaIO io, int colorIndex, byte[] r, byte[] g, byte[] b) {
        super.getPaletteEntry(io, colorIndex, r, g, b);
        r[colorIndex] <<= 2;
        g[colorIndex] <<= 2;
        b[colorIndex] <<= 2;
    }

}
