/*
 * $Id$
 *
 * JNode.org
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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;

import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.vgahw.DisplayMode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaConfiguration extends FrameBufferConfiguration {

    /** Bits/pixel */
    private final int bpp;
    private final NVidiaVgaState vgaState;
    private final DisplayMode mode;
    private final int screen;
    private final int horiz;
    private final int arb0;
    private final int arb1;
    private final int vpll;

    /*
     * { 0x115, 32, 800, 600, 0, 0, 3, 16, 162571, { crt { 127, 99, 99, 131,
     * 106, 26, 114, 240, 0, 96, 0, 0, 0, 0, 0, 0, 89, 13, 87, 144, 0, 87, 115,
     * 227, 255 }, att { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
     * 65, 0, 15, 0, 0 }, gra { 0, 0, 0, 0, 0, 64, 5, 15, 255 }, seq { 3, 1, 15,
     * 0, 14 }, 43 }
     */
    public static final NVidiaConfiguration VESA_115 =
        new NVidiaConfiguration(32, 0, 0, 3, 16, 162571,
            new NVidiaVgaState(new int[]{3, 1, 15, 0, 14},
                new int[]{127, 99, 99, 131, 106, 26, 114, 240, 0, 96, 0, 0, 0, 0, 0,
                    0, 89, 13, 87, 144, 0, 87, 115, 227, 255},
                new int[]{0, 0, 0, 0, 0, 64, 5, 15, 255},
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 65, 0, 15, 0, 0}, 0x2B),
            new DisplayMode("40000 800 840 968 1056 600 601 605 628"));

    /*
     * { 0x118, 32, 1024, 768, 0, 0, 3, 17, 95757, { crt { 163, 127, 127, 135,
     * 132, 149, 36, 245, 0, 96, 0, 0, 0, 0, 0, 0, 3, 9, 255, 0, 0, 255, 37,
     * 227, 255 }, att { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
     * 65, 0, 15, 0, 0 }, gra { 0, 0, 0, 0, 0, 64, 5, 15, 255 }, seq { 3, 1, 15,
     * 0, 14 }, 235 }
     * 
     */
    public static final NVidiaConfiguration VESA_118 =
        new NVidiaConfiguration(32, 0, 0, 3, 17, 95757,
            new NVidiaVgaState(new int[]{3, 1, 15, 0, 14},
                new int[]{163, 127, 127, 135, 132, 149, 36, 245, 0, 96, 0, 0, 0, 0, 0, 0, 3, 9, 255, 0,
                    0, 255, 37, 227, 255},
                new int[]{0, 0, 0, 0, 0, 64, 5, 15, 255},
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 65, 0, 15, 0, 0}, 0xEB),
            new DisplayMode("65000 1024 1048 1184 1344 768 771 777 806"));

    /**
     * @param bpp
     * @param screen
     * @param horiz
     */
    private NVidiaConfiguration(int bpp, int screen, int horiz, int arb0, int arb1, int vpll,
            NVidiaVgaState vgaState, DisplayMode mode) {
        super(mode.getWidth(), mode.getHeight(), createColorModel(bpp));
        this.bpp = bpp;
        this.screen = screen;
        this.horiz = horiz;
        this.arb0 = arb0;
        this.arb1 = arb1;
        this.vpll = vpll;
        this.vgaState = vgaState;
        this.mode = mode;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferConfiguration#createCompatibleImage(int,
     *      int, int)
     */
    public JNodeBufferedImage createCompatibleImage(int w, int h, int transparency) {
        return new JNodeBufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /** Gets the number of bits/pixel for this configuration */
    public int getBitsPerPixel() {
        return bpp;
    }

    /** Gets the vga state for this configuration */
    public NVidiaVgaState getVgaState() {
        return vgaState;
    }

    private static ColorModel createColorModel(int bpp) {
        return new DirectColorModel(bpp, 0xff0000, 0x00ff00, 0x0000ff);
    }

    /**
     * @return The VPLL register value
     */
    public final int getVpll() {
        return this.vpll;
    }

    /**
     * @return The ARBITRATION0 register value
     */
    public final int getArb0() {
        return this.arb0;
    }

    /**
     * @return The ARBITRATION1 register value
     */
    public final int getArb1() {
        return this.arb1;
    }

    /**
     * @return The HORIZ register value
     */
    public final int getHoriz() {
        return this.horiz;
    }

    /**
     * @return The SCREEN register value
     */
    public final int getScreen() {
        return this.screen;
    }

    /**
     * @return The display mode
     */
    public final DisplayMode getMode() {
        return this.mode;
    }
}
