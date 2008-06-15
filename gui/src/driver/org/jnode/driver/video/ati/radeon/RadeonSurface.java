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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.system.MemoryResource;

/**
 * Drawing implementation for the Radeon driver.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonSurface extends AbstractSurface {
    private final RadeonAcceleration accel;

    private final BitmapGraphics bitmapGraphics;

    private final ColorModel colorModel;

    private final RadeonCore kernel;

    private final MemoryResource screen;

    /**
     * Initialize this instance.
     */
    public RadeonSurface(RadeonCore kernel, RadeonConfiguration config,
            BitmapGraphics bitmapGraphics, MemoryResource screen, RadeonAcceleration accel) {
        super(config.getScreenWidth(), config.getScreenHeight());
        this.kernel = kernel;
        this.bitmapGraphics = bitmapGraphics;
        this.colorModel = config.getColorModel();
        this.screen = screen;
        this.accel = accel;
    }

    /**
     * @see org.jnode.driver.video.Surface#close()
     */
    public void close() {
        kernel.close();
        screen.release();
        super.close();
    }

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
     */
    protected int convertColor(Color color) {
        return color.getRGB();
    }

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        bitmapGraphics.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * @see org.jnode.driver.video.Surface#drawAlphaRaster(java.awt.image.Raster,
     *      AffineTransform, int, int, int, int, int, int, java.awt.Color)
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX,
            int dstY, int width, int height, Color color) {
        bitmapGraphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, width, height,
                convertColor(color));
    }

    /**
     * @see org.jnode.driver.video.Surface#drawCompatibleRaster(java.awt.image.Raster,
     *      int, int, int, int, int, int, java.awt.Color)
     */
    public void drawCompatibleRaster(Raster raster, int srcX, int srcY, int dstX, int dstY,
            int width, int height, Color bgColor) {
        if (bgColor == null) {
            bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height);
        } else {
            bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height,
                    convertColor(bgColor));
        }
    }

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int,
     *      int)
     */
    protected void drawPixel(int x, int y, int color, int mode) {
        bitmapGraphics.drawPixels(x, y, 1, color, mode);
    }

    /**
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    @Override
    public int getRGBPixel(int x, int y) {
        return bitmapGraphics.doGetPixel(x, y);
    }

    @Override
    public int[] getRGBPixels(Rectangle region) {
        return bitmapGraphics.doGetPixels(region);
    }
}
