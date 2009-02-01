/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.awt.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.Raster;
import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.system.MultiMediaMemoryResource;

public abstract class AbstractBitmapGraphics extends BitmapGraphics {
    protected final int bytesPerLine;

    protected final int height;

    /**
     * My logger
     */
    protected static final Logger log = Logger.getLogger(AbstractBitmapGraphics.class);

    protected final MultiMediaMemoryResource mem;

    /**
     * Offset of first pixel in mem (in bytes)
     */
    protected final int offset;

    protected final int width;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Create a new instance
     *
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    protected AbstractBitmapGraphics(MemoryResource mem, int width, int height,
                                     int offset, int bytesPerLine) {
        this.mem = mem.asMultiMediaMemoryResource();
        this.offset = offset;
        this.bytesPerLine = bytesPerLine;
        this.width = width;
        this.height = height;
    }

    protected abstract int getOffset(int x, int y);


    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public final void copyArea(final int srcX, final int srcY, final int w, final int h,
                               final int deltaX, final int deltaY) {
        if (w < 1 || h < 1)
            return;

        final int dstX = srcX + deltaX;
        final int dstY = srcY + deltaY;

        final int bytesForWidth = getBytesForWidth(w);

        if (deltaY < 0) {
            int srcOfs = getOffset(srcX, srcY);
            int dstOfs = getOffset(dstX, dstY);
            for (int row = 0; row < h; row++) {
                mem.copy(srcOfs, dstOfs, bytesForWidth);
                srcOfs += bytesPerLine;
                dstOfs += bytesPerLine;
            }
        } else {
            int srcOfs = getOffset(srcX, srcY + h - 1);
            int dstOfs = getOffset(dstX, dstY + h - 1);
            for (int row = 0; row < h; row++) {
                mem.copy(srcOfs, dstOfs, bytesForWidth);
                srcOfs -= bytesPerLine;
                dstOfs -= bytesPerLine;
            }
        }

        /*
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doCopyArea(srcX, srcY, w, h, dstX, dstY);
            }
        }
        */

    }

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX The upper left x coordinate of the source
     * @param srcY The upper left y coordinate of the source
     * @param dstX The upper left x coordinate of the destination
     * @param dstY The upper left y coordinate of the destination
     * @param w
     * @param h
     */
    public final void drawImage(Raster src, int srcX, int srcY, int dstX,
                                int dstY, int w, int h) {
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawImage(src, srcX, srcY, dstX, dstY, w, h);
            }
        }
    }

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX    The upper left x coordinate of the source
     * @param srcY    The upper left y coordinate of the source
     * @param dstX    The upper left x coordinate of the destination
     * @param dstY    The upper left y coordinate of the destination
     * @param w
     * @param h
     * @param bgColor The color to use for transparent pixels
     */
    public final void drawImage(Raster src, int srcX, int srcY, int dstX,
                                int dstY, int w, int h, int bgColor) {
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawImage(src, srcX, srcY, dstX, dstY, w, h, bgColor);
            }
        }
    }

    /**
     * Draw an raster of alpha values using a given color onto to this surface.
     * The given raster is a 1 band gray type raster.
     *
     * @param raster
     * @param srcX   The upper left x coordinate within the raster
     * @param srcY   The upper left y coordinate within the raster
     * @param dstX   The upper left destination x coordinate
     * @param dstY   The upper left destination y coordinate
     * @param w
     * @param h
     * @param color  The color to use.
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX,
                                int srcY, int dstX, int dstY, int w, int h, int color) {
        if (tx != null) {
            Point2D dst = tx.transform(new Point2D.Float(dstX, dstY), null);
            dstX = (int) dst.getX();
            dstY = (int) dst.getY();
        }
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawAlphaRaster(raster, srcX, srcY, dstX, dstY, w, h, color);
            }
        }
    }

    /**
     * Draw a line at location x,y that is w long using the given color.
     *
     * @param x
     * @param y
     * @param w
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    public final void drawLine(int x, int y, int w, int color, int mode) {
        try {
            if ((y >= 0) && (y < height) && (x < width)) {
                if (x < 0) {
                    w += x;
                    x = 0;
                }
                w = Math.min(w, width - x);
                if (w > 0) {
                    doDrawLine(x, y, w, color, mode);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error("Index out of bounds: x=" + x + ", y=" + y + ", w=" + w +
                ", width=" + width + ", height=" + height);
        }
    }

    /**
     * Draw a pixel at location x,y using the given color.
     *
     * @param x
     * @param y
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    public final void drawPixels(int x, int y, int count, int color, int mode) {
        try {
            if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
                doDrawPixels(x, y, count, color, mode);
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error("Index out of bounds: x=" + x + ", y=" + y + ", width=" +
                width + ", height=" + height);
        }
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int color, int mode) {
        if ((x >= 0) && (x < this.width) && (y >= 0) && (y < this.height) &&
            (x + width >= 0) && (x + width < this.width) && (y + height >= 0) && (y + height < this.height)) {

            for (int i = 0; i < height; i++)
                doDrawPixels(x, y + i, width, color, mode);

        } else {
            // super.fillRect(x, y, width, height, color, mode);
        }
    }

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    protected void doCopyArea(int srcX, int srcY, int width, int height,
                              int dstX, int dstY) {
        int srcOfs = getOffset(srcX, srcY);
        int dstOfs = getOffset(dstX, dstY);
        final int bytesForWidth = getBytesForWidth(width);

        for (int row = 0; row < height; row++) {
            mem.copy(srcOfs, dstOfs, bytesForWidth);
            srcOfs += bytesPerLine;
            dstOfs += bytesPerLine;
        }
    }

    protected abstract int getBytesForWidth(int width);

    protected abstract void doDrawAlphaRaster(Raster raster, int srcX,
                                              int srcY, int dstX, int dstY, int width, int height, int color);

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX   The upper left x coordinate of the source
     * @param srcY   The upper left y coordinate of the source
     * @param dstX   The upper left x coordinate of the destination
     * @param dstY   The upper left y coordinate of the destination
     * @param width
     * @param height
     */
    protected abstract void doDrawImage(Raster src, int srcX, int srcY,
                                        int dstX, int dstY, int width, int height);

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX    The upper left x coordinate of the source
     * @param srcY    The upper left y coordinate of the source
     * @param dstX    The upper left x coordinate of the destination
     * @param dstY    The upper left y coordinate of the destination
     * @param width
     * @param height
     * @param bgColor The color to use for transparent pixels
     */
    protected abstract void doDrawImage(Raster src, int srcX, int srcY,
                                        int dstX, int dstY, int width, int height, int bgColor);

    /**
     * Draw a line at location x,y that is w long using the given color.
     *
     * @param x
     * @param y
     * @param w
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    protected abstract void doDrawLine(int x, int y, int w, int color, int mode);

    /**
     * Draw a number of pixels at location x,y using the given color.
     *
     * @param x
     * @param y
     * @param count
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    protected abstract void doDrawPixels(int x, int y, int count, int color,
                                         int mode);

}
