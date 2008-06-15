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

import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class CrtcRegs implements RadeonConstants {

    private final int crtcIndex;

    private int crtc_h_total_disp;

    private int crtc_h_sync_strt_wid;

    private int crtc_v_total_disp;

    private int crtc_v_sync_strt_wid;

    private int crtc_pitch;

    private int crtc_gen_cntl;

    private int crtc_ext_cntl;

    private int dac_cntl;

    private int crtc_offset;

    private int crtc_offset_cntl;

    private int crtc_more_cntl;

    /**
     * Initialize this instance.
     * 
     * @param crtcIndex
     */
    public CrtcRegs(int crtcIndex) {
        if ((crtcIndex < 0) || (crtcIndex > 1)) {
            throw new IllegalArgumentException("Invalid crtcIndex");
        }
        this.crtcIndex = crtcIndex;
    }

    /**
     * Initialize from a given IO.
     */
    public void saveFromVGA(RadeonVgaIO io) {
        if (crtcIndex == 0) {
            this.crtc_gen_cntl = io.getReg32(CRTC_GEN_CNTL);
            this.crtc_ext_cntl = io.getReg32(CRTC_EXT_CNTL);
            this.dac_cntl = io.getReg32(DAC_CNTL);

            this.crtc_h_total_disp = io.getReg32(CRTC_H_TOTAL_DISP);
            this.crtc_h_sync_strt_wid = io.getReg32(CRTC_H_SYNC_STRT_WID);
            this.crtc_v_total_disp = io.getReg32(CRTC_V_TOTAL_DISP);
            this.crtc_v_sync_strt_wid = io.getReg32(CRTC_V_SYNC_STRT_WID);
            this.crtc_offset = io.getReg32(CRTC_OFFSET);
            this.crtc_offset_cntl = io.getReg32(CRTC_OFFSET_CNTL);
            this.crtc_pitch = io.getReg32(CRTC_PITCH);
            this.crtc_more_cntl = io.getReg32(CRTC_MORE_CNTL);
        } else {
            this.crtc_gen_cntl = io.getReg32(CRTC2_GEN_CNTL);

            this.crtc_h_total_disp = io.getReg32(CRTC2_H_TOTAL_DISP);
            this.crtc_h_sync_strt_wid = io.getReg32(CRTC2_H_SYNC_STRT_WID);
            this.crtc_v_total_disp = io.getReg32(CRTC2_V_TOTAL_DISP);
            this.crtc_v_sync_strt_wid = io.getReg32(CRTC2_V_SYNC_STRT_WID);
            this.crtc_offset = io.getReg32(CRTC2_OFFSET);
            this.crtc_offset_cntl = io.getReg32(CRTC2_OFFSET_CNTL);
            this.crtc_pitch = io.getReg32(CRTC2_PITCH);
        }
    }

    /**
     * Save this state to VGA.
     */
    public final void restoreToVGA(RadeonVgaIO io) {
        if (crtcIndex == 0) {
            io.setReg32(CRTC_GEN_CNTL, crtc_gen_cntl);
            io.setRegP32(CRTC_EXT_CNTL, crtc_ext_cntl, CRTC_VSYNC_DIS | CRTC_HSYNC_DIS |
                    CRTC_DISPLAY_DIS);
            // io.setRegP32(DAC_CNTL, dac_cntl, DAC_RANGE_CNTL | DAC_BLANKING);
            io.setReg32(DAC_CNTL, dac_cntl);
            io.setReg32(CRTC_H_TOTAL_DISP, crtc_h_total_disp);
            io.setReg32(CRTC_H_SYNC_STRT_WID, crtc_h_sync_strt_wid);
            io.setReg32(CRTC_V_TOTAL_DISP, crtc_v_total_disp);
            io.setReg32(CRTC_V_SYNC_STRT_WID, crtc_v_sync_strt_wid);
            io.setReg32(CRTC_OFFSET, crtc_offset);
            io.setReg32(CRTC_OFFSET_CNTL, crtc_offset_cntl);
            io.setReg32(CRTC_PITCH, crtc_pitch);
            io.setReg32(CRTC_MORE_CNTL, crtc_more_cntl);
        } else {
            io.setRegP32(CRTC2_GEN_CNTL, crtc_gen_cntl, CRTC2_VSYNC_DIS | CRTC2_HSYNC_DIS |
                    CRTC2_DISP_DIS | CRTC2_CRT2_ON);

            io.setReg32(CRTC2_H_TOTAL_DISP, crtc_h_total_disp);
            io.setReg32(CRTC2_H_SYNC_STRT_WID, crtc_h_sync_strt_wid);
            io.setReg32(CRTC2_V_TOTAL_DISP, crtc_v_total_disp);
            io.setReg32(CRTC2_V_SYNC_STRT_WID, crtc_v_sync_strt_wid);
            io.setReg32(CRTC2_OFFSET, crtc_offset);
            io.setReg32(CRTC2_OFFSET_CNTL, crtc_offset_cntl);
            io.setReg32(CRTC2_PITCH, crtc_pitch);
        }
    }

    /**
     * Set this state up for a given configuration;
     * 
     * @param config
     * @param io
     */
    final void calcForConfiguration(RadeonConfiguration config, PLLInfo pllInfo, RadeonVgaIO io,
            FBInfo fbinfo) {
        final DisplayMode mode = config.getDisplayMode();
        final int bpp = config.getBitsPerPixel();

        final int xres = mode.getWidth();
        final int yres = mode.getHeight();

        int hSyncStart = mode.getHsyncStart();
        int hSyncEnd = mode.getHsyncEnd();
        int hTotal = mode.getHTotal();

        int vSyncStart = mode.getVsyncStart();
        int vSyncEnd = mode.getVsyncEnd();
        int vTotal = mode.getVTotal();

        final int hsync_wid = Math.min(0x3f, Math.max(1, (hSyncEnd - hSyncStart) / 8));
        final int vsync_wid = Math.min(0x1f, Math.max(1, vSyncEnd - vSyncStart));

        final int hsync_start = mode.getHsyncStart() - 8 /* + hsync_fudge */;
        final int h_sync_pol = 0;
        final int v_sync_pol = 0;
        final int format = getFormat(bpp);

        // newmode.crtc_h_total_disp = ((((hTotal / 8) - 1) & 0x3ff) |
        // (((mode->xres / 8) - 1) << 16));
        //
        // newmode.crtc_h_sync_strt_wid = ((hsync_start & 0x1fff) |
        // (hsync_wid << 16) | (h_sync_pol << 23));
        //
        // newmode.crtc_v_total_disp = ((vTotal - 1) & 0xffff) |
        // ((mode->yres - 1) << 16);
        //
        // newmode.crtc_v_sync_strt_wid = (((vSyncStart - 1) & 0xfff) |
        // (vsync_wid << 16) | (v_sync_pol << 23));
        //
        // newmode.crtc_pitch = (mode->xres_virtual >> 3);
        // newmode.crtc_pitch |= (newmode.crtc_pitch << 16);

        this.crtc_h_total_disp = ((hTotal / 8 - 1) & 0x3ff) | ((xres / 8 - 1) << 16);
        this.crtc_h_sync_strt_wid =
                ((hsync_start & 0x1fff) | (hsync_wid << 16) | (h_sync_pol << 23));

        this.crtc_v_total_disp = ((vTotal - 1) & 0xffff) | ((yres - 1) << 16);

        this.crtc_v_sync_strt_wid =
                (((vSyncStart - 1) & 0xfff) | (vsync_wid << 16) | (v_sync_pol << 23));

        this.crtc_pitch = roundVWidth(xres, bpp >> 3) / 8;
        this.crtc_pitch |= (this.crtc_pitch << 16);

        this.crtc_ext_cntl = VGA_ATI_LINEAR | XCRT_CNT_EN | CRTC_CRT_ON;

        this.crtc_gen_cntl = CRTC_EXT_DISP_EN | CRTC_EN | (format << 8);
        this.dac_cntl |= (DAC_MASK_ALL | DAC_VGA_ADR_EN | DAC_8BIT_EN);

        // disable flat panel auto-centering
        // (if we have a CRT on CRTC1, this must be disabled;
        // if we have a flat panel on CRTC1, we setup CRTC manually, not
        // using the auto-centre, automatic-sync-override magic)
        this.crtc_more_cntl = 0;
    }

    private final int getFormat(int bpp) {
        final int format;
        switch (bpp) {
            case 4:
                format = 1;
                break;
            case 8:
                format = 2;
                break;
            case 15:
                format = 3;
                break; /* 555 */
            case 16:
                format = 4;
                break; /* 565 */
            case 24:
                format = 5;
                break; /* RGB */
            case 32:
                format = 6;
                break; /* xRGB */
            default:
                throw new IllegalArgumentException("Invalid bpp " + bpp);
        }
        return format;
    }

    /**
     * round virtual width up to next valid size
     */
    final static int roundVWidth(int virtual_width, int bpp) {
        // we have to make both the CRTC and the accelerator happy:
        // - the CRTC wants virtual width in pixels to be a multiple of 8
        // - the accelerator expects width in bytes to be a multiple of 64

        // to put that together, width (in bytes) must be a multiple of the
        // least
        // common nominator of bytes-per-pixel*8 (CRTC) and 64 (accelerator);

        // if bytes-per-pixel is a power of two and less than 8, the LCM is 64;
        // almost all colour depth satisfy that apart from 24 bit; in this case,
        // the LCM is 64*3=192

        // after dividing by bytes-per-pixel we get pixels: in first case,
        // width must be multiple of 64/bytes-per-pixel; in second case,
        // width must be multiple of 64*3/3=64

        if (bpp != 3) {
            return (virtual_width + 64 / bpp - 1) & ~(64 / bpp - 1);
        } else {
            return (virtual_width + 63) & ~63;
        }
    }

    /**
     * @return Returns the crtc_h_sync_strt_wid.
     */
    final int getCrtc_h_sync_strt_wid() {
        return crtc_h_sync_strt_wid;
    }

    /**
     * @return Returns the crtc_h_total_disp.
     */
    final int getCrtc_h_total_disp() {
        return crtc_h_total_disp;
    }

    /**
     * @return Returns the crtc_v_sync_strt_wid.
     */
    final int getCrtc_v_sync_strt_wid() {
        return crtc_v_sync_strt_wid;
    }

    /**
     * @return Returns the crtc_v_total_disp.
     */
    final int getCrtc_v_total_disp() {
        return crtc_v_total_disp;
    }

    public String toString() {
        return "crtc_h_total_disp:" + crtc_h_total_disp + ", crtc_h_sync_strt_wid:" +
                crtc_h_sync_strt_wid + ", crtc_v_total_disp:" + crtc_v_total_disp +
                ", crtc_v_sync_strt_wid:" + crtc_v_sync_strt_wid + ", crtc_pitch:0x" +
                NumberUtils.hex(crtc_pitch) + ", crtc_gen_cntl:0x" +
                NumberUtils.hex(crtc_gen_cntl) + ", crtc_ext_cntl:0x" +
                NumberUtils.hex(crtc_ext_cntl) + ", dac_cntl:0x" + NumberUtils.hex(dac_cntl) +
                ", crtc_offset:" + crtc_offset + ", crtc_offset_cntl:0x" +
                NumberUtils.hex(crtc_offset_cntl) + ", crtc_more_cntl:0x" +
                NumberUtils.hex(crtc_more_cntl);
    }
}
