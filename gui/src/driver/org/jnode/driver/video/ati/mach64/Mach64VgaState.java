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
 
package org.jnode.driver.video.ati.mach64;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Mach64VgaState implements Mach64Constants {

    private int crtc_h_total_disp;
    private int crtc_h_sync_strt_wid;
    private int crtc_v_total_disp;
    private int crtc_v_sync_strt_wid;

    private int crtc_off_pitch;

    private int crtc_gen_cntl;

    private int ovr_clr;
    private int ovr_wid_left_right;
    private int ovr_wid_top_bottom;

    private int cur_clr0;
    private int cur_clr1;
    private int cur_offset;
    private int cur_horz_vert_posn;
    private int cur_horz_vert_off;

    private int clock_cntl;

    private int bus_cntl;

    private int mem_vga_wp_sel;
    private int mem_vga_rp_sel;

    private int dac_cntl;

    private int config_cntl;

    private int gen_test_cntl;

    /**
     * Save the current card state into this instance.
     * 
     * @param io
     */
    public void saveFromVGA(Mach64VgaIO io) {
        crtc_h_total_disp = io.getReg32(CRTC_H_TOTAL_DISP);
        crtc_h_sync_strt_wid = io.getReg32(CRTC_H_SYNC_STRT_WID);
        crtc_v_total_disp = io.getReg32(CRTC_V_TOTAL_DISP);
        crtc_v_sync_strt_wid = io.getReg32(CRTC_V_SYNC_STRT_WID);

        crtc_off_pitch = io.getReg32(CRTC_OFF_PITCH);

        crtc_gen_cntl = io.getReg32(CRTC_GEN_CNTL);

        ovr_clr = io.getReg32(OVR_CLR);
        ovr_wid_left_right = io.getReg32(OVR_WID_LEFT_RIGHT);
        ovr_wid_top_bottom = io.getReg32(OVR_WID_TOP_BOTTOM);

        cur_clr0 = io.getReg32(CUR_CLR0);
        cur_clr1 = io.getReg32(CUR_CLR1);
        cur_offset = io.getReg32(CUR_OFFSET);
        cur_horz_vert_posn = io.getReg32(CUR_HORZ_VERT_POSN);
        cur_horz_vert_off = io.getReg32(CUR_HORZ_VERT_OFF);

        clock_cntl = io.getReg32(CLOCK_CNTL);

        bus_cntl = io.getReg32(BUS_CNTL);

        mem_vga_wp_sel = io.getReg32(MEM_VGA_WP_SEL);
        mem_vga_rp_sel = io.getReg32(MEM_VGA_RP_SEL);

        dac_cntl = io.getReg32(DAC_CNTL);

        config_cntl = io.getReg32(CONFIG_CNTL);

        gen_test_cntl = io.getReg32(GEN_TEST_CNTL) & ~HWCURSOR_ENABLE;
    }

    /**
     * Restore the state of this instance into the card.
     * 
     * @param io
     */
    public void restoreToVGA(Mach64VgaIO io) {
        io.setReg32(CRTC_H_TOTAL_DISP, crtc_h_total_disp);
        io.setReg32(CRTC_H_SYNC_STRT_WID, crtc_h_sync_strt_wid);
        io.setReg32(CRTC_V_TOTAL_DISP, crtc_v_total_disp);
        io.setReg32(CRTC_V_SYNC_STRT_WID, crtc_v_sync_strt_wid);

        io.setReg32(CRTC_OFF_PITCH, crtc_off_pitch);

        io.setReg32(CRTC_GEN_CNTL, crtc_gen_cntl);

        io.setReg32(OVR_CLR, ovr_clr);
        io.setReg32(OVR_WID_LEFT_RIGHT, ovr_wid_left_right);
        io.setReg32(OVR_WID_TOP_BOTTOM, ovr_wid_top_bottom);

        io.setReg32(CUR_CLR0, cur_clr0);
        io.setReg32(CUR_CLR1, cur_clr1);
        io.setReg32(CUR_OFFSET, cur_offset);
        io.setReg32(CUR_HORZ_VERT_POSN, cur_horz_vert_posn);
        io.setReg32(CUR_HORZ_VERT_OFF, cur_horz_vert_off);

        io.setReg32(CLOCK_CNTL, clock_cntl);

        io.setReg32(BUS_CNTL, bus_cntl);

        io.setReg32(MEM_VGA_WP_SEL, mem_vga_wp_sel);
        io.setReg32(MEM_VGA_RP_SEL, mem_vga_rp_sel);

        io.setReg32(DAC_CNTL, dac_cntl);

        io.setReg32(CONFIG_CNTL, config_cntl);

        io.setReg32(GEN_TEST_CNTL, gen_test_cntl);
    }

    /**
     * Set this state up for a given configuration;
     * 
     * @param config
     * @param io
     */
    final void calcForConfiguration(Mach64Configuration config, Mach64VgaIO io) {
        saveFromVGA(io);
    }

}
