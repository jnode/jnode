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

import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PllRegs implements RadeonConstants {

    private final int architecture;

    private final boolean crtc2;

    private static final int[] DIVIDERS = {1, 2, 4, 8, 3, 16, 6, 12,};

    /* PLL regs */
    private int ppll_div_3;

    private int ppll_ref_div;

    private int htotal_cntl;

    private int vclk_ecp_cntl;

    /**
     * 
     * @param crtc2
     */
    public PllRegs(int architecture, boolean crtc2) {
        this.architecture = architecture;
        this.crtc2 = crtc2;
    }

    /**
     * Save the PLL registers.
     * 
     * @param io
     */
    public final void savePLL(RadeonVgaIO io) {
        this.ppll_ref_div = io.getPLL(PPLL_REF_DIV);
        this.ppll_div_3 = io.getPLL(PPLL_DIV_3);
        this.htotal_cntl = io.getPLL(HTOTAL_CNTL);
        this.vclk_ecp_cntl = io.getPLL(VCLK_ECP_CNTL);

        dumpPLLRegs(io, "@@@ SavePLL");
    }

    /**
     * Restore the PLL registers
     * 
     * @param io
     */
    public final void restorePLL(RadeonVgaIO io) {
        // do {
        // io.setRegP32(CLOCK_CNTL_INDEX, PPLL_DIV_SEL_MASK, 0xffff);
        // } while ((io.getReg32(CLOCK_CNTL_INDEX) & PPLL_DIV_SEL_MASK) !=
        // PPLL_DIV_SEL_MASK);
        //
        // io.setPLLP(PPLL_CNTL, PPLL_RESET, 0xffff);
        //
        // do {
        // io.setPLLP(PPLL_REF_DIV, ppll_ref_div, ~PPLL_REF_DIV_MASK);
        // } while ((io.getPLL(PPLL_REF_DIV) & PPLL_REF_DIV_MASK) !=
        // (ppll_ref_div & PPLL_REF_DIV_MASK));
        //
        // do {
        // io.setPLLP(PPLL_DIV_3, ppll_div_3, ~PPLL_FB3_DIV_MASK);
        // } while ((io.getPLL(PPLL_DIV_3) & PPLL_FB3_DIV_MASK) !=
        // (ppll_div_3 & PPLL_FB3_DIV_MASK));
        //
        // do {
        // io.setPLLP(PPLL_DIV_3, ppll_div_3, ~PPLL_POST3_DIV_MASK);
        // } while ((io.getPLL(PPLL_DIV_3) & PPLL_POST3_DIV_MASK) !=
        // (ppll_div_3 & PPLL_POST3_DIV_MASK));
        //
        // io.setPLL(HTOTAL_CNTL, 0);
        // io.setPLLP(PPLL_CNTL, 0, ~PPLL_RESET);

        // Swich VCKL clock input to CPUCLK so it stays fed while PPLL updates
        io.setPLLP(VCLK_ECP_CNTL, VCLK_SRC_CPU_CLK, ~VCLK_SRC_SEL_MASK);

        // Reset PPLL & enable atomic update
        io.setPLLP(PPLL_CNTL, PPLL_RESET | PPLL_ATOMIC_UPDATE_EN | PPLL_VGA_ATOMIC_UPDATE_EN,
                ~(PPLL_RESET | PPLL_ATOMIC_UPDATE_EN | PPLL_VGA_ATOMIC_UPDATE_EN));

        /* Switch to PPLL div 3 */
        io.setRegP32(CLOCK_CNTL_INDEX, PPLL_DIV_SEL_MASK, ~PPLL_DIV_SEL_MASK);

        /* Set PPLL ref. div */
        switch (architecture) {
            default:
                io.setPLLP(PPLL_REF_DIV, ppll_ref_div, ~PPLL_REF_DIV_MASK);
                break;
            case Architecture.R300:
                throw new Error("Not implemented");
                // if (ppll_ref_div & R300_PPLL_REF_DIV_ACC_MASK) {
                // /* When restoring console mode, use saved PPLL_REF_DIV
                // * setting.
                // */
                // io.setPLLP(PPLL_REF_DIV, ppll_ref_div, 0);
                // } else {
                // /* R300 uses ref_div_acc field as real ref divider */
                // io.setPLLP(PPLL_REF_DIV,
                // (ppll_ref_div << R300_PPLL_REF_DIV_ACC_SHIFT),
                // ~R300_PPLL_REF_DIV_ACC_MASK);
                // }
                // break;
        }

        // Set PPLL divider 3 & post divider
        io.setPLLP(PPLL_DIV_3, ppll_div_3, ~PPLL_FB3_DIV_MASK);
        io.setPLLP(PPLL_DIV_3, ppll_div_3, ~PPLL_POST3_DIV_MASK);

        /* Write update */
        while ((io.getPLL(PPLL_REF_DIV) & PPLL_ATOMIC_UPDATE_R) != 0) {
            // Wait
        }
        io.setPLLP(PPLL_REF_DIV, PPLL_ATOMIC_UPDATE_W, ~PPLL_ATOMIC_UPDATE_W);

        /* Wait read update complete */
        /*
         * FIXME: Certain revisions of R300 can't recover here. Not sure of the
         * cause yet, but this workaround will mask the problem for now. Other
         * chips usually will pass at the very first test, so the workaround
         * shouldn't have any effect on them.
         */
        for (int i = 0; (i < 10000) && ((io.getPLL(PPLL_REF_DIV) & PPLL_ATOMIC_UPDATE_R) != 0); i++) {
            // Wait
        }

        io.setPLL(HTOTAL_CNTL, 0);

        /* Clear reset & atomic update */
        io.setPLLP(PPLL_CNTL, 0,
                ~(PPLL_RESET | PPLL_SLEEP | PPLL_ATOMIC_UPDATE_EN | PPLL_VGA_ATOMIC_UPDATE_EN));

        /* We may want some locking ... oh well */
        TimeUtils.sleep(5);

        /* Switch back VCLK source to PPLL */
        io.setPLLP(VCLK_ECP_CNTL, VCLK_SRC_PPLL_CLK, ~VCLK_SRC_SEL_MASK);

        dumpPLLRegs(io, "@@@ RestorePLL");
    }

    /**
     * Finalize the restoring of the PLL registers
     * 
     * @param io
     */
    public final void finalizeRestorePLL(RadeonVgaIO io) {
        io.setPLL(VCLK_ECP_CNTL, vclk_ecp_cntl);
    }

    /**
     * Print the contents of the PLL registers on System.out.
     * 
     * @param io
     * @param msg
     */
    private void dumpPLLRegs(RadeonVgaIO io, String msg) {
        if (false) {
            if (false) {
                System.out.println(msg);
                for (int i = 0; i <= 0x2e; i++) {
                    System.out.println("PLL[" + NumberUtils.hex(i, 2) + "]=" +
                            NumberUtils.hex(io.getPLL(i)));
                }
            } else {
                System.out.println(msg);
                System.out.println("PPLL_DIV_3   0x" + NumberUtils.hex(io.getPLL(PPLL_DIV_3)));
                System.out.println("PPLL_REF_DIV 0x" + NumberUtils.hex(io.getPLL(PPLL_REF_DIV)));
                System.out.println("HTOTAL_CNTL  0x" + NumberUtils.hex(io.getPLL(HTOTAL_CNTL)));
            }
        }
    }

    /**
     * Calculate PLL dividers (freq is in 10kHz)
     * 
     */
    public final void calcForConfiguration(FBInfo fbinfo, int freq, PLLInfo info) {

        final FPIBlock fpi = fbinfo.getFpi();
        if ((fpi != null) && fpi.useBiosDividers() && false) {
            this.ppll_ref_div = fpi.getBiosRefDivider();
            this.ppll_div_3 = (fpi.getBiosFeedbackDivider() | (fpi.getBiosPostDivider() << 16));
            this.htotal_cntl = 0;
        } else {
            // formula is for generated frequency is:
            // (ref_freq * feedback_div) / (ref_div * post_div )

            // System.out.println("Req. freq=" + freq);
            int pll_output_freq = 0;
            final int ppll_max = info.getMaxPllFreq();
            final int ppll_min = info.getMinPllFreq();
            freq = Math.min(freq, ppll_max);
            if (freq * 12 < ppll_min) {
                freq = ppll_min / 12;
            }
            // System.out.println("Act. freq=" + freq);

            // find proper divider by trial-and-error
            int bitvalue;
            for (bitvalue = 0; bitvalue < DIVIDERS.length; bitvalue++) {
                pll_output_freq = DIVIDERS[bitvalue] * freq;
                // System.out.println("bitvalue=" + bitvalue + ", post_div="
                // + pll_output_freq);

                if (pll_output_freq >= ppll_min && pll_output_freq <= ppll_max) {
                    break;
                }
            }

            if (bitvalue >= DIVIDERS.length) {
                throw new IllegalArgumentException("Frequency (" + freq +
                        " kHz) is out of PLL range!");
            }

            final int ref_clk = info.getRef_clk();
            final int ref_div = info.getRef_div();
            final int feedback_div = (ref_div * pll_output_freq) / ref_clk;

            this.ppll_ref_div = ref_div;
            this.ppll_div_3 = (feedback_div | (bitvalue << 16));
            this.htotal_cntl = 0;

            final int vclk_freq = (ref_clk * feedback_div) / (ref_div * pll_output_freq);
            // System.out.println("vclk_freq=" + vclk_freq + ", fbdiv="
            // + feedback_div + ", postdiv=" + pll_output_freq
            // + ", bitvalue=" + bitvalue);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ppll_ref_div:" + ppll_ref_div + ", ppll_div_3:0x" + NumberUtils.hex(ppll_div_3) +
                ", htotal_cntl:0x" + NumberUtils.hex(htotal_cntl);
    }

    private final void PLLWriteUpdate(RadeonVgaIO io) {
        PLLWaitForReadUpdateComplete(io);
        io.setPLLP(crtc2 ? P2PLL_REF_DIV : PPLL_REF_DIV, PPLL_ATOMIC_UPDATE_W,
                ~PPLL_ATOMIC_UPDATE_W);
    }

    private final void PLLWaitForReadUpdateComplete(RadeonVgaIO io) {
        int i;

        // we should wait forever, but
        // 1. this is unsafe
        // 2. some r300 loop forever (reported by XFree86)
        for (i = 0; i < 10000; ++i) {
            if ((io.getPLL(crtc2 ? P2PLL_REF_DIV : PPLL_REF_DIV) & PPLL_ATOMIC_UPDATE_R) == 0) {
                return;
            }
        }
    }
}
