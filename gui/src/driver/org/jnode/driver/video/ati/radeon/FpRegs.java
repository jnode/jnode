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
final class FpRegs implements RadeonConstants {

    /* Flat panel regs */
    private int fp_crtc_h_total_disp;

    private int fp_crtc_v_total_disp;

    private int fp_gen_cntl;

    private int fp_h_sync_strt_wid;

    private int fp_horz_stretch;

    private int fp_v_sync_strt_wid;

    private int fp_vert_stretch;

    private int lvds_gen_cntl;

    private int lvds_pll_cntl;

    private int tmds_crc;

    private int tmds_transmitter_cntl;

    /**
     * Initialize from a given IO.
     */
    public void saveFromVGA(RadeonVgaIO io) {
        this.fp_crtc_h_total_disp = io.getReg32(FP_CRTC_H_TOTAL_DISP);
        this.fp_crtc_v_total_disp = io.getReg32(FP_CRTC_V_TOTAL_DISP);
        this.fp_h_sync_strt_wid = io.getReg32(FP_H_SYNC_STRT_WID);
        this.fp_v_sync_strt_wid = io.getReg32(FP_V_SYNC_STRT_WID);
        this.fp_horz_stretch = io.getReg32(FP_HORZ_STRETCH);
        this.fp_vert_stretch = io.getReg32(FP_VERT_STRETCH);
        this.fp_gen_cntl = io.getReg32(FP_GEN_CNTL);
        this.lvds_gen_cntl = io.getReg32(LVDS_GEN_CNTL);
        this.lvds_pll_cntl = io.getReg32(LVDS_PLL_CNTL);
        this.tmds_crc = io.getReg32(TMDS_CRC);
        this.tmds_transmitter_cntl = io.getReg32(TMDS_TRANSMITTER_CNTL);
    }

    /**
     * Save this state to VGA.
     */
    public final void restoreToVGA(RadeonVgaIO io) {
        io.setReg32(FP_CRTC_H_TOTAL_DISP, fp_crtc_h_total_disp);
        io.setReg32(FP_CRTC_V_TOTAL_DISP, fp_crtc_v_total_disp);
        io.setRegP32(FP_GEN_CNTL, fp_gen_cntl, FP_SEL_CRTC2);
        io.setReg32(FP_H_SYNC_STRT_WID, fp_h_sync_strt_wid);
        // io.setReg32(FP_HORZ_STRETCH, fp_horz_stretch);
        io.setReg32(FP_V_SYNC_STRT_WID, fp_v_sync_strt_wid);
        // io.setReg32(FP_VERT_STRETCH, fp_vert_stretch);
        io.setRegP32(LVDS_GEN_CNTL, lvds_gen_cntl, LVDS_ON | LVDS_BLON);
        // io.setReg32(LVDS_PLL_CNTL, lvds_pll_cntl);
        io.setReg32(TMDS_CRC, tmds_crc);
        io.setReg32(TMDS_TRANSMITTER_CNTL, tmds_transmitter_cntl);
    }

