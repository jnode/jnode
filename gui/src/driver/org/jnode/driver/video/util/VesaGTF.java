/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.driver.video.util;

/**
 * This is a implementation of Vesa's General Timing Formula base on the code
 * of the Linux gtf tool
 * <p>
 * Timing description I'm accustomed to:
 * <pre>
 * <--------1--------> <--2--> <--3--> <--4--> _________
 * |-------------------|_______| |_______
 * 
 * R SS SE FL
 * </pre>
 * 1: visible image 2: blank before sync (aka front porch) 3: sync pulse 4:
 * blank after sync (aka back porch) R: Resolution SS: Sync Start SE: Sync End
 * FL: Frame Length
 */
public class VesaGTF {

    /* assumed character width in pixels */
    private static final double CELL_GRAN = 8.0;

    /* minimum front porch */
    private static final double MIN_PORCH = 1.0;

    /* min time of vsync + back porch (microsec) */
    private static final double MIN_VSYNC_PLUS_BP = 550.0;

    /* width of vsync in lines */
    private static final double V_SYNC_RQD = 3.0;

    /* width of hsync as % of total line */
    private static final double H_SYNC_PERCENT = 8.0;

    /* C' and M' are part of the Blanking Duty Cycle computation */
    private static final double M_PRIME = 300.0;

    private static final double C_PRIME = 30.0;

    private int hResolution;

    private int hSyncStart;

    private int hSyncEnd;

    private int hFrameLength;

    private int vResolution;

    private int vSyncStart;

    private int vSyncEnd;

    private int vFrameLength;

    private double pixelClock;

    private double hFrequency;

    private double vFrequency;

    public VesaGTF(int hResolution, int hSyncStart, int hSyncEnd,
            int hFrameLength, int vResolution, int vSyncStart, int vSyncEnd,
            int vFrameLength, double pixelClock, double hFrequency,
            double vFrequency) {
        super();
        this.hResolution = hResolution;
        this.hSyncStart = hSyncStart;
        this.hSyncEnd = hSyncEnd;
        this.hFrameLength = hFrameLength;
        this.vResolution = vResolution;
        this.vSyncStart = vSyncStart;
        this.vSyncEnd = vSyncEnd;
        this.vFrameLength = vFrameLength;
        this.pixelClock = pixelClock;
        this.hFrequency = hFrequency;
        this.vFrequency = vFrequency;
    }

    /**
     * calculate the modelines for the given screen resolution and refresh rate.
     * 
     * @param hPixels
     * @param vLines
     * @param frequency
     * @return the modelines
     */
    public static VesaGTF calculate(int hPixels, int vLines, double frequency) {

        double total_active_pixels = Math.round(hPixels / CELL_GRAN)
                * CELL_GRAN;

        double h_period_est = ((1.0 / frequency) - (MIN_VSYNC_PLUS_BP / 1000000.0))
                / (vLines + MIN_PORCH) * 1000000.0;

        double vsync_plus_bp = Math.round(MIN_VSYNC_PLUS_BP / h_period_est);

        double total_v_lines = vLines + vsync_plus_bp + MIN_PORCH;

        double v_field_rate_est = 1.0 / h_period_est / total_v_lines
                * 1000000.0;

        double h_period = h_period_est / (frequency / v_field_rate_est);

        double ideal_duty_cycle = C_PRIME - (M_PRIME * h_period / 1000.0);

        double h_blank = Math.round(total_active_pixels * ideal_duty_cycle
                / (100.0 - ideal_duty_cycle) / (2.0 * CELL_GRAN))
                * (2.0 * CELL_GRAN);

        double total_pixels = total_active_pixels + h_blank;

        double pixel_freq = total_pixels / h_period;

        double h_freq = 1000.0 / h_period;

        double h_sync = Math.round(H_SYNC_PERCENT / 100.0 * total_pixels
                / CELL_GRAN)
                * CELL_GRAN;

        double h_front_porch = (h_blank / 2.0) - h_sync;

        return new VesaGTF((int) total_active_pixels,
                (int) (total_active_pixels + h_front_porch),
                (int) (total_active_pixels + h_front_porch + h_sync),
                (int) total_pixels, vLines, (int) (vLines + MIN_PORCH),
                (int) (vLines + MIN_PORCH + V_SYNC_RQD), (int) total_v_lines,
                pixel_freq, h_freq, frequency);
    }

    public int getHFrameLength() {
        return hFrameLength;
    }

    public double getHFrequency() {
        return hFrequency;
    }

    public int getHResolution() {
        return hResolution;
    }

    public int getHSyncEnd() {
        return hSyncEnd;
    }

    public int getHSyncStart() {
        return hSyncStart;
    }

    public double getPixelClock() {
        return pixelClock;
    }

    public int getVFrameLength() {
        return vFrameLength;
    }

    public double getVFrequency() {
        return vFrequency;
    }

    public int getVResolution() {
        return vResolution;
    }

    public int getVSyncEnd() {
        return vSyncEnd;
    }

    public int getVSyncStart() {
        return vSyncStart;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(100);
        sb.append("Resolution ");
        sb.append(hResolution);
        sb.append("x");
        sb.append(vResolution);
        sb.append("@");
        sb.append(vFrequency);
        sb.append("Hz\nHSS: ");
        sb.append(hSyncStart);
        sb.append(" HSE: ");
        sb.append(hSyncEnd);
        sb.append(" HFL: ");
        sb.append(hFrameLength);
        sb.append("\nVSS: ");
        sb.append(vSyncStart);
        sb.append(" VSE: ");
        sb.append(vSyncEnd);
        sb.append(" VFL: ");
        sb.append(vFrameLength);
        sb.append(" PCLK: ");
        sb.append(pixelClock);
        sb.append(" HFREQ: ");
        sb.append(hFrequency);

        return sb.toString();
    }

    /*
     * Test code..
     */
    public static void main(String[] args) {
        // Test some default values..
        VesaGTF gtf1 = VesaGTF.calculate(800, 600, 50);
        VesaGTF gtf2 = VesaGTF.calculate(800, 600, 60);
        VesaGTF gtf3 = VesaGTF.calculate(800, 600, 70);
        System.out.println("800x600 Resolution:");
        System.out.println("50Hz:\n" + gtf1.toString());
        System.out.println("60Hz:\n" + gtf2.toString());
        System.out.println("70Hz:\n" + gtf3.toString());

        VesaGTF gtf4 = VesaGTF.calculate(1024, 768, 60);
        System.out.println("1024x768 Resolution:\n60Hz:\n" + gtf4.toString());
    }
}
