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

package org.jnode.driver.video.ati.mach64;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;

import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.vgahw.DisplayMode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Mach64Configuration extends FrameBufferConfiguration {
    private final int bitsPerPixel;

    private final DisplayMode displayMode;

    public static final Mach64Configuration VESA_115 =
            new Mach64Configuration(32, new DisplayMode("40000 800 840 968 1056 600 601 605 628"));

    public static final Mach64Configuration VESA_118 =
            new Mach64Configuration(32,
                    new DisplayMode("65000 1024 1048 1184 1344 768 771 777 806"));

    /**
     * Initialize this instance.
     * 
     * @param bpp
     * @param mode
     */
    public Mach64Configuration(int bpp, DisplayMode mode) {
        super(mode.getWidth(), mode.getHeight(), createColorModel(bpp));
        this.bitsPerPixel = bpp;
        this.displayMode = mode;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferConfiguration#createCompatibleImage(int,
     *      int, int)
     */
    public JNodeBufferedImage createCompatibleImage(int w, int h, int transparency) {
        return new JNodeBufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Create a color model for a given bits per pixel.
     * 
     * @param bpp
     * @return
     */
    private static ColorModel createColorModel(int bpp) {
        return new DirectColorModel(bpp, 0xff0000, 0x00ff00, 0x0000ff);
    }

    /**
     * @return Returns the bitsPerPixel.
     */
    public final int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /**
     * Gets the number of bytes per line
     * 
     * @return
     */
    public final int getBytesPerLine() {
        return (displayMode.getWidth() * bitsPerPixel) >> 3;
    }

    /**
     * @return Returns the displayMode.
     */
    public final DisplayMode getDisplayMode() {
        return this.displayMode;
    }

    public String toString() {
        return "bpp=" + bitsPerPixel + ", mode={" + displayMode + "}";
    }

}