    /**
     * Set this state up for a given configuration;
     * 
     * @param config
     * @param io
     */
    final void calcForConfiguration(RadeonConfiguration config, PLLInfo pllInfo, RadeonVgaIO io,
            FBInfo fbinfo, CrtcRegs crtc) {
        final DisplayMode mode = config.getDisplayMode();
        final int bpp = config.getBitsPerPixel();

        final int panel_xres = fbinfo.getPanelXres();
        final int panel_yres = fbinfo.getPanelYres();

        final int xres = Math.max(mode.getWidth(), panel_xres);
        final int yres = Math.max(mode.getHeight(), panel_yres);

        final float hRatio = (float) xres / (float) panel_xres;
        final float vRatio = (float) yres / (float) panel_yres;

        if ((hRatio == 1.0f) || true) {
            fp_horz_stretch &= ~(HORZ_STRETCH_BLEND | HORZ_STRETCH_ENABLE);
            fp_horz_stretch &= ~(HORZ_AUTO_RATIO | HORZ_PANEL_SIZE);
            fp_horz_stretch |= ((xres / 8 - 1) << 16);
        } else {
            fp_horz_stretch =
                    (((int) ((hRatio * HORZ_STRETCH_RATIO_MAX + 0.5)) & HORZ_STRETCH_RATIO_MASK)) |
                            (fp_horz_stretch & (HORZ_PANEL_SIZE | HORZ_FP_LOOP_STRETCH | HORZ_AUTO_RATIO_INC));
            fp_horz_stretch |= (HORZ_STRETCH_BLEND | HORZ_STRETCH_ENABLE);

            fp_horz_stretch &= ~(HORZ_AUTO_RATIO | HORZ_PANEL_SIZE);
            fp_horz_stretch |= ((panel_xres / 8 - 1) << HORZ_PANEL_SHIFT);
        }

        if ((vRatio == 1.0f) || true) {
            fp_vert_stretch &= ~(VERT_STRETCH_ENABLE | VERT_STRETCH_BLEND);
            fp_vert_stretch &= ~(VERT_AUTO_RATIO_EN | VERT_PANEL_SIZE);
            fp_vert_stretch |= ((yres - 1) << 12);
        } else {
            fp_vert_stretch = (((((int) (vRatio * VERT_STRETCH_RATIO_MAX + 0.5)) & VERT_STRETCH_RATIO_MASK)) |
                (fp_vert_stretch & (VERT_PANEL_SIZE | VERT_STRETCH_RESERVED)));
            fp_vert_stretch |= (VERT_STRETCH_ENABLE | VERT_STRETCH_BLEND);

            fp_vert_stretch &= ~(VERT_AUTO_RATIO_EN | VERT_PANEL_SIZE);
            fp_vert_stretch |= ((panel_yres - 1) << VERT_PANEL_SHIFT);

        }

        fp_gen_cntl &= ~(FP_SEL_CRTC2 | FP_RMX_HVSYNC_CONTROL_EN | FP_DFP_SYNC_SEL | FP_CRT_SYNC_SEL |
            /* FP_CRTC_LOCK_8DOT | FP_USE_SHADOW_EN | */ FP_CRTC_USE_SHADOW_VEND | FP_CRT_SYNC_ALT);

        fp_gen_cntl |= (FP_CRTC_DONT_SHADOW_VPAR | FP_CRTC_DONT_SHADOW_HEND);

        if (fbinfo.getPrimaryMonitorType() == MonitorType.LCD) {
            lvds_gen_cntl |= (LVDS_ON | LVDS_BLON);
            fp_gen_cntl &= ~(FP_FPON | FP_TMDS_EN);
        } else {
            /* DFP */
            fp_gen_cntl |= (FP_FPON | FP_TMDS_EN);
            tmds_transmitter_cntl = (TMDS_RAN_PAT_RST | ICHCSEL | TMDS_PLL_EN) & ~(TMDS_PLLRST);
        }

        fp_crtc_h_total_disp = crtc.getCrtc_h_total_disp();
        fp_crtc_v_total_disp = crtc.getCrtc_v_total_disp();
        fp_h_sync_strt_wid = crtc.getCrtc_h_sync_strt_wid();
        fp_v_sync_strt_wid = crtc.getCrtc_v_sync_strt_wid();
    }

    private static final int round_div(int num, int den) {
        return (num + (den / 2)) / den;
    }

    public String toString() {
        return "fp_crtc_h_total_disp:" + fp_crtc_h_total_disp + ", fp_crtc_v_total_disp:" +
                fp_crtc_v_total_disp + ", fp_gen_cntl:0x" + NumberUtils.hex(fp_gen_cntl) +
                ", fp_h_sync_strt_wid:" + fp_h_sync_strt_wid + ", fp_horz_stretch:" +
                fp_horz_stretch + ", fp_v_sync_strt_wid:" + fp_v_sync_strt_wid +
                ", fp_vert_stretch:" + fp_vert_stretch + ", lvds_gen_cntl:0x" +
                NumberUtils.hex(lvds_gen_cntl) + ", lvds_pll_cntl:0x" +
                NumberUtils.hex(lvds_pll_cntl) + ", tmds_crc:" + tmds_crc +
                ", tmds_transmitter_cntl:0x" + NumberUtils.hex(tmds_transmitter_cntl);
    }
}
